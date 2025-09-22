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

VERSION_STRING = "0.0.0"

python do_extract_version() {
    import os
    import re

    version = "0.0.0"
    version_file = os.path.join(d.getVar('S'), 'version.h')
    if not os.path.exists(version_file):
        bb.warn("Version file not found: %s" % version_file)
    else:
        major = minor = build = None
        with open(version_file, 'r') as f:
            for line in f:
                if "#define VERSION_MAJOR" in line:
                    major = re.search(r'\d+', line).group()
                elif "#define VERSION_MINOR" in line:
                    minor = re.search(r'\d+', line).group()
                elif "#define VERSION_BUILD" in line:
                    build = re.search(r'\d+', line).group()
        if major and minor and build:
            version = f"{major}.{minor}.{build}"

    bb.warn(f"Extracted version: {version}")
    VERSION_STRING = version
}

addtask extract_version after do_unpack before do_configure

do_install() {
    install -d ${D}/opt/rc-car/update-server
    install -m 0755 ${B}/src/rc-car-updater ${D}/opt/rc-car/update-server/

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/rc-car-updater.service ${D}${systemd_system_unitdir}/

    # Log directory
    install -d {D}/var/log/rc-car-updater/

    install -d ${D}${sysconfdir}/versions
    echo "Version : ${VERSION_STRING}"
    echo "${VERSION_STRING}" > ${D}${sysconfdir}/versions/updater-version.txt
}

FILES:${PN} += "/opt/rc-car/update-server/"
FILES:${PN} += "${systemd_system_unitdir}/rc-car-updater.service"
FILES:${PN} += "${sysconfdir}/rc-car/versions/updater-version.txt"

SYSTEMD_SERVICE:${PN} = "rc-car-updater.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
