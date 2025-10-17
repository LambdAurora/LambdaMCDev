/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.task

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Represents a task which checks that the ref name used for the Git tag is accurate to the version that's being published.
 *
 * @version 1.8.0
 * @since 1.8.0
 */
public abstract class CheckActionsRefTask : DefaultTask() {
	@get:Input
	public abstract val version: Property<String>

	init {
		this.group = "verification"
		this.enabled = (System.getenv("GITHUB_ACTIONS") ?: "") == "true"

		this.version.convention(this.project.version.toString())
	}

	@TaskAction
	public fun execute() {
		val refName = System.getenv("ACTIONS_REF")

		if (refName != null && refName != "refs/tags/v${this.version.get()}") {
			throw GradleException("Failed to validate ref name.")
		}
	}
}