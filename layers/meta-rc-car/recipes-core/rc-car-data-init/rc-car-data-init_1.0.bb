SUMMARY = "One-time initializer for persistent /data partition"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit systemd

SRC_URI = " \
    file://rc-car-data-init.sh.in \
    file://rc-car-data-init.service \
"

RC_CAR_DATA_PARTLABEL ??= "UDA"
RC_CAR_DATA_PART_FSTYPE ??= "ext4"
RC_CAR_DATA_MOUNTPOINT ??= "/data"
RC_CAR_DATA_MIN_SIZE_BYTES ??= "${RC_CAR_DATA_PART_SIZE_BYTES}"
RC_CAR_DATA_AUTO_RESIZE ??= "0"

SYSTEMD_SERVICE:${PN} = "rc-car-data-init.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} += " \
    bash \
    e2fsprogs-e2fsck \
    e2fsprogs-mke2fs \
    e2fsprogs-resize2fs \
    gptfdisk \
    util-linux-blkid \
    util-linux-blockdev \
    util-linux-lsblk \
    util-linux-mount \
    util-linux-mountpoint \
    util-linux-partx \
"

do_install() {
    install -d ${D}${sbindir}
    sed \
        -e "s|@PARTLABEL@|${RC_CAR_DATA_PARTLABEL}|g" \
        -e "s|@FSTYPE@|${RC_CAR_DATA_PART_FSTYPE}|g" \
        -e "s|@MOUNTPOINT@|${RC_CAR_DATA_MOUNTPOINT}|g" \
        -e "s|@MIN_SIZE_BYTES@|${RC_CAR_DATA_MIN_SIZE_BYTES}|g" \
        -e "s|@AUTO_RESIZE@|${RC_CAR_DATA_AUTO_RESIZE}|g" \
        ${WORKDIR}/rc-car-data-init.sh.in > ${D}${sbindir}/rc-car-data-init
    chmod 0755 ${D}${sbindir}/rc-car-data-init

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/rc-car-data-init.service ${D}${systemd_system_unitdir}/
}
