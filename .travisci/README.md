# Continuous Integration Support for Travis

This directory contains files and helpers for Travis-CI.

## Toolchains

The [`toolchains.xml`](toolchains.xml) file contains Maven toolchains descriptions
for the installations found on the Travis build images.  They may need to be updated
as changes are made to the Travis images.

## Uploading Build Reports to GCS

The .travis.yml is configured to upload build reports and test
artifacts, like SWTBot `screenshots/` and `surefire-reports/`, to
a GCS bucket.  The bucket can be accessed at:

   https://console.cloud.google.com/storage/browser/BUCKET

### Configuring Public Access

[source](https://cloud.google.com/storage/docs/access-control/making-data-public#buckets)

```
$ gsutil iam ch allUsers:objectViewer gs://BUCKET
```

### Configuring Auto-Deletion for the GCS Bucket

`gcs.lifecycle.json` provides an [auto-deletion configuration
policy](https://cloud.google.com/storage/docs/managing-lifecycles#delete_an_object)
suitable for a GCS bucket hosting the build reports.  It should be
installed with:

```
$ gsutil lifecycle set .travisci/gcs.lifecycle.json gs://BUCKET
```

