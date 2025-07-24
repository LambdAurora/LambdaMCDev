/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;

public interface MojmapRemapping {
	SourceSet sourceSet();

	Configuration mappingsConfiguration();

	void setJarArtifact(Object artifact);

	void setSourcesArtifact(Object artifact);
}
