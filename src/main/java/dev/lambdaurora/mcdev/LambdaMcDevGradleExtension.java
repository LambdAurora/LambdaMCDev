/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev;

import dev.lambdaurora.mcdev.api.mappings.LambdaLayeredMappingsSpecBuilder;
import dev.lambdaurora.mcdev.mappings.LambdaLayeredMappingsSpecBuilderImpl;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;
import org.jetbrains.annotations.NotNull;

public class LambdaMcDevGradleExtension {
	private final LoomGradleExtensionAPI loom;

	public LambdaMcDevGradleExtension(LoomGradleExtensionAPI loom) {
		this.loom = loom;
		this.loom.getRuntimeOnlyLog4j().convention(true);
	}

	@SuppressWarnings("UnstableApiUsage")
	public Dependency layered(@NotNull Action<LambdaLayeredMappingsSpecBuilder> action) {
		return this.loom.layered(builder -> {
			var lambdaBuilder = new LambdaLayeredMappingsSpecBuilderImpl(builder);
			action.execute(lambdaBuilder);
		});
	}
}
