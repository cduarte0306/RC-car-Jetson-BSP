UMMARY = "Update server for RC car"
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c6808d7433e09d2b717e8d022fd743f3"

SRCREV = "${AUTOREV}"
SRC_URI = "gitsm://github.com/cduarte0306/RC-Car-navigation-and-control.git;protocol=https;branch=main;submodules=1 \
           file://rc-car-nav.service"

S = "${WORKDIR}/git"

DEPENDS = "cmake-native boost systemd"

inherit cmake pkgconfig systemd

EXTRA_OECMAKE = ""

APP_FOLDER = "rc-car-nav"
APP_NAME = "rc-car-nav"

do_install() {
    install -d ${D}/opt/rc-car/${APP_FOLDER}
    install -m 0755 ${B}/src/${APP_NAME} ${D}/opt/rc-car/${APP_FOLDER}/

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/${APP_NAME}.service ${D}${systemd_system_unitdir}/

    # Log directory
    install -d {D}/var/log/

    install -d ${D}${sysconfdir}/versions
    version_file=${S}/version.h
    if [ -f "$version_file" ]; then
        major=$(grep '#define VERSION_MAJOR' $version_file | awk '{print $3}')
        minor=$(grep '#define VERSION_MINOR' $version_file | awk '{print $3}')
        build=$(grep '#define VERSION_BUILD' $version_file | awk '{print $3}')
        version="${major}.${minor}.${build}"
    else
        version="0.0.0"
    fi

    echo "$version" > ${D}${sysconfdir}/versions/${APP_NAME}-version.txt
}

FILES:${PN} += "/opt/rc-car/${APP_FOLDER}/"
FILES:${PN} += "${systemd_system_unitdir}/${APP_NAME}.service"
FILES:${PN} += "${sysconfdir}/versions/${APP_NAME}-version.txt"
FILES:${PN} += "/var/log/${APP_FOLDER}/"

SYSTEMD_SERVICE:${PN} = "${APP_NAME}.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"