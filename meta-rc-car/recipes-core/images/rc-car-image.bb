DESCRIPTION = "RC Car custom image"
LICENSE = "MIT"

inherit core-image

IMAGE_INSTALL += " \
    packagegroup-core-boot \
    bash \
    openssh \
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
    dtc \
    ethtool \ 
    iproute2 \
    net-tools \
    tcpdump \
    busybox \
"

TOOLCHAIN_TARGET_TASK:append = " boost"

IMAGE_CLASSES += "image_types_tegra"
IMAGE_FSTYPES = "tegraflash"
LICENSE_FLAGS_ACCEPTED += "commercial"

KERNEL_MODULE_AUTOLOAD += "spidev"
TEGRA_PLUGIN_MANAGER_OVERLAYS:append  = " tegra234-p3767-0000+p3509-a02-hdr40.dtbo"
IMAGE_INSTALL:append= " kernel-module-spidev spidev-test "