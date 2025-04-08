/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.task

import dev.lambdaurora.mcdev.api.AccessWidenerToTransformer
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

public abstract class ConvertAccessWidenerToTransformer @Inject constructor() : DefaultTask() {
	@get:InputFile
	public abstract val input: RegularFileProperty

	@get:OutputFile
	public abstract val output: RegularFileProperty

	@TaskAction
	@Throws(IOException::class)
	public fun convert() {
		val inputPath: Path = this.input.asFile.get().toPath()
		val outputPath: Path = this.output.asFile.get().toPath()

		if (Files.exists(outputPath)) {
			Files.delete(outputPath)
		}

		if (inputPath.toString().endsWith(".jar")) {
			// This is a JAR file.
			FileSystems.newFileSystem(inputPath).use { fs ->
				val jarCandidates = fs.rootDirectories.flatMap { root ->
					var candidates: List<Path> = emptyList()

					Files.list(root).use { files ->
						candidates = files.filter { path -> path.toString().endsWith(".accesswidener") }
							.toList()
					}

					candidates
				}

				if (jarCandidates.size != 1) {
					throw IllegalStateException("Input JAR has more than one access widener.")
				}

				AccessWidenerToTransformer.convert(jarCandidates[0], outputPath)
			}

			return
		}

		AccessWidenerToTransformer.convert(inputPath, outputPath)
	}
}
