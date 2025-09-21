DESCRIPTION = "RC Car SWUpdate image"
LICENSE = "MIT"

require versions.inc
inherit rc-car-update-type

# ✅ These MUST be static for BitBake to work
VERSION := "${OE_VERSION}"
IMAGE_NAME = "rc-car-update-${MACHINE}-${VERSION}"
SWUPDATE_OUTPUTIMAGE = "${IMAGE_NAME}.swu"

# ✅ Create timestamped copy AFTER the .swu is built
python do_timestamped_swu_copy() {
    import os
    import datetime

    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE')
    orig_filename = d.getVar('SWUPDATE_OUTPUTIMAGE')
    version = d.getVar('OE_VERSION').replace(" ", "_").replace(":", "_")
    machine = d.getVar('MACHINE')
    timestamp = datetime.datetime.now().strftime("%Y%m%d%H%M")

    timestamped_filename = f"rc-car-update-{machine}-{version}-{timestamp}.swu"
    src = os.path.join(deploy_dir, orig_filename)
    dst = os.path.join(deploy_dir, timestamped_filename)

    bb.note(f"[rc-car-update] Copying {src} -> {dst}")
    bb.utils.copyfile(src, dst)

    latest_link = os.path.join(deploy_dir, "latest.swu")
    if os.path.islink(latest_link) or os.path.exists(latest_link):
        os.remove(latest_link)
    os.symlink(timestamped_filename, latest_link)
    bb.note(f"[rc-car-update] Created symlink: latest.swu -> {timestamped_filename}")
}

addtask timestamped_swu_copy after do_swuimage