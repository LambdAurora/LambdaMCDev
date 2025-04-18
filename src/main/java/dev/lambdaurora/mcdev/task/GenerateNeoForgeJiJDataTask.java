/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.lambdaurora.mcdev.util.JsonUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedVariantResult;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GenerateNeoForgeJiJDataTask extends DefaultTask {
	@Input
	protected abstract MapProperty<String, Metadata> getJarIds();

	@OutputFile
	public abstract RegularFileProperty getOutputFile();

	public GenerateNeoForgeJiJDataTask() {
		this.setGroup("generation");
	}

	@TaskAction
	public void generate() throws IOException {
		var json = new JsonObject();
		var jars = new JsonArray();
		json.add("jars", jars);

		this.getJarIds().get().forEach((path, metadata) -> {
			var jar = new JsonObject();

			{
				var identifier = new JsonObject();
				identifier.addProperty("group", metadata.group);
				identifier.addProperty("artifact", metadata.name);
				jar.add("identifier", identifier);
			}

			{
				var version = new JsonObject();
				version.addProperty("range", "[%s,)".formatted(metadata.version));
				version.addProperty("artifactVersion", metadata.version);
				jar.add("version", version);
			}

			jar.addProperty("path", "META-INF/jars/" + path);
			jar.addProperty("isObfuscated", false);

			jars.add(jar);
		});

		var output = this.getOutputFile().getAsFile().get().toPath();

		Files.writeString(output, JsonUtils.GSON.toJson(json));
	}

	public void from(Configuration configuration) {
		this.dependsOn(configuration);

		ArtifactView artifacts = configuration.getIncoming().artifactView(config -> {
			config.attributes(
					attr -> attr.attribute(
							ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE,
							ArtifactTypeDefinition.JAR_TYPE
					)
			);
		});

		this.getJarIds().set(artifacts.getArtifacts().getResolvedArtifacts().map(set -> {
			Map<String, Metadata> map = new HashMap<>();
			set.forEach(artifact -> {
				ResolvedVariantResult variant = artifact.getVariant();

				ComponentIdentifier id = variant.getOwner();
				Metadata moduleLocation = null;

				if (id instanceof ModuleComponentIdentifier moduleIdentifier) {
					moduleLocation = new Metadata(
							moduleIdentifier.getGroup(),
							moduleIdentifier.getModule(),
							moduleIdentifier.getVersion(),
							null
					);
				}

				List<Metadata> capabilityLocations = variant.getCapabilities().stream()
						.map(capability -> new Metadata(capability.getGroup(), capability.getName(), capability.getVersion(), null))
						.toList();

				if (!capabilityLocations.isEmpty() && (moduleLocation == null || !capabilityLocations.contains(moduleLocation))) {
					moduleLocation = capabilityLocations.getFirst();
				}

				if (moduleLocation == null) {
					throw new RuntimeException("Attempted to nest artifact " + id + " which is not a module component and has no capabilities.");
				} else if (moduleLocation.version == null) {
					throw new RuntimeException("Attempted to nest artifact " + id + " which has no version");
				}

				String group = moduleLocation.group;
				String name = moduleLocation.name;
				String version = moduleLocation.version;
				String classifier = null;

				if (artifact.getFile().getName().startsWith(name + "-" + version + "-")) {
					String rest = artifact.getFile().getName().substring(name.length() + version.length() + 2);
					int dotIndex = rest.indexOf('.');

					if (dotIndex != -1) {
						classifier = rest.substring(0, dotIndex);
					}
				}

				var metadata = new Metadata(group, name, version, classifier);
				map.put(artifact.getFile().getName(), metadata);
			});
			return map;
		}));
	}

	public record Metadata(String group, String name, String version, @Nullable String classifier) implements Serializable {
		@Override
		public String classifier() {
			if (classifier == null) {
				return "";
			} else {
				return "_" + classifier;
			}
		}

		@Override
		public String toString() {
			return group + ":" + name + ":" + version + classifier();
		}
	}
}
