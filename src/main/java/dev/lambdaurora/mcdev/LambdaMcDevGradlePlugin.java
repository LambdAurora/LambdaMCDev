/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class LambdaMcDevGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(@NotNull Project project) {
		// Apply loom.
		project.getPlugins().apply("fabric-loom");

		var loomExt = project.getExtensions().getByName("loom");
		project.getExtensions().create("lambdamcdev", LambdaMcDevGradleExtension.class, loomExt);
	}
}
