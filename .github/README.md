# GitHub Issue Templates

This directory contains issue templates for the RC-car-Jetson-BSP project.

## Available Templates

### 1. Sub-Issue Template (`sub-issue.md`)
A general template for creating sub-issues that are part of a larger parent issue.

**Usage:**
- When creating a new issue, select "Sub-Issue Template" from the issue templates
- Fill in the parent issue number
- Complete the description and acceptance criteria

### 2. Add New Partition Sub-Issue (`add-partition-sub-issue.md`)
A specific template for sub-issue #36 to add a new partition for camera data storage.

**Usage:**
- This template is pre-filled with details for adding a partition for the IMX219 camera driver
- Review and adjust the proposed partition specifications as needed
- Create the issue to track the partition addition work

## Creating Sub-Issues

To create a sub-issue:
1. Go to the Issues tab in the repository
2. Click "New Issue"
3. Select the appropriate template
4. Fill in any required information
5. Submit the issue
6. Link it to the parent issue by adding "Related to #[parent-issue-number]" in the description

## Sub-Issue Workflow

1. Sub-issues should reference their parent issue using "Parent Issue: #XX"
2. Add the `sub-issue` label to all sub-issues
3. When all sub-issues are complete, the parent issue can be closed
4. Use task lists in the parent issue to track sub-issue completion
