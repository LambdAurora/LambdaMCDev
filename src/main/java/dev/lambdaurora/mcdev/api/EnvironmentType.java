/*
 * Copyright 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

/// Represents an environment type.
///
/// @version 2.3.0
/// @since 2.3.0
public enum EnvironmentType {
	CLIENT_AND_SERVER,
	CLIENT_ONLY,
	CLIENT_ONLY_SERVER_OPTIONAL,
	SINGLEPLAYER_ONLY,
	SERVER_ONLY,
	SERVER_ONLY_CLIENT_OPTIONAL,
	DEDICATED_SERVER_ONLY,
	CLIENT_OR_SERVER,
	CLIENT_OR_SERVER_PREFERS_BOTH;

	/// {@return the identifier of this environment type}
	public String id() {
		return this.name().toLowerCase();
	}
}
