/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

public interface MappingVariant extends Named {
	Attribute<MappingVariant> ATTRIBUTE = Attribute.of("net.minecraft.mappings", MappingVariant.class);

	String INTERMEDIARY = "intermediary";

	String MOJMAP = "mojmap";
}
