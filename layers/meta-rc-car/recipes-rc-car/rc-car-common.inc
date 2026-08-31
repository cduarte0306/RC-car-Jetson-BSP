# Common variables for RC Car recipes
# Define branches and tags for each recipe
# Users can override these in local.conf to customize revisions
#
# To pin to a specific tag, override in local.conf AND modify SRC_URI to use tag= instead of branch=
# Example in local.conf:
#   BRANCH:pn-rc-car-nav = "v1.1.1"
# Then modify recipe SRC_URI accordingly

# RC Car Navigation and Control
BRANCH:pn-rc-car-nav = "main"
SRCREV:pn-rc-car-nav = "cf9dbd43a57ce345a50acb8bc93b540eac4103f2"
TAG:pn-rc-car-nav = "V1.04.0000"

# Update Web Server
BRANCH:pn-update-web-server = "main"
SRCREV:pn-update-web-server = "94c12ee9d4a1bfd7bf6d53d4302864134cf6a61a"
TAG:pn-update-web-server = "V1.01.0000"

# Update Server
BRANCH:pn-update-server = "main"
SRCREV:pn-update-server = "002148bbc5742ead69adb9f7848279ddd1701c5b"
TAG:pn-update-server = "V1.01.0000"