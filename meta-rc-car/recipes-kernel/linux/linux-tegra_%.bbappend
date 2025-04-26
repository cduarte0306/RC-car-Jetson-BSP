FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
    file://rc-car-spi.cfg \
    file://rc-car-spi-overlay.dts \
"

KERNEL_CONFIG_FRAGMENTS += "rc-car-spi.cfg"
