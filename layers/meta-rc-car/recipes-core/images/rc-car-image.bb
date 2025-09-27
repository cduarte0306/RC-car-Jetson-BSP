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
    iproute2 \
    ethtool \
    net-tools \
    tcpdump \
    procps \
    systemd \
    busybox \
    opencv \
    opencv-samples \
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
    networkmanager \
    networkmanager-nmcli \
    wpa-supplicant \
    iw \
    wireless-regdb-static \
    linux-firmware \
    rc-car-eth-setup \
    libgpiod \
    libgpiod-tools \
    python3 \
    python3-flask \
    python3-gunicorn \
    swupdate \
    kernel-module-spidev \
    spidev-test \
    update-server \
    update-web-server \
    rc-car-nav \
"
# Python images
IMAGE_INSTALL:append = " \
  python3-core \
  python3-flask \
  python3-werkzeug \
  python3-jinja2 \
  python3-itsdangerous \
  python3-click \
  "

IMAGE_INSTALL:append = " version"

IMAGE_CLASSES += "image_types_tegra"

TOOLCHAIN_TARGET_TASK:append = " boost"

LICENSE_FLAGS_ACCEPTED += "commercial"

IMAGE_CLASSES += "image_types_tegra"
LICENSE_FLAGS_ACCEPTED += "commercial"

KERNEL_MODULE_AUTOLOAD += "spidev"

IMAGE_FSTYPES:append = " tar.gz"
IMAGE_FSTYPES:append = " tegraflash"

# Enable NM at boot
SYSTEMD_AUTO_ENABLE:append = " NetworkManager"
TOOLCHAIN_TARGET_TASK:append = " swupdate-dev"

SYSTEMD_AUTO_ENABLE:append = " NetworkManager-wait-online.service"