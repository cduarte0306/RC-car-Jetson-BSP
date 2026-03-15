SUMMARY = "WebSocket support for Flask"
HOMEPAGE = "https://github.com/miguelgrinberg/flask-sock"
SECTION = "devel/python"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9d272c9fe2437531b5bbecf4fcc82e24"

inherit pypi python_setuptools_build_meta

PYPI_PACKAGE = "flask-sock"

SRC_URI[sha256sum] = "e023b578284195a443b8d8bdb4469e6a6acf694b89aeb51315b1a34fcf427b7d"

RDEPENDS:${PN} += "\
    python3-flask \
    python3-simple-websocket \
    "
