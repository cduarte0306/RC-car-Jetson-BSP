SUMMARY = "Ethernet static IP setup for RC Car"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r0"

SRC_URI = "file://20-enP8p1s0.network\
           file://20-wlP1p1s0.network"

inherit systemd

# Ensure the build system can locate the files directory
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

do_install() {
    install -d ${D}${sysconfdir}/systemd/network
    install -m 0644 ${WORKDIR}/20-enP8p1s0.network ${D}${sysconfdir}/systemd/network/
    install -m 0644 ${WORKDIR}/20-wlP1p1s0.network ${D}${sysconfdir}/systemd/network/
}

FILES:${PN} += "${sysconfdir}/systemd/network"
