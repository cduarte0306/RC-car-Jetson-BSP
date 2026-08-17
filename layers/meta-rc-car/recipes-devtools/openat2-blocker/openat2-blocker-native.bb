SUMMARY = "LD_PRELOAD shim that makes openat2() fail with ENOSYS"
DESCRIPTION = "Works around a gap in the pseudo-native version pinned by poky \
(1.9.0+git), which has no openat2() wrapper. GNU tar >= 1.34 uses \
openat2()/RESOLVE_BENEATH for directory-fd caching whenever the host kernel \
supports it (Linux >= 5.6), and pseudo loses track of the resulting fd, \
corrupting later wrapped *at() calls on it. Preloading this shim ahead of \
libpseudo.so forces openat2() to fail with ENOSYS so tar falls back to plain \
openat()/mkdirat(), which pseudo already wraps correctly. Scoped narrowly to \
recipes that need it (see rc-car-nav's do_package) rather than bumping the \
shared pseudo-native tool, which cascades a resignature of every recipe's \
packaging tasks."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://openat2_enosys.c"

inherit native

do_compile() {
    ${BUILD_CC} ${BUILD_CFLAGS} -shared -fPIC -o openat2_enosys.so ${WORKDIR}/openat2_enosys.c -ldl
}

do_install() {
    install -d ${D}${libdir}
    install -m 0755 openat2_enosys.so ${D}${libdir}/openat2_enosys.so
}

FILES:${PN} += "${libdir}/openat2_enosys.so"
