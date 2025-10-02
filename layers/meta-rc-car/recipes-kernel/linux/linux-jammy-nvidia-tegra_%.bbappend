FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " \
    file://tegra234-p3767-0000+p3509-a02-spi-overlay.dts;subdir=git/arch/${ARCH}/boot/dts/nvidia \
"

KERNEL_DEVICETREE:append = " nvidia/tegra234-p3767-0000+p3509-a02-spi-overlay.dtbo"
KERNEL_DTB_OVERLAYS:append = " nvidia/tegra234-p3767-0000+p3509-a02-spi-overlay.dtbo"

# Apply both overlays
TEGRA_PLUGIN_MANAGER_OVERLAYS:append = " tegra234-p3767-0000+p3509-a02-hdr40.dtbo"
TEGRA_PLUGIN_MANAGER_OVERLAYS:append = " tegra234-p3767-0000+p3509-a02-spi-overlay.dtbo"

# OR, if you prefer build-time apply instead of plugin-manager:
# KERNEL_DEVICETREE_APPLY_OVERLAYS:append = " \
#    nvidia/tegra234-p3767-0000+p3509-a02-hdr40.dtbo \
#    nvidia/tegra234-p3767-0000+p3509-a02-spi-overlay.dtbo \
# "
