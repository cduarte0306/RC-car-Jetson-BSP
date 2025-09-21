SUMMARY = "Version recipe for device versioning"
LICENSE = "CLOSED"

require recipes-core/images/versions.inc

S = "${WORKDIR}"

VERSION_STRING := "OE: ${OE_VERSION}"

do_install() {
    install -d ${D}${sysconfdir}/versions
    echo "${VERSION_STRING}" >> ${D}${sysconfdir}/versions/version.txt
}

FILES:${PN} += "${sysconfdir}/rc-car/version.txt"
