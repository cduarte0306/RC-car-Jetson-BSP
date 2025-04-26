SUMMARY = "Configure Jetson Nano WiFi as an AP"
LICENSE = "MIT"
PR = "r0"

SRC_URI = "file://hostapd.conf \
           file://dnsmasq.conf \
           file://setup-wifi-ap.service \
           "

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "setup-wifi-ap.service"

do_install() {
    install -d ${D}${sysconfdir}/hostapd
    install -m 0644 ${WORKDIR}/hostapd.conf ${D}${sysconfdir}/hostapd/hostapd.conf

    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/dnsmasq.conf ${D}${sysconfdir}/dnsmasq.conf

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/setup-wifi-ap.service ${D}${systemd_system_unitdir}/
}
