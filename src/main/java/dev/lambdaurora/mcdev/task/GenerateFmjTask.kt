/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.task

import com.google.gson.FormattingStyle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.lambdaurora.mcdev.api.manifest.Fmj
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

public abstract class GenerateFmjTask @Inject constructor() : DefaultTask() {
	@get:Input
	public abstract val fmj: Property<Fmj>

	@get:OutputDirectory
	public abstract val outputDir: DirectoryProperty

	init {
		this.group = "generation"
	}

	@TaskAction
	@Throws(IOException::class)
	public fun generateManifest() {
		val output: Path = this.outputDir.asFile.get().toPath().resolve("fabric.mod.json")

		if (Files.exists(output)) {
			Files.delete(output)
		}

		Files.writeString(output, GSON.toJson(this.fmj.get()))
	}

	public companion object {
		private val GSON: Gson = GsonBuilder()
			.registerTypeAdapter(Fmj::class.java, Fmj.Serializer())
			.setFormattingStyle(FormattingStyle.PRETTY.withIndent("\t"))
			.create()

		public const val TASK_NAME: String = "generateFmj"
	}
}
