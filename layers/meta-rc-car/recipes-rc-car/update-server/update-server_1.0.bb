SUMMARY = "Update server for RC car"
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464"

SRCREV = "${AUTOREV}"
SRC_URI = "gitsm://github.com/cduarte0306/RC-Car-Update-Server.git;protocol=https;branch=main;submodules=1 \
           file://rc-car-updater.service"

S = "${WORKDIR}/git"

DEPENDS = "cmake-native swupdate boost"

inherit cmake pkgconfig systemd

EXTRA_OECMAKE = ""

do_install() {
    # Install binary
    install -d ${D}/opt/rc-car/update-server
    install -m 0755 ${B}/src/rc-car-updater ${D}/opt/rc-car/update-server/

    # Install systemd service
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/rc-car-updater.service ${D}${systemd_system_unitdir}/
}

FILES:${PN} += "/opt/rc-car/update-server/"
FILES:${PN} += "${systemd_system_unitdir}/rc-car-updater.service"

SYSTEMD_SERVICE:${PN} = "rc-car-updater.service"
