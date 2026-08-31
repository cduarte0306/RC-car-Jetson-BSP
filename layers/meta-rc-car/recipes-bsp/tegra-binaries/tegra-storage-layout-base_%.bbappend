# FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# SRC_URI += " \
#     file://flash_l4t_t234_nvme_data.xml \
#     file://flash_l4t_t234_nvme_rootfs_ab_data.xml \
# "

# # Use the layer-provided external layouts instead of expecting them in the L4T kit.
# PARTITION_FILE_EXTERNAL = "${THISDIR}/files/${PARTITION_LAYOUT_EXTERNAL}"

# do_install:append() {
#     install -m 0644 ${THISDIR}/files/flash_l4t_t234_nvme_data.xml ${D}${datadir}/l4t-storage-layout/flash_l4t_t234_nvme_data.xml
#     install -m 0644 ${THISDIR}/files/flash_l4t_t234_nvme_rootfs_ab_data.xml ${D}${datadir}/l4t-storage-layout/flash_l4t_t234_nvme_rootfs_ab_data.xml
# }
