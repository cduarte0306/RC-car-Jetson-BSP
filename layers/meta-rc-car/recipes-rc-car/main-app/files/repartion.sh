#!/usr/bin/env bash

# One-shot helper to add a DATA partition on NVMe and mount it at /data.
# Defaults are safe for Jetson Orin NVMe; adjust SIZE if you want more than 8G.

set -euo pipefail

DEVICE="${DEVICE:-/dev/nvme0n1}"
LABEL="${LABEL:-DATA}"
SIZE="${SIZE:-8G}"
MNT="${MNT:-/data}"
FSTYPE="${FSTYPE:-ext4}"
GPT_BAK="/var/backups/gpt-$(date +%Y%m%d-%H%M%S).sgdisk"

require_root() {
    if [ "$(id -u)" != "0" ]; then
        echo "Must run as root" >&2
        exit 1
    fi
}

check_tools() {
    for t in sgdisk partprobe udevadm blkid mkfs."${FSTYPE}"; do
        command -v "$t" >/dev/null 2>&1 || { echo "Missing tool: $t" >&2; exit 1; }
    done
}

label_exists() {
    [ -e "/dev/disk/by-partlabel/${LABEL}" ]
}

already_mounted() {
    mountpoint -q "${MNT}"
}

ensure_fstab_entry() {
    local entry="LABEL=${LABEL} ${MNT} ${FSTYPE} defaults 0 2"
    if ! grep -qs "^LABEL=${LABEL}[[:space:]]\+${MNT}[[:space:]]" /etc/fstab; then
        echo "${entry}" >> /etc/fstab
        echo "Added persistent mount to /etc/fstab"
    else
        echo "fstab entry already present"
    fi
}

main() {
    require_root
    check_tools

    if label_exists; then
        echo "${LABEL} already exists; skipping partition create."
    else
        echo "Backing up GPT to ${GPT_BAK} ..."
        mkdir -p "$(dirname "${GPT_BAK}")"
        sgdisk --backup="${GPT_BAK}" "${DEVICE}"

        echo "Creating ${SIZE} partition labeled ${LABEL} at end of ${DEVICE} ..."
        sgdisk -n 0:0:+${SIZE} -c 0:${LABEL} -t 0:8300 "${DEVICE}"

        echo "Rereading partition table ..."
        partprobe "${DEVICE}" || true
        udevadm settle

        echo "Waiting for by-partlabel/${LABEL} ..."
        for i in $(seq 1 20); do
            label_exists && break
            sleep 1
        done
        if ! label_exists; then
            echo "New partition with label ${LABEL} not found; aborting." >&2
            exit 1
        fi
    fi

    PART="/dev/disk/by-partlabel/${LABEL}"

    if blkid "${PART}" | grep -q "${FSTYPE}"; then
        echo "${PART} already formatted as ${FSTYPE}."
    else
        echo "Formatting ${PART} as ${FSTYPE} ..."
        mkfs.${FSTYPE} -L "${LABEL}" "${PART}"
    fi

    mkdir -p "${MNT}"
    if already_mounted; then
        echo "${MNT} already mounted; done."
        ensure_fstab_entry
        exit 0
    fi

    echo "Mounting ${PART} at ${MNT} ..."
    mount "${PART}" "${MNT}"
    ensure_fstab_entry

    echo "Done. ${PART} mounted at ${MNT} and persisted in /etc/fstab."
}

main "$@"