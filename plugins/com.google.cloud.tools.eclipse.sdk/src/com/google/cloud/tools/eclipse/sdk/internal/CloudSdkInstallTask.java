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

import com.google.cloud.tools.managedcloudsdk.ManagedCloudSdk;
import com.google.cloud.tools.managedcloudsdk.ManagedSdkVerificationException;
import com.google.cloud.tools.managedcloudsdk.ManagedSdkVersionMismatchException;
import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.UnsupportedOsException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponent;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponentInstaller;
import com.google.cloud.tools.managedcloudsdk.install.SdkInstaller;
import com.google.cloud.tools.managedcloudsdk.install.SdkInstallerException;
import com.google.cloud.tools.managedcloudsdk.install.UnknownArchiveTypeException;
import com.google.cloud.tools.managedcloudsdk.update.SdkUpdater;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation is not thread-safe. {@code #run()} should not be executing concurrently.
 */
public class CloudSdkInstallTask implements Runnable {

  private static final Logger logger = Logger.getLogger(CloudSdkInstallTask.class.getName());

  private final ManagedCloudSdk managedSdk;

  public CloudSdkInstallTask(ManagedCloudSdk managedSdk) {
    this.managedSdk = managedSdk;
  }

  @Override
  public void run() {
    System.out.println("Install task started.");
    try {
      if (!managedSdk.isInstalled()) {
        System.out.println("Installing the SDK.");
        SdkInstaller installer = managedSdk.newInstaller();
        installer.install(new NoOpMessageListener());
        System.out.println("SDK installation complete.");
      }

      if (!managedSdk.hasComponent(SdkComponent.APP_ENGINE_JAVA)) {
        System.out.println("Installing the App Engine Java component.");
        SdkComponentInstaller componentInstaller = managedSdk.newComponentInstaller();
        componentInstaller.installComponent(
            SdkComponent.APP_ENGINE_JAVA, new NoOpMessageListener());
        System.out.println("Component installation complete.");
      }

      if (!managedSdk.isUpToDate()) {
        System.out.println("Updating the SDK.");
        SdkUpdater updater = managedSdk.newUpdater();
        updater.update(new NoOpMessageListener());
        System.out.println("Update complete.");
      }

    } catch (InterruptedException | IOException | SdkInstallerException | CommandExecutionException
        | CommandExitException | ManagedSdkVerificationException e) {
      logger.log(Level.SEVERE, "Failed to install Cloud SDK automatically.", e);
    } catch (UnsupportedOsException e) {
      throw new RuntimeException("Cloud Tools for Eclipse supports Windows, Linux, and Mac only.");
    } catch (ManagedSdkVersionMismatchException e) {
      throw new RuntimeException("This is never thrown because we always use LATEST.");
    } catch (UnknownArchiveTypeException e) {
      throw new RuntimeException("Never thrown.");
    }
    System.out.println("Install task finished.");
  }

  private static class NoOpMessageListener implements MessageListener {
    @Override
    public void message(String rawString) {
      // Do nothing.
    }

  }
}
