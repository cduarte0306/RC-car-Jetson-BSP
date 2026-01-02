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
SRCREV:pn-rc-car-nav = "926167854387d7566c7e6bed44336510c391f6ee"

# Update Web Server
BRANCH:pn-update-web-server = "main"
SRCREV:pn-update-web-server = "c26a0d73a31fb027c01fe56ade8648424c31fe79"
TAG:pn-update-web-server = "v1.00.0003"

# Update Server
BRANCH:pn-update-server = "main"
SRCREV:pn-update-server = "885a8760b011fde7e976a6698bde17c8e03b798d"