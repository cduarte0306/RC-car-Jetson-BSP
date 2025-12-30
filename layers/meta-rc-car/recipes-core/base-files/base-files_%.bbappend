RC_CAR_DATA_PARTLABEL ??= "UDA"
RC_CAR_DATA_PART_FSTYPE ??= "ext4"

do_install:append() {
    install -d ${D}/data

    if [ -e ${D}${sysconfdir}/fstab ]; then
        if ! grep -qE '^[^#]*[[:space:]]+/data[[:space:]]' ${D}${sysconfdir}/fstab; then
            echo "/dev/disk/by-partlabel/${RC_CAR_DATA_PARTLABEL}  /data  ${RC_CAR_DATA_PART_FSTYPE}  defaults,nofail,x-systemd.device-timeout=10  0  2" >> ${D}${sysconfdir}/fstab
        fi
    fi
}
