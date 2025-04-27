DESCRIPTION = "RC Car custom image"
LICENSE = "MIT"

inherit core-image

# Install base packages into the image
IMAGE_INSTALL += " \
    packagegroup-core-boot \
    bash \
    openssh \
    linux-firmware-mt7601u \
    kernel-module-mt7601u \
    nano \
    hostapd \
    dnsmasq \
    net-tools \
    systemd \
    opencv \
    opencv-samples \
    rc-car-eth-setup \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-ugly \
    gstreamer1.0-libav \
    gstreamer1.0-plugins-tegra \
    gstreamer1.0-plugins-tegra-binaryonly \
    gdbserver \
"

# Output a WIC image compressed with gzip
IMAGE_CLASSES += "image_types_tegra"
IMAGE_FSTYPES = "tegraflash wic.gz"

LICENSE_FLAGS_ACCEPTED += "commercial"

# Files to include in the boot partition
IMAGE_BOOT_FILES = " \
    Image;Image \
    tegra210-p3448-0000-p3449-0000-b00.dtb;tegra210-p3448-0000-p3449-0000-b00.dtb \
    extlinux.conf \
"
WKS_FILE = "rc-car-wic.wks"
WKS_FILES_DIR = "${LAYERDIR}/wic"

# Stage extlinux.conf into DEPLOY_DIR_IMAGE so WIC can use it
python do_image_wic:append() {
    import shutil
    shutil.copyfile("${THISDIR}/extlinux.conf", "${DEPLOY_DIR_IMAGE}/extlinux.conf")
}
