/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.task.packaging

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.lambdaurora.mcdev.api.ModVersionDependency
import dev.lambdaurora.mcdev.api.ModUtils
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.nio.file.FileSystem
import java.nio.file.Files
import javax.inject.Inject

public abstract class PackageModrinthTask @Inject constructor() : AbstractPackageTask() {
	@get:Input
	public abstract val versionName: Property<String>

	@get:Input
	public abstract val gameVersions: ListProperty<String>

	@get:Input
	public abstract val loaders: ListProperty<String>

	@get:Input
	public abstract val dependencies: ListProperty<ModVersionDependency>

	@get:Input
	public abstract val changelog: Property<String>

	@get:Input
	public abstract val readme: Property<String>

	init {
		val version = this.project.version.toString()
		this.changelog.convention(ModUtils.fetchChangelog(this.project, version))
		this.zipOut.convention(this.project.layout.buildDirectory.map { it -> it.file("modrinth.zip") })
	}

	override fun appendToZip(fs: FileSystem) {
		Files.writeString(fs.getPath("README.md"), this.readme.get())
	}

	override fun appendToManifest(json: JsonObject) {
		json.addProperty("name", this.versionName.get())
		json.addProperty("changelog", this.changelog.get())

		val gameVersions = JsonArray()
		this.gameVersions.get().forEach { gameVersions.add(it) }
		json.add("game_versions", gameVersions)

		val loaders = JsonArray()
		this.loaders.get().forEach { loaders.add(it) }
		json.add("loaders", loaders)

		val dependencies = JsonArray()
		this.dependencies.get().forEach { dependencies.add(it.toJson()) }
		json.add("dependencies", dependencies)
	}
}