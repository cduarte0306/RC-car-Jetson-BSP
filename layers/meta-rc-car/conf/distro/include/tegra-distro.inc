# ---- Identity / versioning ----
DISTRO                = "tegra-distro"
DISTRO_NAME           = "RC-Car Tegra Distro"
DISTRO_VERSION_BASE   = "1.0.0"
DISTRO_CODENAME       = "master"
DISTRO_VERSION        = "${DISTRO_VERSION_BASE}+snapshot-${METADATA_REVISION}"

SDK_VENDOR            = "-tdsdk"
SDK_VERSION := "${@'${DISTRO_VERSION}'.replace('snapshot-${METADATA_REVISION}','snapshot')}"
SDK_VERSION[vardepvalue] = "${SDK_VERSION}"

MAINTAINER            = "Carlos / RC-Car Team <noreply@example.com>"
TARGET_VENDOR         = "-oe4t"

# Optional sanity knobs (purely informational for your layer templates)
REQUIRED_TD_BBLAYERS_CONF_VERSION = "${DISTRO}-1"
LOCALCONF_VERSION = "2"

# ---- Distro features / policy ----
# Must include 'systemd' and 'usrmerge' so systemd builds & is not skipped.
TD_DEFAULT_DISTRO_FEATURES = "largefile opengl ptest multiarch wayland vulkan systemd pam virtualization usrmerge"
DISTRO_FEATURES ?= "${DISTRO_FEATURES_DEFAULT} ${TD_DEFAULT_DISTRO_FEATURES}"

# Package format policy
PACKAGE_CLASSES ?= "package_rpm"

# Host distros you’ve tested on (purely advisory to sanity checker)
SANITY_TESTED_DISTROS ?= " \
    ubuntu-20.04 \n \
    ubuntu-22.04 \n \
    ubuntu-24.04 \n \
"

# ---- Init/systemd selection ----
# Old style (kept for layers that read INIT_MANAGER) and the canonical virtual.
INIT_MANAGER = "systemd"
VIRTUAL-RUNTIME_init_manager = "systemd"

# Udev comes from systemd
PREFERRED_PROVIDER_virtual/udev = "systemd-udev"

# Avoid backfilling sysvinit bits when using systemd
DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"
VIRTUAL-RUNTIME_initscripts = ""

# ---- Common includes / hardening / native toolchain bootstrap ----
require conf/distro/include/no-static-libs.inc
require conf/distro/include/yocto-uninative.inc
require conf/distro/include/security_flags.inc
INHERIT += "uninative"

# ---- Licenses you accept (add/trim as you need) ----
LICENSE_FLAGS_ACCEPTED += "commercial_faad2 commercial_x264"

# Example NVIDIA flash layout knob used by some BSPs (harmless if unused)
USE_REDUNDANT_FLASH_LAYOUT_DEFAULT ?= "1"

# ---- Kernel preferences (optional) ----
# If you are on meta-tegra, the kernel is usually linux-tegra; don’t force linux-yocto.
# Leave these commented unless you intend to use linux-yocto.
# PREFERRED_VERSION_linux-yocto ?= "6.6%"
# PREFERRED_VERSION_linux-yocto-rt ?= "6.6%"

# ---- SDK naming ----
SDK_NAME        = "${DISTRO}-${TCLIBC}-${SDKMACHINE}-${IMAGE_BASENAME}-${TUNE_PKGARCH}-${MACHINE}"
SDKPATHINSTALL  = "/opt/${DISTRO}/${SDK_VERSION}"

# Keep empty unless you have a custom libc variant tag to append
TCLIBCAPPEND = ""
