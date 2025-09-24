/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

import net.fabricmc.loom.task.RemapJarTask;
import net.fabricmc.loom.task.RemapSourcesJarTask;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

public interface MojmapRemapping {
	@NotNull SourceSet sourceSet();

	@NotNull Configuration mappingsConfiguration();

	@NotNull Provider<RemapJarTask> registerRemap(String taskName, Action<RemapJarTask> action);

	default @NotNull Provider<RemapJarTask> registerRemap(Jar artifact, Action<RemapJarTask> action) {
		return this.registerRemap("remapJarToMojmap", task -> {
			task.dependsOn(artifact);
			task.getInputFile().set(artifact.getArchiveFile());

			action.execute(task);
		});
	}

	default @NotNull Provider<RemapJarTask> registerRemap(Provider<? extends Jar> artifact, Action<RemapJarTask> action) {
		return this.registerRemap("remapJarToMojmap", task -> {
			task.dependsOn(artifact);
			task.getInputFile().set(artifact.flatMap(AbstractArchiveTask::getArchiveFile));

			action.execute(task);
		});
	}

	@NotNull Provider<RemapSourcesJarTask> registerSourcesRemap(String taskName, Action<RemapSourcesJarTask> action);

	default @NotNull Provider<RemapSourcesJarTask> registerSourcesRemap(Jar artifact, Action<RemapSourcesJarTask> action) {
		return this.registerSourcesRemap("remapSourcesJarToMojmap", task -> {
			task.dependsOn(artifact);
			task.getInputFile().set(artifact.getArchiveFile());

			action.execute(task);
		});
	}

	default @NotNull Provider<RemapSourcesJarTask> registerSourcesRemap(Provider<? extends Jar> artifact, Action<RemapSourcesJarTask> action) {
		return this.registerSourcesRemap("remapSourcesJarToMojmap", task -> {
			task.dependsOn(artifact);
			task.getInputFile().set(artifact.flatMap(AbstractArchiveTask::getArchiveFile));

			action.execute(task);
		});
	}

	void setJarArtifact(Object artifact);

	void setSourcesArtifact(Object artifact);
}
