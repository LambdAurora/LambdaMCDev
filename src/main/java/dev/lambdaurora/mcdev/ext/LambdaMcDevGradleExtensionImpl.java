/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.ext;

import dev.lambdaurora.mcdev.api.LambdaMcDevGradleExtension;
import dev.lambdaurora.mcdev.api.MappingVariant;
import dev.lambdaurora.mcdev.api.ModManifests;
import dev.lambdaurora.mcdev.api.MojmapRemapping;
import dev.lambdaurora.mcdev.api.manifest.Fmj;
import dev.lambdaurora.mcdev.api.manifest.Nmt;
import dev.lambdaurora.mcdev.api.mappings.LambdaLayeredMappingsSpecBuilder;
import dev.lambdaurora.mcdev.mappings.LambdaLayeredMappingsSpecBuilderImpl;
import dev.lambdaurora.mcdev.task.CheckActionsRefTask;
import dev.lambdaurora.mcdev.task.GenerateFmjTask;
import dev.lambdaurora.mcdev.task.GenerateNeoForgeJiJDataTask;
import dev.lambdaurora.mcdev.task.GenerateNmtTask;
import dev.lambdaurora.mcdev.util.Utils;
import dev.yumi.commons.function.YumiPredicates;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.build.nesting.NestableJarGenerationTask;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.attributes.*;
import org.gradle.api.attributes.java.TargetJvmVersion;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

public class LambdaMcDevGradleExtensionImpl implements LambdaMcDevGradleExtension {
	final Project project;
	private final Property<String> namespace;
	private final JavaPluginExtension java;
	final LoomGradleExtensionAPI loom;
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
	public void setupJarJarCompat() {
		var includeConfig = this.project.getConfigurations().getByName("includeInternal");

		var generateJarJarMetadata = this.project.getTasks()
				.register("generateJarJarMetadata", GenerateNeoForgeJiJDataTask.class, task -> {
					task.from(includeConfig);
					task.getOutputFile().set(
							this.project.getLayout().getBuildDirectory()
									.getAsFile()
									.map(File::toPath)
									.map(path -> path.resolve("generated/jarjar/metadata.json"))
									.map(Path::toFile)
									.get()
					);
				});

		this.project.getTasks().named("processResources", ProcessResources.class).configure(task -> {
			task.dependsOn(generateJarJarMetadata);
			task.from(generateJarJarMetadata, copySpec -> {
				copySpec.into("META-INF/jarjar/");
			});
		});
	}

	@Override
	public @NotNull MojmapRemapping setupMojmapRemapping() {
		final var configurations = this.project.getConfigurations();
		final var tasks = this.project.getTasks();
		final var objects = this.project.getObjects();
		final boolean hasSources = tasks.findByPath("remapSourcesJar") != null;

		var mojmapConfig = configurations.create("mojangMappings");
		this.project.getDependencies().add("mojangMappings", this.loom.officialMojangMappings());

		var published = this.java.getSourceSets().create("mojmap");

		configurations.getByName("apiElements").attributes(this.setMappingsAttribute(MappingVariant.INTERMEDIARY));
		configurations.getByName("runtimeElements").attributes(this.setMappingsAttribute(MappingVariant.INTERMEDIARY));

		if (hasSources) {
			configurations.getByName("sourcesElements").attributes(this.setMappingsAttribute(MappingVariant.INTERMEDIARY));
		}

		final var apiConfig = configurations.create(published.getApiConfigurationName(), config -> {
			config.attributes(this.setMappingsAttribute(MappingVariant.MOJMAP));
		});

		final var apiElements = configurations.create(published.getApiElementsConfigurationName(), config -> {
			config.extendsFrom(apiConfig);
			config.attributes(attributes -> {
				this.setMappingsAttribute(attributes, MappingVariant.MOJMAP);
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.LIBRARY));
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.JAR));
				attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_API));
				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
				var languageVersion = this.java.getToolchain().getLanguageVersion();
				if (languageVersion.isPresent()) {
					attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, languageVersion.get().asInt());
				}
			});
		});
		final var runtimeElements = configurations.create(published.getRuntimeElementsConfigurationName(), config -> {
			config.extendsFrom(apiConfig, configurations.getByName(published.getImplementationConfigurationName()));
			config.attributes(attributes -> {
				this.setMappingsAttribute(attributes, MappingVariant.MOJMAP);
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.LIBRARY));
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.JAR));
				attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));
				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
				var languageVersion = this.java.getToolchain().getLanguageVersion();
				if (languageVersion.isPresent()) {
					attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, languageVersion.get().asInt());
				}
			});
		});
		final var sourcesElements = configurations.create(published.getSourcesElementsConfigurationName(), config -> {
			config.attributes(attributes -> {
				this.setMappingsAttribute(attributes, MappingVariant.MOJMAP);
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, DocsType.SOURCES));
				attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));
			});
		});

		final var include = configurations.register(published.getName() + "Include", config -> {
			config.setCanBeConsumed(false);
			config.setCanBeResolved(false);
		});
		final var includeInternal = configurations.register(published.getName() + "IncludeInternal", config -> {
			config.getDependencies().addAllLater(this.project.provider(() -> {
				var dependencies = new ArrayList<Dependency>();

				for (Dependency dependency : include.get().getIncoming().getDependencies()) {
					if (dependency instanceof HasConfigurableAttributes<?> hasAttributes) {
						Category category = hasAttributes.getAttributes().getAttribute(Category.CATEGORY_ATTRIBUTE);

						if (category != null && (category.getName().equals(Category.ENFORCED_PLATFORM) || category.getName().equals(Category.REGULAR_PLATFORM))) {
							dependencies.add(dependency);
							continue;
						} else if (dependency instanceof ModuleDependency moduleDependency) {
							var copy = moduleDependency.copy();
							copy.setTransitive(false);
							dependencies.add(copy);
							continue;
						}
					}

					dependencies.add(dependency);
				}

				return dependencies;
			}));
			config.attributes(attributes -> {
				this.setMappingsAttribute(attributes, MappingVariant.MOJMAP);
				attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.JAR));
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.LIBRARY));
				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
			});
		});

		var processIncludeJars = tasks.register("process" + Utils.capitalize(published.getName()) + "IncludeJars", NestableJarGenerationTask.class, task -> {
			task.from(includeInternal.get());
			task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir(task.getName()));
		});

		final var javaComponent = ((AdhocComponentWithVariants) this.project.getComponents().getByName("java"));
		javaComponent.addVariantsFromConfiguration(apiElements, details -> {
			details.mapToMavenScope("compile");
			details.mapToOptional();
		});
		javaComponent.addVariantsFromConfiguration(runtimeElements, details -> {
			details.mapToMavenScope("runtime");
			details.mapToOptional();
		});
		javaComponent.addVariantsFromConfiguration(sourcesElements, ConfigurationVariantDetails::mapToOptional);

		return new MojmapRemappingImpl(this, published, mojmapConfig, processIncludeJars);
	}

	private Action<AttributeContainer> setMappingsAttribute(String variant) {
		return attributes -> this.setMappingsAttribute(attributes, variant);
	}

	private void setMappingsAttribute(AttributeContainer attributes, String variant) {
		attributes.attribute(
				MappingVariant.ATTRIBUTE,
				this.project.getObjects().named(MappingVariant.class, variant)
		);
	}

	@Override
	public void replaceArtifactInConfiguration(
			String configuration, Object artifact,
			Action<? super ConfigurablePublishArtifact> configureAction
	) {
		project.getConfigurations().getByName(configuration).getArtifacts().removeIf(YumiPredicates.alwaysTrue());
		project.getArtifacts().add(configuration, artifact, configureAction);
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

	@Override
	public void setupActionsRefCheck() {
		if (this.project.getRootProject() != this.project) return; // Do nothing if not the root project.

		final var checkActionsTask = this.project.getTasks().register("checkActions", CheckActionsRefTask.class);
		this.project.getTasks().named("check").configure(checkTask -> {
			checkTask.dependsOn(checkActionsTask);
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
