/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.eclipse.dataflow.core.project;

import com.google.cloud.tools.eclipse.util.MavenCoordinatesValidator;
import org.junit.Assert;
import org.junit.Test;

public class DataflowArchetypeTest {

  @Test
  public void testTemplates() {
    for (DataflowProjectArchetype template : DataflowProjectArchetype.values()) {
      Assert.assertTrue(MavenCoordinatesValidator.validateArtifactId(template.getArtifactId()));
      Assert.assertFalse(template.getSdkVersions().isEmpty());
      Assert.assertFalse(template.getLabel().isEmpty());
    }
  }

  // The Dataflow wizard assumes the last SDK version in the ordered set is the latest version.
  @Test
  public void testTemplateSdkVersions_inIncreasingMajorVersionOrder() {
    for (DataflowProjectArchetype template : DataflowProjectArchetype.values()) {
      MajorVersion previousVersion = null;
      for (MajorVersion majorVersion : template.getSdkVersions()) {
        if (previousVersion != null) {
          int compared = previousVersion.getMaxVersion().compareTo(majorVersion.getMaxVersion());
          Assert.assertTrue(compared < 0);
        }
      }
    }
  }
}
