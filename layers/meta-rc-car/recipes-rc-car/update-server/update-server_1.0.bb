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
    install -d ${D}/opt/rc-car/update-server
    install -m 0755 ${B}/src/rc-car-updater ${D}/opt/rc-car/update-server/

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/rc-car-updater.service ${D}${systemd_system_unitdir}/

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

    echo "$version" > ${D}${sysconfdir}/versions/updater-version.txt
}

FILES:${PN} += "/opt/rc-car/update-server/"
FILES:${PN} += "${systemd_system_unitdir}/rc-car-updater.service"
FILES:${PN} += "${sysconfdir}/versions/updater-version.txt"
FILES:${PN} += "/var/log/rc-car-updater/"

SYSTEMD_SERVICE:${PN} = "rc-car-updater.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
