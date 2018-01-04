/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.eclipse.sdk.internal;

import com.google.cloud.tools.eclipse.util.status.StatusUtil;
import com.google.cloud.tools.managedcloudsdk.ManagedCloudSdk;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Wraps {@code CloudSdkInstallTask} in {@code Job} mainly to report install progress to users.
 */
public class CloudSdkInstallJob extends Job {

  private static final ExecutorService serialExecutor = Executors.newSingleThreadExecutor();

  private final ManagedCloudSdk managedSdk;

  public CloudSdkInstallJob(ManagedCloudSdk managedSdk) {
    super("Installing the Google Cloud SDK... (may take up to several minutes)");
    this.managedSdk = managedSdk;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    Future<Boolean> installTask = serialExecutor.submit(new CloudSdkInstallTask(managedSdk));

    try {
      while (true) {
        try {
          installTask.get(1, TimeUnit.SECONDS);
          return Status.OK_STATUS;
        } catch (TimeoutException e) {
          // Install still in progress. Check completion again.
        }
      }
    } catch (InterruptedException e) {
      return Status.CANCEL_STATUS;
    } catch (ExecutionException e) {
      return StatusUtil.error(this, e.getLocalizedMessage(), e);
    }
  }
}
