SUMMARY = "Ethernet static IP setup for RC Car using NetworkManager"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r1"

SRC_URI = "file://static-enP8p1s0.nmconnection"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

do_install() {
    install -d ${D}${sysconfdir}/NetworkManager/system-connections
    install -m 0600 ${WORKDIR}/static-enP8p1s0.nmconnection \
        ${D}${sysconfdir}/NetworkManager/system-connections/
}

FILES:${PN} += "${sysconfdir}/NetworkManager/system-connections"
