/*
 * Copyright 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

import dev.lambdaurora.mcdev.api.manifest.ModEnvironment;
import org.jspecify.annotations.NullMarked;

/// Represents an environment type.
///
/// @version 3.0.0
/// @since 2.3.0
@NullMarked
public enum EnvironmentType {
	CLIENT_AND_SERVER(ModEnvironment.UNIVERSAL),
	CLIENT_ONLY(ModEnvironment.CLIENT),
	CLIENT_ONLY_SERVER_OPTIONAL(ModEnvironment.UNIVERSAL),
	SINGLEPLAYER_ONLY(ModEnvironment.CLIENT),
	SERVER_ONLY(ModEnvironment.UNIVERSAL),
	SERVER_ONLY_CLIENT_OPTIONAL(ModEnvironment.UNIVERSAL),
	DEDICATED_SERVER_ONLY(ModEnvironment.SERVER),
	CLIENT_OR_SERVER(ModEnvironment.UNIVERSAL),
	CLIENT_OR_SERVER_PREFERS_BOTH(ModEnvironment.UNIVERSAL);

	private final ModEnvironment modEnvironment;

	EnvironmentType(ModEnvironment modEnvironment) {
		this.modEnvironment = modEnvironment;
	}

	/// {@return the identifier of this environment type}
	public String id() {
		return this.name().toLowerCase();
	}

	/// {@return best-guess as to what mod environment would be most accurate for this environment type}
	public ModEnvironment toModEnvironment() {
		return this.modEnvironment;
	}

	/// {@return the environment type based off the manifest's mod environment}
	///
	/// This is a best-guess attempt, as [EnvironmentType] is a lot more granular than [ModEnvironment]
	public static EnvironmentType of(ModEnvironment environment) {
		return switch (environment) {
			case CLIENT -> CLIENT_ONLY;
			case SERVER -> SERVER_ONLY;
			default -> CLIENT_AND_SERVER;
		};
	}
}
