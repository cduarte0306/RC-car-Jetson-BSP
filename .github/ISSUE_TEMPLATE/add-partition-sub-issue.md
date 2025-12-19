---
name: Add New Partition Sub-Issue
about: Sub-issue template for adding a new partition to the BSP
title: '[SUB-36] Add new partition for camera data storage'
labels: ['sub-issue', 'enhancement']
assignees: ''
---

**Parent Issue:** #36 - Add IMX219 camera driver

## Description
Add a new partition to the RC-car BSP storage layout to support camera data storage for the IMX219 camera driver. This partition will be used to store captured images and video data from the camera.

## Acceptance Criteria
- [ ] Update WIC partition layout in `layers/meta-rc-car/wic/rc-car-wic.wks`
- [ ] Add partition configuration to sw-description files for OTA updates
- [ ] Define partition size and filesystem type
- [ ] Ensure partition is mounted automatically at boot
- [ ] Update documentation with new partition information
- [ ] Test partition creation during image build
- [ ] Verify partition persists across OTA updates

## Additional Context
The IMX219 camera driver (parent issue #36) will need dedicated storage space for:
- Captured images
- Video recordings
- Temporary camera buffer data
- Camera calibration data

Current partition layout from `rc-car-wic.wks`:
- Boot partition: 16 MB (vfat)
- Root filesystem: 2 GB (ext4)

## Related Files/Components
- `layers/meta-rc-car/wic/rc-car-wic.wks` - Main partition layout
- `layers/meta-rc-car/recipes-core/images/files/sw-description` - SWUpdate configuration
- `layers/meta-rc-car/dynamic-layers/meta-swupdate/recipes-demo/images/swupdate-image-tegra/sw-description` - SWUpdate Tegra configuration

## Proposed Partition Specifications
- **Name:** camera-data
- **Label:** camera
- **Size:** 1 GB (adjustable based on requirements)
- **Filesystem:** ext4
- **Mount point:** /var/camera or /mnt/camera
- **Alignment:** 4 MB (matching existing partitions)
