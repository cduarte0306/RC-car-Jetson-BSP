FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

DEPENDS += "dtc-native"

SRC_URI += " \
    file://rc-car-spi.cfg \
    file://rc-car-spi-overlay.dts \
"

KERNEL_CONFIG_FRAGMENTS += "file://rc-car-spi.cfg"

do_compile:append() {
    dtc -@ -I dts -O dtb -o ${WORKDIR}/rc-car-spi-overlay.dtbo ${WORKDIR}/rc-car-spi-overlay.dts
}

do_deploy:append() {
    install -Dm 0644 ${WORKDIR}/rc-car-spi-overlay.dtbo ${DEPLOYDIR}/rc-car-spi-overlay.dtbo
}

IMAGE_BOOT_FILES += " \
    rc-car-spi-overlay.dtbo \
    "