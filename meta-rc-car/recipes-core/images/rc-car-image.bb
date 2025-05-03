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
    procps \
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
    boost \
"

TOOLCHAIN_TARGET_TASK:append = " boost"

# Tegra-specific image types
IMAGE_CLASSES += "image_types_tegra"
IMAGE_FSTYPES = "tegraflash"
LICENSE_FLAGS_ACCEPTED += "commercial"

# Boot partition contents
IMAGE_BOOT_FILES = " \
    Image;Image \
    tegra210-p3448-0000-p3449-0000-b00.dtb;tegra210-p3448-0000-p3449-0000-b00.dtb \
    extlinux.conf;extlinux/extlinux.conf \
"

# Copy extlinux.conf from layer's files/ directory into boot partition
SRC_URI += "file://extlinux.conf"

do_install:append() {
    install -d ${D}/boot/extlinux
    install -m 0644 ${WORKDIR}/extlinux.conf ${D}/boot/extlinux/extlinux.conf
}
