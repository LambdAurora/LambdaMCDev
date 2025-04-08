/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.task

import dev.lambdaurora.mcdev.api.manifest.Nmt
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.nio.file.Files
import javax.inject.Inject

public abstract class GenerateNmtTask @Inject constructor() : DefaultTask() {
	@get:Input
	public abstract val nmt: Property<Nmt>

	@get:OutputDirectory
	public abstract val outputDir: DirectoryProperty

	init {
		this.group = "generation"
	}

	@TaskAction
	@Throws(IOException::class)
	public fun generateManifest() {
		val metaInfDir = this.outputDir.asFile.get().toPath().resolve("META-INF")
		val output = metaInfDir.resolve("neoforge.mods.toml")

		Files.createDirectories(metaInfDir)
		if (Files.exists(output)) {
			Files.delete(output)
		}

		Files.writeString(output, this.nmt.get().toToml())
	}

	public companion object {
		public const val TASK_NAME: String = "generateNmt"
	}
}
