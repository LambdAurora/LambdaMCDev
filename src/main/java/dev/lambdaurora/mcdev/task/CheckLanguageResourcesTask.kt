/*
 * Copyright 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.task

import com.google.gson.JsonParser
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.FileChange
import org.gradle.work.InputChanges
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import java.util.stream.StreamSupport

/**
 * Represents a task which checks that the language resource files are valid.
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public abstract class CheckLanguageResourcesTask : DefaultTask() {
	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	@get:SkipWhenEmpty
	@get:IgnoreEmptyDirectories
	public abstract val files: ConfigurableFileCollection

	@get:OutputFile
	public abstract val reportFile: RegularFileProperty

	init {
		this.group = "verification"
	}

	@TaskAction
	public fun execute(inputChanges: InputChanges) {
		val files = StreamSupport.stream(
			inputChanges.getFileChanges(this.files).spliterator(),
			false
		)
			.filter { action -> action.changeType != ChangeType.REMOVED }
			.map(FileChange::getFile)
			.map { it.toPath() }
			.filter { it.fileName.toString().endsWith(".json") }
			.toList()

		val errors = files
			.parallelStream()
			.map { testFile(it) }
			.flatMap { it.stream() }
			.toList()

		if (errors.isEmpty()) {
			val message = String.format("All language resource checks passed (%d files).", files.size)
			this.logger.lifecycle(message)
			this.writeReportFile(message + "\n")
		} else {
			val builder = StringBuilder()

			for (failedCheck in errors) {
				logger.error(" - {} - language resource checks have failed.", failedCheck.path)
				builder.append(String.format("- %s - language resource checks have failed.\n", failedCheck.path))
				logger.error("    -> ({}): {}", failedCheck.error.javaClass.name, failedCheck.error.message)
				builder.append(
					String.format("\t-> (%s): %s\n", failedCheck.error.javaClass.name, failedCheck.error.message)
				)
			}

			this.writeReportFile(
				String.format(
					"Language resource checks have failed on %d out of %d files.\n\n%s",
					errors.size, files.size,
					builder
				)
			)

			throw GradleException(
				String.format(
					"Language resource checks have failed on %d out of %d files.",
					errors.size, files.size,
				)
			)
		}
	}

	private fun testFile(path: Path): Optional<Error> {
		try {
			val content = Files.readString(path, StandardCharsets.UTF_8)
			JsonParser.parseString(content)
		} catch (e: Exception) {
			return Optional.of(Error(path, e))
		}

		return Optional.empty()
	}

	private fun writeReportFile(content: String) {
		val reportFilePath: Path = this.reportFile.get().asFile.toPath()
		try {
			Files.createDirectories(reportFilePath.parent)
			Files.writeString(reportFilePath, content)
		} catch (e: IOException) {
			this.logger.error("Failed to create report file.", e)
		}
	}

	private data class Error(val path: Path, val error: Exception)

	public companion object {
		/**
		 * Configures a check task with default values.
		 *
		 * @param project the project
		 * @param sourceSet the source set of the files to check
		 * @param sourceSetName the name of the source set
		 * @return the configuration action
		 */
		@JvmStatic
		public fun configureDefault(
			project: Project,
			sourceSet: SourceDirectorySet,
			sourceSetName: String?
		): Action<CheckLanguageResourcesTask> {
			return { task ->
				task.setDescription(
					("Checks whether language resource files in the " + sourceSet.getName() + " source set are valid.")
				)
				val buildPath = project.getLayout().getBuildDirectory().get().getAsFile().toPath()
				task.files.from(
					sourceSet
						.filter { file -> !file.toPath().startsWith(buildPath) }
						.filter { file -> file.toString().matches(".*[/\\\\]assets[/\\\\].+?[/\\\\]lang[/\\\\][a-z]+_[a-z]+\\.json$".toRegex()) }
						.getFiles()
				);
				task.reportFile.fileValue(
					project.getLayout().getBuildDirectory().get().getAsFile().toPath()
						.resolve("lambdamcdev/languages/check_report_" + sourceSetName + ".txt")
						.toFile()
				)
			}
		}
	}
}