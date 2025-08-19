/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

import com.google.gson.JsonObject;

import java.io.Serializable;

public record ModVersionDependency(
		String id,
		Type type
) implements Serializable {
	public enum Type {
		REQUIRED,
		OPTIONAL,
		INCOMPATIBLE,
		EMBEDDED;

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}

	public JsonObject toJson() {
		var json = new JsonObject();
		json.addProperty("type", this.type.toString());
		json.addProperty("project_id", this.id);
		return json;
	}
}
