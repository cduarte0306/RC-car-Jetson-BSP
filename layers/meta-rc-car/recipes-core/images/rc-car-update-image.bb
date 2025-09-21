DESCRIPTION = "RC Car SWUpdate image"
LICENSE = "MIT"

require versions.inc
inherit rc-car-update-type

# Safe default values to avoid basehash mismatch
VERSION := "OE: ${OE_VERSION}"
IMAGE_NAME = "rc-car-update-${MACHINE}-${VERSION}"
SWUPDATE_OUTPUTIMAGE = "${IMAGE_NAME}.swu"

# Override dynamically inside the task (safe way)
python do_swuimage:prepend() {
    import datetime

    # Create timestamp string
    timestamp = datetime.datetime.now().strftime("%Y%m%d%H%M")

    # Clean up version string (remove spaces or colons)
    version = d.getVar("VERSION").replace(" ", "_").replace(":", "_")
    machine = d.getVar("MACHINE")

    # Override IMAGE_NAME and SWUPDATE_OUTPUTIMAGE at runtime
    image_name = f"rc-car-update-{machine}-{version}-{timestamp}"
    d.setVar("IMAGE_NAME", image_name)
    d.setVar("SWUPDATE_OUTPUTIMAGE", f"{image_name}.swu")

    bb.note(f"[rc-car-update-image] Final SWUpdate image: {image_name}.swu")
}
