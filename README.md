# RC-car-Jetson-BSP
This project is to show-case the use of a Linux SBC to implement machine vision for the autonomous RC car.

# Set up environment
`source poky/oe-init-build-env build-jetson/`

# Build image
`bitbake rc-car-image`

# Build sdk
`bitbake rc-car-image -c populate_sdk`
 
