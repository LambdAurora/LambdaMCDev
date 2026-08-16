/*
 * Copyright 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api.manifest;

import com.google.gson.*;

import java.lang.reflect.Type;

/// Represents a mixin entry.
///
/// @author LambdAurora
/// @version 3.0.0
/// @since 3.0.0
public record MixinEntry(String config, ModEnvironment environment) {
	public MixinEntry(String config) {
		this(config, ModEnvironment.UNIVERSAL);
	}

	public static class JsonAdapter implements JsonSerializer<MixinEntry> {
		@Override
		public JsonElement serialize(MixinEntry src, Type typeOfSrc, JsonSerializationContext context) {
			if (src.environment == ModEnvironment.UNIVERSAL) {
				return new JsonPrimitive(src.config);
			}

			var object = new JsonObject();
			object.addProperty("config", src.config);
			object.addProperty("environment", src.environment.value());
			return object;
		}
	}
}
