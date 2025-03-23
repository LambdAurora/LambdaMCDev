/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.mappings;

import net.fabricmc.loom.api.mappings.layered.spec.MojangMappingsSpecBuilder;

public class MojangMappingsSpecBuilderImpl implements MojangMappingsSpecBuilder {
	private boolean nameSyntheticMembers = false;

	private MojangMappingsSpecBuilderImpl() {
	}

	public static MojangMappingsSpecBuilderImpl builder() {
		return new MojangMappingsSpecBuilderImpl();
	}

	@Override
	public MojangMappingsSpecBuilder setNameSyntheticMembers(boolean value) {
		this.nameSyntheticMembers = value;
		return this;
	}

	@Override
	public boolean getNameSyntheticMembers() {
		return this.nameSyntheticMembers;
	}

	public MojangMappingsSpec build() {
		return new MojangMappingsSpec(this.nameSyntheticMembers);
	}
}