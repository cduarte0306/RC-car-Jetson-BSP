# Common variables for RC Car recipes
# Define branches and tags for each recipe
# Users can override these in local.conf to customize revisions
#
# To pin to a specific tag, override in local.conf AND modify SRC_URI to use tag= instead of branch=
# Example in local.conf:
#   BRANCH:pn-rc-car-nav = "v1.1.1"
# Then modify recipe SRC_URI accordingly

# RC Car Navigation and Control
BRANCH:pn-rc-car-nav = "23-add-support-for-remote-updates"
SRCREV:pn-rc-car-nav = "cca87be4afe380f45d6c67a180d734b699c16543"
TAG:pn-rc-car-nav = "v1.03.0005"

# Update Web Server
BRANCH:pn-update-web-server = "24-feature-adapt-internal-updater-iface-pipe-to-new-updater-proxy-structure"
SRCREV:pn-update-web-server = "ed8c6aac0fa401ea67dfbc32e82ebbe276031144"
TAG:pn-update-web-server = "v1.00.0005"

# Update Server
BRANCH:pn-update-server = "main"
SRCREV:pn-update-server = "05703b778b4aa3a75aaefb29a1ac080c64620c00"
TAG:pn-update-server = "v1.00.0003"