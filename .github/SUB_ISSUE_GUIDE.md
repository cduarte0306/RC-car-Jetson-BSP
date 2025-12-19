# Sub-Issue Creation for Issue #36

## Summary
This PR adds GitHub issue template infrastructure to enable easy creation of sub-issues for issue #36 "Add IMX219 camera driver". Specifically, it provides a pre-filled template for creating a sub-issue to add a new partition for camera data storage.

## What Was Created

### 1. Generic Sub-Issue Template
**File:** `.github/ISSUE_TEMPLATE/sub-issue.md`
- A reusable template for creating any sub-issue
- Includes fields for parent issue reference, description, acceptance criteria, and related components

### 2. Partition Addition Sub-Issue Template
**File:** `.github/ISSUE_TEMPLATE/add-partition-sub-issue.md`
- Pre-filled template specifically for creating a sub-issue to add a camera data partition
- Already references issue #36 as the parent
- Includes detailed acceptance criteria:
  - Update WIC partition layout
  - Configure sw-description files for OTA updates
  - Define partition specifications
  - Set up automatic mounting
  - Update documentation
  - Testing requirements
- Proposed partition specifications:
  - **Name:** camera-data
  - **Label:** camera  
  - **Size:** 1 GB
  - **Filesystem:** ext4
  - **Mount point:** /mnt/camera
  - **Alignment:** 4 MB

### 3. Documentation
**File:** `.github/README.md`
- Instructions on how to use the issue templates
- Workflow for creating and managing sub-issues

### 4. Template Configuration
**File:** `.github/ISSUE_TEMPLATE/config.yml`
- Configures the GitHub issue template chooser

## How to Use

### To Create the Partition Sub-Issue:
1. Go to https://github.com/cduarte0306/RC-car-Jetson-BSP/issues/new/choose
2. Select "Add New Partition Sub-Issue" from the available templates
3. Review and adjust the pre-filled content if needed
4. Click "Submit new issue"

The template will create an issue with:
- Title: `[SUB-36] Add new partition for camera data storage`
- Labels: `sub-issue`, `enhancement`
- All the details needed to implement the partition

### Files That Will Need to Be Modified:
When implementing the partition addition, these files will be affected:
- `layers/meta-rc-car/wic/rc-car-wic.wks` - Add new partition line
- `layers/meta-rc-car/recipes-core/images/files/sw-description` - Update for slot A and B
- `layers/meta-rc-car/dynamic-layers/meta-swupdate/recipes-demo/images/swupdate-image-tegra/sw-description` - Update for slot A and B

## Benefits
- Provides clear, structured approach to breaking down issue #36
- Pre-filled with all necessary technical details
- Ensures consistency in sub-issue creation
- Makes it easy to track and manage the partition addition work separately

## Next Steps
After creating the sub-issue from the template, the implementation work can begin following the acceptance criteria outlined in the template.
