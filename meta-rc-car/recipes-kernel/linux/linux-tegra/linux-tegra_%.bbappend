FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
    file://rc-car-spi-overlay.dts \
    file://spi-rc-car.cfg \
"

KERNEL_FEATURES:append = " spi-rc-car"

do_compile:append() {
    ${STAGING_BINDIR_NATIVE}/dtc -I dts -O dtb -@ \
        -o ${B}/rc-car-spi.dtbo \
        ${WORKDIR}/rc-car-spi-overlay.dts
}

do_install:append() {
    install -d ${D}/boot/overlays
    install -m 0644 ${B}/rc-car-spi.dtbo ${D}/boot/overlays/
}
