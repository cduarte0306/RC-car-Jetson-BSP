SUMMARY = "Version file for RC Car image"
LICENSE = "CLOSED"
require recipes-core/images/versions.inc

SRC_URI = ""
S = "${WORKDIR}"

do_install() {
    install -d ${D}${sysconfdir}/rc-car
    echo "${VERSION}" > ${D}${sysconfdir}/rc-car/version.txt
}

FILES:${PN} += "${sysconfdir}/rc-car/version.txt"
