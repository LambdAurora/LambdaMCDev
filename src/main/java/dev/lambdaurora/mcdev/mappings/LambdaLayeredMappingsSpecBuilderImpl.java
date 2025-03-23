/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.mappings;

import dev.lambdaurora.mcdev.api.mappings.LambdaLayeredMappingsSpecBuilder;
import net.fabricmc.loom.api.mappings.layered.spec.*;
import org.gradle.api.Action;

@SuppressWarnings("UnstableApiUsage")
public class LambdaLayeredMappingsSpecBuilderImpl implements LambdaLayeredMappingsSpecBuilder {
	private final LayeredMappingSpecBuilder parentBuilder;

	public LambdaLayeredMappingsSpecBuilderImpl(LayeredMappingSpecBuilder parentBuilder) {
		this.parentBuilder = parentBuilder;
	}

	@Override
	public LayeredMappingSpecBuilder addLayer(MappingsSpec<?> mappingSpec) {
		this.parentBuilder.addLayer(mappingSpec);
		return this;
	}

	@Override
	public LayeredMappingSpecBuilder officialMojangMappings(Action<MojangMappingsSpecBuilder> action) {
		var spec = MojangMappingsSpecBuilderImpl.builder();
		action.execute(spec);
		return this.addLayer(spec.build());
	}

	@Override
	public LayeredMappingSpecBuilder parchment(Object object, Action<ParchmentMappingsSpecBuilder> action) {
		this.parentBuilder.parchment(object, action);
		return this;
	}

	@Override
	public LayeredMappingSpecBuilder signatureFix(Object object) {
		this.parentBuilder.signatureFix(object);
		return this;
	}

	@Override
	public LayeredMappingSpecBuilder mappings(Object file, Action<? super FileMappingsSpecBuilder> action) {
		this.parentBuilder.mappings(file, action);
		return this;
	}
}
