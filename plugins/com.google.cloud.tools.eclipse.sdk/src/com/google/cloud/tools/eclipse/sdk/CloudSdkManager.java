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

package com.google.cloud.tools.eclipse.sdk;

import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.managedcloudsdk.ManagedCloudSdk;
import com.google.cloud.tools.managedcloudsdk.ManagedSdkVerificationException;
import com.google.cloud.tools.managedcloudsdk.ManagedSdkVersionMismatchException;
import com.google.cloud.tools.managedcloudsdk.UnsupportedOsException;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponent;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudSdkManager {

  private static final ExecutorService serialExecutor = Executors.newSingleThreadExecutor();

  /**
   * Returns {@code CloudSdk} if there exists an up-to-date Cloud SDK with the App Engine Java
   * component installed at the managed location. Otherwise, returns {@code null} after triggering a
   * background installation task.
   *
   * Note that even if the Cloud SDK is already installed, this method executes {@code gcloud}
   * twice externally to check components and versions, which may incur a noticeable delay. If
   * possible, avoid calling the method repetitively.
   *
   * @return {@code CloudSdk} if there exists an up-to-date Cloud SDK with the App Engine Java
   *     component installed at the managed location; {@code null} otherwise
   * @throws ManagedSdkVerificationException if unable to verify SDK installation; possible causes
   *     include corrupted binaries or a problem executing binaries
   */
  public static CloudSdk getCloudSdk() throws ManagedSdkVerificationException {
    try {
      ManagedCloudSdk managedSdk = ManagedCloudSdk.newManagedSdk();
      if (managedSdk.isInstalled()
          && managedSdk.hasComponent(SdkComponent.APP_ENGINE_JAVA)
          && managedSdk.isUpToDate()) {
        System.out.println("Found the Cloud SDK.");
        Path sdkHome = managedSdk.getSdkHome();
        CloudSdk sdk = new CloudSdk.Builder().sdkPath(sdkHome).build();
        return sdk;
      }

      serialExecutor.submit(new CloudSdkInstallTask(managedSdk));
      return null;

    } catch (UnsupportedOsException e) {
      throw new RuntimeException("Cloud Tools for Eclipse supports Windows, Linux, and Mac only.");
    } catch (ManagedSdkVersionMismatchException e) {
      throw new RuntimeException("This is never thrown because we always use LATEST.");
    }
  }
}
