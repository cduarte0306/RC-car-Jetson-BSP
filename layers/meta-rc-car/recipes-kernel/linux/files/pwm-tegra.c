// SPDX-License-Identifier: GPL-2.0-or-later
/*
 * drivers/pwm/pwm-tegra.c
 *
 * Tegra pulse-width-modulation controller driver
 *
 * Copyright (c) 2010-2020, NVIDIA Corporation.
 * Based on arch/arm/plat-mxc/pwm.c by Sascha Hauer <s.hauer@pengutronix.de>
 *
 * Overview of Tegra Pulse Width Modulator Register:
 * 1. 13-bit: Frequency division (SCALE)
 * 2. 8-bit : Pulse division (DUTY)
 * 3. 1-bit : Enable bit
 *
 * The PWM clock frequency is divided by 256 before subdividing it based
 * on the programmable frequency division value to generate the required
 * frequency for PWM output. The maximum output frequency that can be
 * achieved is (max rate of source clock) / 256.
 * e.g. if source clock rate is 408 MHz, maximum output frequency can be:
 * 408 MHz/256 = 1.6 MHz.
 * This 1.6 MHz frequency can further be divided using SCALE value in PWM.
 *
 * PWM pulse width: 8 bits are usable [23:16] for varying pulse width.
 * To achieve 100% duty cycle, program Bit [24] of this register to
 * 1’b1. In which case the other bits [23:16] are set to don't care.
 *
 * Limitations:
 * -	When PWM is disabled, the output is driven to inactive.
 * -	It does not allow the current PWM period to complete and
 *	stops abruptly.
 *
 * -	If the register is reconfigured while PWM is running,
 *	it does not complete the currently running period.
 *
 * -	If the user input duty is beyond acceptible limits,
 *	-EINVAL is returned.
 */

#include <linux/cdev.h>
#include <linux/clk.h>
#include <linux/device.h>
#include <linux/pwm.h>
#include <linux/err.h>
#include <linux/io.h>
#include <linux/module.h>
#include <linux/mutex.h>
#include <linux/of.h>
#include <linux/of_device.h>
#include <linux/pm_opp.h>
#include <linux/pwm.h>
#include <linux/platform_device.h>
#include <linux/pinctrl/consumer.h>
#include <linux/pm_runtime.h>
#include <linux/slab.h>
#include <linux/reset.h>
#include <linux/uaccess.h>

#include <soc/tegra/common.h>

#define PWM_ENABLE	(1 << 31)
#define PWM_DUTY_WIDTH	8
#define PWM_DUTY_SHIFT	16
#define PWM_SCALE_WIDTH	13
#define PWM_SCALE_SHIFT	0

/* ioctl commands for PWM character device */
#define TEGRA_PWM_IOC_MAGIC		'P'
#define TEGRA_PWM_IOC_GET_DUTY		_IOR(TEGRA_PWM_IOC_MAGIC, 0, unsigned int)
#define TEGRA_PWM_IOC_SET_DUTY		_IOW(TEGRA_PWM_IOC_MAGIC, 1, unsigned int)
#define TEGRA_PWM_IOC_GET_FREQ		_IOR(TEGRA_PWM_IOC_MAGIC, 2, unsigned int)
#define TEGRA_PWM_IOC_SET_FREQ		_IOW(TEGRA_PWM_IOC_MAGIC, 3, unsigned int)
#define TEGRA_PWM_IOC_ENABLE		_IO(TEGRA_PWM_IOC_MAGIC, 4)
#define TEGRA_PWM_IOC_DISABLE		_IO(TEGRA_PWM_IOC_MAGIC, 5)

struct tegra_pwm_soc {
	unsigned int num_channels;

	/* Maximum IP frequency for given SoCs */
	unsigned long max_frequency;
};

struct tegra_pwm_chip {
	struct pwm_chip chip;
	struct device *dev;

	struct clk *clk;
	struct reset_control*rst;

	unsigned long clk_rate;
	unsigned long min_period_ns;

	void __iomem *regs;

	const struct tegra_pwm_soc *soc;

	/* Character device fields */
	struct cdev cdev;
	dev_t devt;
	int instance;
};

static inline struct tegra_pwm_chip *to_tegra_pwm_chip(struct pwm_chip *chip)
{
	return container_of(chip, struct tegra_pwm_chip, chip);
}

static inline u32 pwm_readl(struct tegra_pwm_chip *chip, unsigned int num)
{
	return readl(chip->regs + (num << 4));
}

static inline void pwm_writel(struct tegra_pwm_chip *chip, unsigned int num,
			     unsigned long val)
{
	writel(val, chip->regs + (num << 4));
}

static int tegra_pwm_config(struct pwm_chip *chip, struct pwm_device *pwm,
			    int duty_ns, int period_ns)
{
	struct tegra_pwm_chip *pc = to_tegra_pwm_chip(chip);
	unsigned long long c = duty_ns;
	unsigned long rate, required_clk_rate;
	u32 val = 0;
	int err;

	/*
	 * Convert from duty_ns / period_ns to a fixed number of duty ticks
	 * per (1 << PWM_DUTY_WIDTH) cycles and make sure to round to the
	 * nearest integer during division.
	 */
	c *= (1 << PWM_DUTY_WIDTH);
	c = DIV_ROUND_CLOSEST_ULL(c, period_ns);

	val = (u32)c << PWM_DUTY_SHIFT;

	/*
	 *  min period = max clock limit >> PWM_DUTY_WIDTH
	 */
	if (period_ns < pc->min_period_ns)
		return -EINVAL;

	/*
	 * Compute the prescaler value for which (1 << PWM_DUTY_WIDTH)
	 * cycles at the PWM clock rate will take period_ns nanoseconds.
	 *
	 * num_channels: If single instance of PWM controller has multiple
	 * channels (e.g. Tegra210 or older) then it is not possible to
	 * configure separate clock rates to each of the channels, in such
	 * case the value stored during probe will be referred.
	 *
	 * If every PWM controller instance has one channel respectively, i.e.
	 * nums_channels == 1 then only the clock rate can be modified
	 * dynamically (e.g. Tegra186 or Tegra194).
	 */
	if (pc->soc->num_channels == 1) {
		/*
		 * Rate is multiplied with 2^PWM_DUTY_WIDTH so that it matches
		 * with the maximum possible rate that the controller can
		 * provide. Any further lower value can be derived by setting
		 * PFM bits[0:12].
		 *
		 * required_clk_rate is a reference rate for source clock and
		 * it is derived based on user requested period. By setting the
		 * source clock rate as required_clk_rate, PWM controller will
		 * be able to configure the requested period.
		 */
		required_clk_rate = DIV_ROUND_UP_ULL((u64)NSEC_PER_SEC << PWM_DUTY_WIDTH,
						     period_ns);

		if (required_clk_rate > clk_round_rate(pc->clk, required_clk_rate))
			/*
			 * required_clk_rate is a lower bound for the input
			 * rate; for lower rates there is no value for PWM_SCALE
			 * that yields a period less than or equal to the
			 * requested period. Hence, for lower rates, double the
			 * required_clk_rate to get a clock rate that can meet
			 * the requested period.
			 */
			required_clk_rate *= 2;

		err = dev_pm_opp_set_rate(pc->dev, required_clk_rate);
		if (err < 0)
			return -EINVAL;

		/* Store the new rate for further references */
		pc->clk_rate = clk_get_rate(pc->clk);
	}

	/* Consider precision in PWM_SCALE_WIDTH rate calculation */
	rate = mul_u64_u64_div_u64(pc->clk_rate, period_ns,
				   (u64)NSEC_PER_SEC << PWM_DUTY_WIDTH);

	/*
	 * Since the actual PWM divider is the register's frequency divider
	 * field plus 1, we need to decrement to get the correct value to
	 * write to the register.
	 */
	if (rate > 0)
		rate--;
	else
		return -EINVAL;

	/*
	 * Make sure that the rate will fit in the register's frequency
	 * divider field.
	 */
	if (rate >> PWM_SCALE_WIDTH)
		return -EINVAL;

	val |= rate << PWM_SCALE_SHIFT;

	/*
	 * If the PWM channel is disabled, make sure to turn on the clock
	 * before writing the register. Otherwise, keep it enabled.
	 */
	if (!pwm_is_enabled(pwm)) {
		err = pm_runtime_resume_and_get(pc->dev);
		if (err)
			return err;
	} else
		val |= PWM_ENABLE;

	pwm_writel(pc, pwm->hwpwm, val);

	/*
	 * If the PWM is not enabled, turn the clock off again to save power.
	 */
	if (!pwm_is_enabled(pwm))
		pm_runtime_put(pc->dev);

	return 0;
}

static int tegra_pwm_enable(struct pwm_chip *chip, struct pwm_device *pwm)
{
	struct tegra_pwm_chip *pc = to_tegra_pwm_chip(chip);
	int rc = 0;
	u32 val;

	rc = pm_runtime_resume_and_get(pc->dev);
	if (rc)
		return rc;

	val = pwm_readl(pc, pwm->hwpwm);
	val |= PWM_ENABLE;
	pwm_writel(pc, pwm->hwpwm, val);

	return 0;
}

static void tegra_pwm_disable(struct pwm_chip *chip, struct pwm_device *pwm)
{
	struct tegra_pwm_chip *pc = to_tegra_pwm_chip(chip);
	u32 val;

	val = pwm_readl(pc, pwm->hwpwm);
	val &= ~PWM_ENABLE;
	pwm_writel(pc, pwm->hwpwm, val);

	pm_runtime_put_sync(pc->dev);
}

/* PWM open handler	*/
static int tegra_pwm_open(struct inode *inode, struct file *filp)
{
    struct tegra_pwm_chip *pc;

    pc = container_of(inode->i_cdev, struct tegra_pwm_chip, cdev);
    filp->private_data = pc;

    return 0;
}

/* Character device ioctl handler - thread-safe */
static long tegra_pwm_ioctl(struct file *filp, unsigned int cmd, unsigned long arg)
{
	struct tegra_pwm_chip *pc = filp->private_data;
	unsigned int val;
	u32 reg_val, duty_val;

	if (_IOC_TYPE(cmd) != TEGRA_PWM_IOC_MAGIC)
		return -ENOTTY;

	switch (cmd) {
	case TEGRA_PWM_IOC_GET_DUTY:
		/* Get duty cycle of PWM channel 0 (in percent) */
		if (pc->chip.npwm > 0) {
			reg_val = pwm_readl(pc, 0);
			val = ((reg_val >> PWM_DUTY_SHIFT) & 0xFF) * 100 / 256;
			if (copy_to_user((void __user *)arg, &val, sizeof(val)))
				return -EFAULT;
		}
		break;

	case TEGRA_PWM_IOC_SET_DUTY:
		/* Set duty cycle of PWM channel 0 (in percent, 0-100) */
		if (copy_from_user(&val, (void __user *)arg, sizeof(val)))
			return -EFAULT;
		if (val > 100)
			return -EINVAL;
		if (pc->chip.npwm > 0) {
			reg_val = pwm_readl(pc, 0);
			duty_val = (val * 256 / 100) & 0xFF;
			reg_val = (reg_val & ~(0xFF << PWM_DUTY_SHIFT)) |
				  (duty_val << PWM_DUTY_SHIFT);
			pwm_writel(pc, 0, reg_val);
		}
		break;

	case TEGRA_PWM_IOC_GET_FREQ:
	{
		if (pc->chip.npwm > 0) {
			u32 reg = pwm_readl(pc, 0);
			u32 scale = reg & ((1 << PWM_SCALE_WIDTH) - 1);
			unsigned long freq = pc->clk_rate /
								((scale + 1) << PWM_DUTY_WIDTH);
			if (copy_to_user((void __user *)arg, &freq, sizeof(freq)))
				return -EFAULT;
		}
		break;
	}

	case TEGRA_PWM_IOC_SET_FREQ:
	{
		if (copy_from_user(&val, (void __user *)arg, sizeof(val)))
			return -EFAULT;
		if (val == 0)
			return -EINVAL;

		if (pc->chip.npwm > 0) {
			/* Read current register values */
			u32 reg = pwm_readl(pc, 0);
			u32 duty_bits = (reg >> PWM_DUTY_SHIFT) & 0xFF;

			/* Convert frequency to period in ns */
			unsigned long long period_ns = div64_ul(NSEC_PER_SEC, val);
            pr_info("tegra_pwm: Setting period %llu\n", period_ns);

			/* Convert duty_bits back to time */
			unsigned long long duty_ns = (period_ns * duty_bits) >> 8;

			struct pwm_device *pwm = &pc->chip.pwms[0];

			/* Use the official Tegra path */
			return tegra_pwm_config(&pc->chip, pwm,
									(int)duty_ns, (int)period_ns);
		}
		return -ENODEV;
	}

	case TEGRA_PWM_IOC_ENABLE:
			/* Enable PWM channel 0 using the normal driver path */
			if (pc->chip.npwm > 0)
				return tegra_pwm_enable(&pc->chip, &pc->chip.pwms[0]);
			return -ENODEV;

	case TEGRA_PWM_IOC_DISABLE:
		/* Disable PWM channel 0 using the normal driver path */
		if (pc->chip.npwm > 0) {
			tegra_pwm_disable(&pc->chip, &pc->chip.pwms[0]);
			return 0;
		}
		return -ENODEV;

	default:
		return -ENOTTY;
	}

	return 0;
}

static const struct file_operations tegra_pwm_fops = {
	.owner = THIS_MODULE,
	.open           = tegra_pwm_open,
	.unlocked_ioctl = tegra_pwm_ioctl,
};

static const struct pwm_ops tegra_pwm_ops = {
	.config = tegra_pwm_config,
	.enable = tegra_pwm_enable,
	.disable = tegra_pwm_disable,
	.owner = THIS_MODULE,
};

/* Global state for character device - protected by mutex */
static struct class *tegra_pwm_class;
static dev_t tegra_pwm_devt;
static int tegra_pwm_major;
static int tegra_pwm_instance_count;
static DEFINE_MUTEX(tegra_pwm_dev_mutex);

static int tegra_pwm_probe(struct platform_device *pdev)
{
	struct tegra_pwm_chip *pwm;
	int ret;

	pwm = devm_kzalloc(&pdev->dev, sizeof(*pwm), GFP_KERNEL);
	if (!pwm)
		return -ENOMEM;

	pwm->soc = of_device_get_match_data(&pdev->dev);
	pwm->dev = &pdev->dev;

	pwm->regs = devm_platform_ioremap_resource(pdev, 0);
	if (IS_ERR(pwm->regs))
		return PTR_ERR(pwm->regs);

	platform_set_drvdata(pdev, pwm);

	pwm->clk = devm_clk_get(&pdev->dev, NULL);
	if (IS_ERR(pwm->clk))
		return PTR_ERR(pwm->clk);

	ret = devm_tegra_core_dev_init_opp_table_common(&pdev->dev);
	if (ret)
		return ret;

	pm_runtime_enable(&pdev->dev);
	ret = pm_runtime_resume_and_get(&pdev->dev);
	if (ret)
		return ret;

	/* Set maximum frequency of the IP */
	ret = dev_pm_opp_set_rate(pwm->dev, pwm->soc->max_frequency);
	if (ret < 0) {
		dev_err(&pdev->dev, "Failed to set max frequency: %d\n", ret);
		goto put_pm;
	}

	/*
	 * The requested and configured frequency may differ due to
	 * clock register resolutions. Get the configured frequency
	 * so that PWM period can be calculated more accurately.
	 */
	pwm->clk_rate = clk_get_rate(pwm->clk);

	/* Set minimum limit of PWM period for the IP */
	pwm->min_period_ns =
	    (NSEC_PER_SEC / (pwm->soc->max_frequency >> PWM_DUTY_WIDTH)) + 1;

	pwm->rst = devm_reset_control_get_exclusive(&pdev->dev, "pwm");
	if (IS_ERR(pwm->rst)) {
		ret = PTR_ERR(pwm->rst);
		dev_err(&pdev->dev, "Reset control is not found: %d\n", ret);
		goto put_pm;
	}

	reset_control_deassert(pwm->rst);

	pwm->chip.dev = &pdev->dev;
	pwm->chip.ops = &tegra_pwm_ops;
	pwm->chip.npwm = pwm->soc->num_channels;

	ret = pwmchip_add(&pwm->chip);
	if (ret < 0) {
		dev_err(&pdev->dev, "pwmchip_add() failed: %d\n", ret);
		reset_control_assert(pwm->rst);
		goto put_pm;
	}

	/* Register character device for ioctl interface (thread-safe) */
	mutex_lock(&tegra_pwm_dev_mutex);
	
	if (!tegra_pwm_class) {
		tegra_pwm_class = class_create(THIS_MODULE, "tegra_pwm");
		if (IS_ERR(tegra_pwm_class)) {
			ret = PTR_ERR(tegra_pwm_class);
			dev_err(&pdev->dev, "Failed to create device class: %d\n", ret);
			mutex_unlock(&tegra_pwm_dev_mutex);
			goto remove_pwm;
		}
		ret = alloc_chrdev_region(&tegra_pwm_devt, 0, 10, "tegra_pwm");
		if (ret < 0) {
			dev_err(&pdev->dev, "Failed to allocate char device region: %d\n", ret);
			class_destroy(tegra_pwm_class);
			tegra_pwm_class = NULL;
			mutex_unlock(&tegra_pwm_dev_mutex);
			goto remove_pwm;
		}
		tegra_pwm_major = MAJOR(tegra_pwm_devt);
	}

	/* Assign unique instance number (thread-safe under mutex) */
	pwm->instance = tegra_pwm_instance_count++;
	pwm->devt = MKDEV(tegra_pwm_major, pwm->instance);
	
	mutex_unlock(&tegra_pwm_dev_mutex);

	cdev_init(&pwm->cdev, &tegra_pwm_fops);
	pwm->cdev.owner = THIS_MODULE;
	
	ret = cdev_add(&pwm->cdev, pwm->devt, 1);
	if (ret < 0) {
		dev_err(&pdev->dev, "Failed to add char device: %d\n", ret);
		goto remove_pwm;
	}

	device_create(tegra_pwm_class, &pdev->dev, pwm->devt, pwm, "pwm%d", pwm->instance);

	dev_info(&pdev->dev, "Tegra PWM driver loaded, major device number: %d, instance: %d\n", 
		 tegra_pwm_major, pwm->instance);

	return 0;

remove_pwm:
	pwmchip_remove(&pwm->chip);
	reset_control_assert(pwm->rst);

put_pm:
	pm_runtime_put_sync_suspend(&pdev->dev);
	pm_runtime_force_suspend(&pdev->dev);
	return ret;
}

static int tegra_pwm_remove(struct platform_device *pdev)
{
	struct tegra_pwm_chip *pc = platform_get_drvdata(pdev);

	/* Remove character device */
	device_destroy(tegra_pwm_class, pc->devt);
	cdev_del(&pc->cdev);

	pwmchip_remove(&pc->chip);

	reset_control_assert(pc->rst);

	pm_runtime_force_suspend(&pdev->dev);

	return 0;
}

static int __maybe_unused tegra_pwm_runtime_suspend(struct device *dev)
{
	struct tegra_pwm_chip *pc = dev_get_drvdata(dev);
	int err;

	clk_disable_unprepare(pc->clk);

	err = pinctrl_pm_select_sleep_state(dev);
	if (err) {
		clk_prepare_enable(pc->clk);
		return err;
	}

	return 0;
}

static int __maybe_unused tegra_pwm_runtime_resume(struct device *dev)
{
	struct tegra_pwm_chip *pc = dev_get_drvdata(dev);
	int err;

	err = pinctrl_pm_select_default_state(dev);
	if (err)
		return err;

	err = clk_prepare_enable(pc->clk);
	if (err) {
		pinctrl_pm_select_sleep_state(dev);
		return err;
	}

	return 0;
}

static const struct tegra_pwm_soc tegra20_pwm_soc = {
	.num_channels = 4,
	.max_frequency = 48000000UL,
};

static const struct tegra_pwm_soc tegra186_pwm_soc = {
	.num_channels = 1,
	.max_frequency = 102000000UL,
};

static const struct tegra_pwm_soc tegra194_pwm_soc = {
	.num_channels = 1,
	.max_frequency = 408000000UL,
};

static const struct of_device_id tegra_pwm_of_match[] = {
	{ .compatible = "nvidia,tegra20-pwm", .data = &tegra20_pwm_soc },
	{ .compatible = "nvidia,tegra186-pwm", .data = &tegra186_pwm_soc },
	{ .compatible = "nvidia,tegra194-pwm", .data = &tegra194_pwm_soc },
	{ }
};
MODULE_DEVICE_TABLE(of, tegra_pwm_of_match);

static const struct dev_pm_ops tegra_pwm_pm_ops = {
	SET_RUNTIME_PM_OPS(tegra_pwm_runtime_suspend, tegra_pwm_runtime_resume,
				NULL)
	SET_SYSTEM_SLEEP_PM_OPS(pm_runtime_force_suspend,
				pm_runtime_force_resume)
};

static struct platform_driver tegra_pwm_driver = {
	.driver = {
		.name = "tegra-pwm",
		.of_match_table = tegra_pwm_of_match,
		.pm = &tegra_pwm_pm_ops,
	},
	.probe = tegra_pwm_probe,
	.remove = tegra_pwm_remove,
};

static int __init tegra_pwm_module_init(void)
{
	pr_info("tegra_pwm: Loading Tegra PWM driver module with ioctl interface\n");
	return platform_driver_register(&tegra_pwm_driver);
}

static void __exit tegra_pwm_module_exit(void)
{
	platform_driver_unregister(&tegra_pwm_driver);

	/* Clean up global char device state (thread-safe under mutex) */
	mutex_lock(&tegra_pwm_dev_mutex);
	if (tegra_pwm_class) {
		class_destroy(tegra_pwm_class);
		tegra_pwm_class = NULL;
	}
	if (tegra_pwm_major > 0) {
		unregister_chrdev_region(MKDEV(tegra_pwm_major, 0), 10);
		tegra_pwm_major = 0;
	}
	tegra_pwm_instance_count = 0;
	mutex_unlock(&tegra_pwm_dev_mutex);
	
	pr_info("tegra_pwm: Unloaded\n");
}

module_init(tegra_pwm_module_init);
module_exit(tegra_pwm_module_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Sandipan Patra <spatra@nvidia.com>");
MODULE_DESCRIPTION("Tegra PWM controller driver");
MODULE_ALIAS("platform:tegra-pwm");
