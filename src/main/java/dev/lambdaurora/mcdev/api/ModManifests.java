/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

import dev.lambdaurora.mcdev.api.manifest.Fmj;
import dev.lambdaurora.mcdev.api.manifest.Nmt;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.NotNull;

public interface ModManifests {
	@NotNull Provider<Fmj> fmj();

	@NotNull Fmj fmj(@NotNull Fmj fmj);

	@NotNull Fmj fmj(@NotNull Action<Fmj> action);

	@NotNull Provider<Nmt> nmt();

	@NotNull Nmt nmt(@NotNull Nmt nmt);

	@NotNull Nmt nmt(@NotNull Action<Nmt> action);
}
