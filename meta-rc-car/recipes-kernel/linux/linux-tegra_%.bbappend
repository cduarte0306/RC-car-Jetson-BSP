FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# SRC_URI += " \
#     file://rc-car-spi.cfg \
# "

SRC_URI += " \
    file://rc-car-spi.cfg \
    file://0001-rc-car-reconfigure-spi1-pinmux.patch \
"

# KERNEL_CONFIG_FRAGMENTS += "file://rc-car-spi.cfg"