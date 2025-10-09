SUMMARY = "RC Car Device Tree and Overlays"
LICENSE = "CLOSED"

SRC_URI = "file://rc-car-devicetree-overlay.dts"

S = "${WORKDIR}"
DEPENDS += "dtc-native"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit deploy

do_compile() {
    # -@ is required so overlays can resolve symbols
    dtc -@ -I dts -O dtb \
        -o ${B}/rc-car-devicetree-overlay.dtbo \
        ${S}/rc-car-devicetree-overlay.dts
}

do_install() {
    install -d ${D}/boot
    install -m 0644 ${B}/rc-car-devicetree-overlay.dtbo \
        ${D}/boot/rc-car-devicetree-overlay.dtbo
}

# Make sure ${DEPLOYDIR} exists and we drop the file there
do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${B}/rc-car-devicetree-overlay.dtbo \
        ${DEPLOYDIR}/rc-car-devicetree-overlay.dtbo
}

# Make the task run in the right dir and at the right time
do_deploy[dirs] = "${DEPLOYDIR}"
addtask deploy after do_install before do_build

FILES:${PN} += "/boot/rc-car-devicetree-overlay.dtbo"
