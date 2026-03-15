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
SRCREV:pn-rc-car-nav = "119e40f367a475d3e618be932cdccf87c1123985"
TAG:pn-rc-car-nav = "v1.03.0005"

# Update Web Server
BRANCH:pn-update-web-server = "main"
SRCREV:pn-update-web-server = "4d56b1e6946ed660d609384caca80239245284be"
TAG:pn-update-web-server = "v1.00.0005"

# Update Server
BRANCH:pn-update-server = "main"
SRCREV:pn-update-server = "885a8760b011fde7e976a6698bde17c8e03b798d"
TAG:pn-update-server = "v1.00.0003"