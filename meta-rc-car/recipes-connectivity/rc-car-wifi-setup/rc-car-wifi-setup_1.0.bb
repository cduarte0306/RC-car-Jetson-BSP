SUMMARY = "WiFi AP setup for Carlos' RC Car"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r0"

SRC_URI = "file://hostapd.conf \
           file://dnsmasq.conf \
           file://10-wlan0.network \
           file://hostapd.service \
           file://dnsmasq.service \
          "

S = "${WORKDIR}"

inherit systemd

SYSTEMD_AUTO_ENABLE = "disable"
SYSTEMD_SERVICE:${PN} = ""

do_install() {
    install -d ${D}${sysconfdir}/rc-car-wifi
    install -d ${D}${sysconfdir}/systemd/network
    install -d ${D}${systemd_system_unitdir}/rc-car-wifi
    install -d ${D}${systemd_system_unitdir}/multi-user.target.wants

    install -m 0644 ${WORKDIR}/hostapd.conf ${D}${sysconfdir}/rc-car-wifi/
    install -m 0644 ${WORKDIR}/dnsmasq.conf ${D}${sysconfdir}/rc-car-wifi/
    install -m 0644 ${WORKDIR}/10-wlan0.network ${D}${sysconfdir}/systemd/network/
    install -m 0644 ${WORKDIR}/hostapd.service ${D}${systemd_system_unitdir}/rc-car-wifi/
    install -m 0644 ${WORKDIR}/dnsmasq.service ${D}${systemd_system_unitdir}/rc-car-wifi/

    ln -sf ../rc-car-wifi/hostapd.service ${D}${systemd_system_unitdir}/multi-user.target.wants/hostapd.service
    ln -sf ../rc-car-wifi/dnsmasq.service ${D}${systemd_system_unitdir}/multi-user.target.wants/dnsmasq.service
}

FILES:${PN} += "${systemd_system_unitdir} ${sysconfdir}"
