/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

import dev.lambdaurora.mcdev.api.mappings.LambdaLayeredMappingsSpecBuilder;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

public interface LambdaMcDevGradleExtension {
	/**
	 * {@return the mod namespace associated with this project}
	 */
	@NotNull Property<String> getNamespace();

	/**
	 * Sets the mod namespace associated with this project.
	 *
	 * @param namespace the mod namespace
	 */
	default void setNamespace(@NotNull String namespace) {
		this.getNamespace().set(namespace);
	}

	@NotNull ModManifests getManifests();

	void manifests(@NotNull Action<ModManifests> action);

	void setupJarJarCompat();

	@NotNull MojmapRemapping setupMojmapRemapping();

	default void replaceArtifactInConfiguration(String configuration, Object artifact) {
		this.replaceArtifactInConfiguration(configuration, artifact, ignored -> {});
	}

	void replaceArtifactInConfiguration(String configuration, Object artifact, Action<? super ConfigurablePublishArtifact> configureAction);

	@NotNull Dependency layered(@NotNull Action<LambdaLayeredMappingsSpecBuilder> action);

	void setupActionsRefCheck();
}
