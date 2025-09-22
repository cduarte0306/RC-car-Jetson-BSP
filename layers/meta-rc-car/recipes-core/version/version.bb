SUMMARY = "Version recipe for device versioning"
LICENSE = "CLOSED"

require recipes-core/images/versions.inc

S = "${WORKDIR}"

# Capture build version from OE
VERSION_STRING := "${OE_VERSION}"

do_install() {
    install -d ${D}${sysconfdir}/versions
    echo "${VERSION_STRING}" > ${D}${sysconfdir}/versions/oe-version.txt
}

FILES:${PN} += "${sysconfdir}/versions/oe-version.txt"
