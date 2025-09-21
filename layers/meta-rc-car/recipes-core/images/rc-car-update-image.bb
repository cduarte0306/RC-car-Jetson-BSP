DESCRIPTION = "RC Car SWUpdate image"
LICENSE = "MIT"

require versions.inc

inherit rc-car-update-type

# Set VERSION from OE_VERSION (you can override in versions.inc)
VERSION := "${OE_VERSION}"

# Set the SWU output filename with timestamp and version
python __set_swu_output_name() {
    import datetime
    timestamp = datetime.datetime.now().strftime("%Y%m%d%H%M")
    version = d.getVar("VERSION")
    machine = d.getVar("MACHINE")
    image_name = f"rc-car-update-{machine}-{version}-{timestamp}"
    d.setVar("IMAGE_NAME", image_name)
    d.setVar("SWUPDATE_OUTPUTIMAGE", image_name + ".swu")
}

__set_swu_output_name[vardeps] += "VERSION MACHINE"
addhandler __set_swu_output_name
