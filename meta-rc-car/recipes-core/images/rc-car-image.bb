DESCRIPTION = "RC Car custom image"
LICENSE = "MIT"

inherit core-image

IMAGE_INSTALL += " \
    packagegroup-core-boot \
    bash \
    openssh \
    nano \
    hostapd \
    dnsmasq \
    iproute2 \
    ethtool \
    net-tools \
    tcpdump \
    procps \
    systemd \
    busybox \
    opencv \
    opencv-samples \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-ugly \
    gstreamer1.0-libav \
    gstreamer1.0-plugins-tegra \
    gstreamer1.0-plugins-tegra-binaryonly \
    gdbserver \
    boost \
    dtc \
    nodejs \
    networkmanager \
    networkmanager-nmcli \
    wpa-supplicant \
    iw \
    wireless-regdb-static \
    linux-firmware \
    rc-car-eth-setup \
    python3 \
"

TOOLCHAIN_TARGET_TASK:append = " boost"

DISTRO_FEATURES:append = " systemd usrmerge"
DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"
VIRTUAL-RUNTIME_init_manager = "systemd"
VIRTUAL-RUNTIME_initscripts = "systemd-compat-units"

LICENSE_FLAGS_ACCEPTED += "commercial"

IMAGE_CLASSES += "image_types_tegra"
LICENSE_FLAGS_ACCEPTED += "commercial"

KERNEL_MODULE_AUTOLOAD += "spidev"
TEGRA_PLUGIN_MANAGER_OVERLAYS:append = " tegra234-p3767-0000+p3509-a02-hdr40.dtbo"
IMAGE_INSTALL:append = " kernel-module-spidev spidev-test"

# Enable NM at boot
SYSTEMD_AUTO_ENABLE:append = " NetworkManager"
