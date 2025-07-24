/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.ext;

import dev.lambdaurora.mcdev.api.LambdaMcDevGradleExtension;
import dev.lambdaurora.mcdev.api.MojmapRemapping;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;

public record MojmapRemappingImpl(
		LambdaMcDevGradleExtension ext,
		SourceSet sourceSet,
		Configuration mappingsConfiguration
) implements MojmapRemapping {
	@Override
	public void setJarArtifact(Object artifact) {
		this.ext.replaceArtifactInConfiguration(
				this.sourceSet.getApiElementsConfigurationName(), artifact,
				a -> a.setClassifier("mojmap")
		);
		this.ext.replaceArtifactInConfiguration(
				this.sourceSet.getRuntimeElementsConfigurationName(), artifact,
				a -> a.setClassifier("mojmap")
		);
	}

	@Override
	public void setSourcesArtifact(Object artifact) {
		// Add the remapped sources artifact
		this.ext.replaceArtifactInConfiguration(
				this.sourceSet.getSourcesElementsConfigurationName(), artifact,
				a -> a.setClassifier("mojmap-sources")
		);
	}
}
