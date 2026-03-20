/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev;

import dev.lambdaurora.mcdev.api.LambdaMcDevGradleExtension;
import dev.lambdaurora.mcdev.ext.LambdaMcDevGradleExtensionImpl;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.reflect.TypeOf;
import org.jetbrains.annotations.NotNull;

public class LambdaMcDevGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(@NotNull Project project) {
		var javaExt = project.getExtensions().getByType(JavaPluginExtension.class);
		var loomExt = project.getExtensions().findByName("loom");
		project.getExtensions().create(
				TypeOf.typeOf(LambdaMcDevGradleExtension.class),
				"lambdamcdev",
				LambdaMcDevGradleExtensionImpl.class,
				project,
				javaExt,
				loomExt
		);
	}
}
