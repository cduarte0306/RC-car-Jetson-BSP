SUMMARY = "Web server for RC car"
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464"

SRCREV = "${AUTOREV}"
SRC_URI = "git://github.com/cduarte0306/RC-Car-Web-Server.git;protocol=https;branch=main \
           file://rc-car-web-server.service"

S = "${WORKDIR}/git"

# Assume this runs with Python 3 and uses the standard interpreter
RDEPENDS:${PN} += "python3 bash"

inherit systemd

do_install() {
    # Install the Python script and web files
    install -d ${D}/opt/rc-car/web-server
    install -d ${D}/home/images/
    cp -r ${S}/src/* ${D}/opt/rc-car/web-server/

    # Install the systemd service
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/rc-car-web-server.service ${D}${systemd_system_unitdir}/
}

FILES:${PN} += "/opt/rc-car/web-server/"
FILES:${PN} += "${systemd_system_unitdir}/rc-car-web-server.service"
FILES:${PN} += "/home/images"

SYSTEMD_SERVICE:${PN} = "rc-car-web-server.service"
