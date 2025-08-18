#!/usr/bin/env bash
# Source me:  . ./env.sh [builddir]
# Example:    . ./env.sh build

# ----- strict mode (but we'll disable it around Poky source) -----
set -o pipefail
set -u
set -e

# detect if sourced
__SRC=0
if [ -n "${BASH_SOURCE:-}" ] && [ "${BASH_SOURCE[0]}" != "$0" ]; then __SRC=1; fi
die(){ echo "env.sh: $*" >&2; [ $__SRC -eq 1 ] && return 1 || exit 1; }

# resolve paths
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]:-${0}}")" && pwd)"
BUILD_DIR="${1:-${REPO_ROOT}/build}"
[ "${BUILD_DIR#/}" != "$BUILD_DIR" ] || BUILD_DIR="${REPO_ROOT}/${BUILD_DIR}"

POKY_INIT="${REPO_ROOT}/layers/poky/oe-init-build-env"
[ -f "$POKY_INIT" ] || die "Can't find ${POKY_INIT}"

mkdir -p "${BUILD_DIR}/conf"

# ----- source Poky safely (don't let -e/-u kill our shell) -----
# save current shell options
__OLD_E="$(set +o | grep -E '^set -o errexit' || true)"
__OLD_U="$(set +o | grep -E '^set -o nounset' || true)"

set +e
set +u
# shellcheck disable=SC1090
. "$POKY_INIT" "$BUILD_DIR"
__RC=$?
# restore options
eval "$__OLD_E"
eval "$__OLD_U"
[ $__RC -eq 0 ] || die "oe-init-build-env failed (rc=$__RC). Check TEMPLATECONF or your layer layout."

# ---- bblayers.conf (keep ${TOPDIR} literal) ----
cat > conf/bblayers.conf <<'EOF'
# POKY_BBLAYERS_CONF_VERSION is increased each time build/conf/bblayers.conf changes incompatibly
POKY_BBLAYERS_CONF_VERSION = "2"

BBPATH = "${TOPDIR}"
BBFILES ?= ""

BBLAYERS ?= " \
  ${TOPDIR}/../layers/poky/meta \
  ${TOPDIR}/../layers/poky/meta-poky \
  ${TOPDIR}/../layers/poky/meta-yocto-bsp \
  ${TOPDIR}/../layers/meta-openembedded/meta-oe \
  ${TOPDIR}/../layers/meta-openembedded/meta-networking \
  ${TOPDIR}/../layers/meta-openembedded/meta-python \
  ${TOPDIR}/../layers/meta-tegra \
  ${TOPDIR}/../layers/meta-rc-car \
"
EOF

# ---- local.conf (your full content, written once) ----
cat > conf/local.conf <<'EOF'
MACHINE ?= "rc-car-machine"
TUNE_FEATURES:append = " aarch64"
TARGET_ARCH = "aarch64"

SERIAL_CONSOLE ?= "115200 ttyS0"
EXTRA_IMAGE_FEATURES = "debug-tweaks"
SDKMACHINE = "x86_64"

DISTRO ?= "poky"
PACKAGE_CLASSES ?= "package_rpm"
EXTRA_IMAGE_FEATURES ?= "debug-tweaks"
USER_CLASSES ?= "buildstats"

# Systemd defaults
DISTRO_FEATURES:append = " systemd usrmerge"
VIRTUAL-RUNTIME_init_manager = "systemd"
PREFERRED_PROVIDER_virtual/udev = "systemd-udev"
DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"
VIRTUAL-RUNTIME_initscripts = ""

LICENSE_FLAGS_ACCEPTED += "commercial"
CONF_VERSION = "2"

# Jetson-specific overlays and modules
KERNEL_MODULE_AUTOLOAD += "spidev"
TEGRA_PLUGIN_MANAGER_OVERLAYS:append = " tegra234-p3767-0000+p3509-a02-hdr40.dtbo"
IMAGE_INSTALL:append = " kernel-module-spidev spidev-test"

# Parallelism
BB_NUMBER_THREADS ?= "16"
PARALLEL_MAKE ?= "-j16"
EOF

echo "Yocto environment ready."
echo "  BUILDDIR: ${BUILDDIR}"
echo "Run: bitbake-layers show-layers"
