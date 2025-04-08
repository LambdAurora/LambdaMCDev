/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api.manifest;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the shell data of a mod.
 *
 * @param <SELF> the type of this class
 */
public class ModShell<SELF extends ModShell<SELF>> implements Serializable {
	@SerializedName("id")
	protected String namespace;
	protected String name;
	protected String description;
	protected String icon;

	public ModShell(@NotNull String namespace, @NotNull String name) {
		this.namespace = Objects.requireNonNull(namespace);
		this.name = Objects.requireNonNull(name);
	}

	@SuppressWarnings("unchecked")
	private SELF $self() {
		return (SELF) this;
	}

	@Contract(value = "_ -> this")
	public SELF withNamespace(@NotNull String namespace) {
		this.namespace = Objects.requireNonNull(namespace);
		return this.$self();
	}

	@Contract(value = "_ -> this")
	public SELF withName(@NotNull String name) {
		this.name = Objects.requireNonNull(name);
		return this.$self();
	}

	@Contract(value = "_ -> this")
	public SELF withDescription(String description) {
		this.description = description;
		return this.$self();
	}

	@Contract(value = "_ -> this")
	public SELF withIcon(String icon) {
		this.icon = icon;
		return this.$self();
	}

	public void copyTo(@NotNull ModShell<?> target) {
		target.namespace = this.namespace;
		target.name = this.name;
		target.description = this.description;
		target.icon = this.icon;
	}
}
