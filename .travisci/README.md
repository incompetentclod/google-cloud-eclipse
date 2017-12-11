# Continuous Integration Support for Travis

This directory contains files and helpers for Travis-CI.

## Toolchains

The [`toolchains.xml`](toolchains.xml) file contains Maven toolchains descriptions
for the installations found on the Travis build images.  They may need to be updated
as changes are made to the Travis images.

## Uploading Build Reports to GCS

The .travis.yml is configured to upload build reports and test
artifacts, like SWTBot `screenshots/` and `surefire-reports/`, to
a GCS bucket.

### Configuring Auto-Deletion for the GCS Bucket

`gcloud-eclipse-testing.lifecycle.json` provides an auto-deletion configuration
policy suitable for a GCS bucket hosting the build reports.  It should be installed
with:

```
$ gsutil lifecycle set .travisci/gcloud-eclipse-testing.lifecycle.json gs://gcloud-eclipse-testing
```

