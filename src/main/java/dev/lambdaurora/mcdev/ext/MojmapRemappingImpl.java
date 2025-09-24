/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.ext;

import dev.lambdaurora.mcdev.api.MojmapRemapping;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.build.nesting.NestableJarGenerationTask;
import net.fabricmc.loom.task.RemapJarTask;
import net.fabricmc.loom.task.RemapSourcesJarTask;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;

public record MojmapRemappingImpl(
		LambdaMcDevGradleExtensionImpl ext,
		SourceSet sourceSet,
		Configuration mappingsConfiguration,
		TaskProvider<NestableJarGenerationTask> processIncludeJars
) implements MojmapRemapping {
	@SuppressWarnings("UnstableApiUsage")
	@Override
	public @NotNull Provider<RemapJarTask> registerRemap(String taskName, Action<RemapJarTask> action) {
		return this.ext.project.getTasks().register(taskName, RemapJarTask.class, task -> {
			task.setGroup("remapping");

			task.getCustomMappings().from(this.mappingsConfiguration());
			task.getSourceNamespace().set("intermediary");
			task.getTargetNamespace().set("named");
			task.getArchiveClassifier().set("mojmap");
			task.getClasspath().setFrom(
					((LoomGradleExtension) this.ext.loom).getMinecraftJars(MappingsNamespace.INTERMEDIARY),
					this.ext.project.getConfigurations().getByName("modCompileClasspath")
			);

			task.getNestedJars().setFrom(
					this.processIncludeJars.map(nestableGeneration ->
							this.ext.project.fileTree(nestableGeneration.getOutputDirectory())
					)
			);
			task.getNestedJars().builtBy(this.processIncludeJars);

			action.execute(task);
		});
	}

	@Override
	public @NotNull Provider<RemapSourcesJarTask> registerSourcesRemap(String taskName, Action<RemapSourcesJarTask> action) {
		return this.ext.project.getTasks().register(taskName, RemapSourcesJarTask.class, task -> {
			task.setGroup("remapping");

			task.getCustomMappings().from(this.mappingsConfiguration());
			task.getSourceNamespace().set("intermediary");
			task.getTargetNamespace().set("named");
			task.getArchiveClassifier().set("mojmap-sources");
			task.getClasspath().setFrom(
					((LoomGradleExtension) this.ext.loom).getMinecraftJars(MappingsNamespace.INTERMEDIARY),
					this.ext.project.getConfigurations().getByName("modCompileClasspath")
			);

			action.execute(task);
		});
	}

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
