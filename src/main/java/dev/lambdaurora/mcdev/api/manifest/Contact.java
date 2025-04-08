/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api.manifest;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public final class Contact implements Serializable {
	private String homepage;
	private String sources;
	private String issues;

	@Contract(value = "_ -> this")
	public Contact withHomepage(String homepage) {
		this.homepage = homepage;
		return this;
	}

	@Contract(value = "_ -> this")
	public Contact withSources(String sources) {
		this.sources = sources;
		return this;
	}

	@Contract(value = "_ -> this")
	public Contact withIssues(String issues) {
		this.issues = issues;
		return this;
	}

	public String homepage() {
		return this.homepage;
	}

	public String sources() {
		return this.sources;
	}

	public String issues() {
		return this.issues;
	}

	public @NotNull Contact copy() {
		return new Contact()
				.withHomepage(this.homepage)
				.withSources(this.sources)
				.withIssues(this.issues);
	}
}
