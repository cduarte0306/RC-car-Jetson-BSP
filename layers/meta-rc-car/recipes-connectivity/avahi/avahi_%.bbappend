FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://avahi-daemon.conf"

do_install:append() {
    install -m 0644 ${WORKDIR}/avahi-daemon.conf ${D}${sysconfdir}/avahi/avahi-daemon.conf
}