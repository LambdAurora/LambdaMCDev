/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.util;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.lambdaurora.mcdev.api.manifest.Fmj;

public final class JsonUtils {
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Fmj.class, new Fmj.Serializer())
			.setFormattingStyle(FormattingStyle.PRETTY.withIndent("\t"))
			.create();
}
