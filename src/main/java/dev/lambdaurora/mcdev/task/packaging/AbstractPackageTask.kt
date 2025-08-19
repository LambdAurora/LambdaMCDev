/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.task.packaging

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.lambdaurora.mcdev.api.VersionType
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import javax.inject.Inject

public abstract class AbstractPackageTask @Inject constructor() : DefaultTask() {
	@get:Input
	public abstract val version: Property<String>

	@get:Input
	public abstract val versionType: Property<VersionType>

	@get:InputFiles
	public abstract val files: ConfigurableFileCollection

	@get:OutputFile
	public abstract val zipOut: RegularFileProperty

	init {
		val version = this.project.version.toString()

		this.version.convention(version)
		this.versionType.convention(VersionType.RELEASE)
	}

	@TaskAction
	public fun run() {
		val zipPath = this.zipOut.get().asFile.toPath()
		Files.deleteIfExists(zipPath)

		val jarPaths = this.files.files.stream().map { it.toPath() }.toList()

		val json = JsonObject()
		json.addProperty("version", this.version.get())
		json.addProperty("type", this.versionType.get().toString())

		this.appendToManifest(json)

		val files = JsonArray()
		jarPaths.forEach { jarPath ->
			files.add(jarPath.fileName.toString())
		}
		json.add("files", files)

		FileSystems.newFileSystem(
			this.zipOut.get().asFile.toPath(),
			mapOf(Pair("create", "true"))
		).use { fs ->
			Files.writeString(fs.getPath("manifest.json"), GSON.toJson(json))
			jarPaths.forEach { jarPath ->
				Files.copy(jarPath, fs.getPath(jarPath.fileName.toString()))
			}

			this.appendToZip(fs)
		}
	}

	protected open fun appendToZip(fs: FileSystem) {}

	protected open fun appendToManifest(json: JsonObject) {}

	private companion object {
		private val GSON = GsonBuilder().setPrettyPrinting().create()
	}
}