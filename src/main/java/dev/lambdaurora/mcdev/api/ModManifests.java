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
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ModManifests {
	Provider<Fmj> fmj();

	Fmj fmj(Fmj fmj);

	Fmj fmj(Action<Fmj> action);

	Provider<Nmt> nmt();

	Nmt nmt(Nmt nmt);

	Nmt nmt(Action<Nmt> action);
}
