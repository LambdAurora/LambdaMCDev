/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.ext;

import dev.lambdaurora.mcdev.api.LambdaMcDevGradleExtension;
import dev.lambdaurora.mcdev.api.ModManifests;
import dev.lambdaurora.mcdev.api.manifest.Fmj;
import dev.lambdaurora.mcdev.api.manifest.Nmt;
import dev.lambdaurora.mcdev.api.mappings.LambdaLayeredMappingsSpecBuilder;
import dev.lambdaurora.mcdev.mappings.LambdaLayeredMappingsSpecBuilderImpl;
import dev.lambdaurora.mcdev.task.GenerateFmjTask;
import dev.lambdaurora.mcdev.task.GenerateNmtTask;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class LambdaMcDevGradleExtensionImpl implements LambdaMcDevGradleExtension {
	private final Project project;
	private final Property<String> namespace;
	private final JavaPluginExtension java;
	private final LoomGradleExtensionAPI loom;
	private final Manifests manifests;

	public LambdaMcDevGradleExtensionImpl(
			Project project,
			JavaPluginExtension java, LoomGradleExtensionAPI loom
	) {
		this.project = project;
		this.namespace = project.getObjects().property(String.class);
		this.java = java;
		this.loom = loom;

		if (this.loom != null) {
			this.setupLoom();
		}

		this.manifests = new Manifests();
	}

	/**
	 * Setups loom stuff.
	 */
	private void setupLoom() {
		this.loom.getRuntimeOnlyLog4j().convention(true);
	}

	@Override
	public @NotNull Property<String> getNamespace() {
		return this.namespace;
	}

	@Override
	public @NotNull ModManifests getManifests() {
		return this.manifests;
	}

	@Override
	public void manifests(@NotNull Action<ModManifests> action) {
		action.execute(this.manifests);
	}

	@Override
	public @NotNull Dependency layered(@NotNull Action<LambdaLayeredMappingsSpecBuilder> action) {
		if (this.loom == null) {
			throw new UnsupportedOperationException("No loom extension available.");
		}

		return this.loom.layered(builder -> {
			var lambdaBuilder = new LambdaLayeredMappingsSpecBuilderImpl(builder);
			action.execute(lambdaBuilder);
		});
	}

	public class Manifests implements ModManifests {
		private final File generatedDir = project.file("build/generated/generated_resources/");
		private final Property<Fmj> fmj
				= project.getObjects().property(Fmj.class);
		private final Property<Nmt> nmt
				= project.getObjects().property(Nmt.class);
		private boolean isSourceSetSetup = false;

		@Override
		public @NotNull Provider<Fmj> fmj() {
			return this.fmj;
		}

		@Override
		public @NotNull Fmj fmj(@NotNull Fmj fmj) {
			return this.setFmj(fmj);
		}

		@Override
		public @NotNull Fmj fmj(@NotNull Action<Fmj> action) {
			if (this.fmj.isPresent()) {
				var fmj = this.fmj.get();
				action.execute(fmj);
				return fmj;
			}

			var fmj = new Fmj(
					namespace.get(),
					project.getName(),
					project.getVersion().toString()
			);
			action.execute(fmj);
			return this.setFmj(fmj);
		}

		private @NotNull Fmj setFmj(@NotNull Fmj fmj) {
			boolean shouldSetup = !this.fmj.isPresent();

			this.fmj.set(fmj);
			if (shouldSetup) {
				this.setupFmj();
			}
			return fmj;
		}

		private void setupFmj() {
			var generateFmj = project.getTasks().register(
					GenerateFmjTask.TASK_NAME, GenerateFmjTask.class,
					task -> {
						task.getFmj().set(this.fmj.get());
						task.getOutputDir().set(this.generatedDir);
					}
			);
			this.setupTask(generateFmj);
		}

		@Override
		public @NotNull Provider<Nmt> nmt() {
			return this.nmt;
		}

		@Override
		public @NotNull Nmt nmt(@NotNull Nmt nmt) {
			return this.setNmt(nmt);
		}

		@Override
		public @NotNull Nmt nmt(@NotNull Action<Nmt> action) {
			if (this.nmt.isPresent()) {
				var nmt = this.nmt.get();
				action.execute(nmt);
				return nmt;
			}

			var nmt = new Nmt(
					namespace.get(),
					project.getName(),
					project.getVersion().toString()
			);
			action.execute(nmt);
			return this.setNmt(nmt);
		}

		private @NotNull Nmt setNmt(@NotNull Nmt nmt) {
			boolean shouldSetup = !this.nmt.isPresent();

			this.nmt.set(nmt);
			if (shouldSetup) {
				this.setupNmt();
			}
			return nmt;
		}

		private void setupNmt() {
			var generateNmt = project.getTasks().register(
					GenerateNmtTask.TASK_NAME, GenerateNmtTask.class,
					task -> {
						task.getNmt().set(this.nmt.get());
						task.getOutputDir().set(this.generatedDir);
					}
			);
			this.setupTask(generateNmt);
		}

		// This is needed so that people can use their IDE to compile the project (bypassing Gradle).
		private void setupTask(TaskProvider<?> task) {
			project.getTasks()
					.named("ideaSyncTask")
					.configure(syncTask -> syncTask.dependsOn(task));

			var sourcesJar = project.getTasks().findByName("sourcesJar");
			if (sourcesJar != null) {
				sourcesJar.dependsOn(task);
			}

			var processResources = project.getTasks().findByName("processResources");
			if (processResources != null) {
				processResources.dependsOn(task);
			}

			this.setupSourceSet();
		}

		private void setupSourceSet() {
			if (this.isSourceSetSetup) {
				return;
			}

			this.isSourceSetSetup = true;
			var sourceSets = java.getSourceSets();
			var main = sourceSets.getByName("main");
			main.resources(files -> {
				files.srcDir(this.generatedDir);
			});
		}
	}
}
