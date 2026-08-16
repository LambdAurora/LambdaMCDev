/*
 * Copyright 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api.manifest;

public enum ModEnvironment {
	CLIENT("client"),
	SERVER("server"),
	UNIVERSAL("*");

	private final String value;

	ModEnvironment(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}
}
