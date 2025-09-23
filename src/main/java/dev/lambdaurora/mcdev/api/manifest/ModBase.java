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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ModBase<SELF extends ModBase<SELF>> extends ModShell<SELF> {
	protected final String version;
	protected final List<String> authors = new ArrayList<>();
	protected final List<String> contributors = new ArrayList<>();
	protected Contact contact;
	protected String license;

	public ModBase(String namespace, String name, String version) {
		super(namespace, name);
		this.version = version;
	}

	@SuppressWarnings("unchecked")
	private SELF $self() {
		return (SELF) this;
	}

	@Contract(value = "_ -> this")
	public SELF withAuthors(List<String> authors) {
		this.authors.addAll(authors);
		return this.$self();
	}

	@Contract(value = "_ -> this")
	public SELF withAuthors(String... authors) {
		return this.withAuthors(Arrays.asList(authors));
	}

	@Contract(value = "_ -> this")
	public SELF withContributors(List<String> contributors) {
		this.contributors.addAll(contributors);
		return this.$self();
	}

	@Contract(value = "_ -> this")
	public SELF withContributors(String... contributors) {
		return this.withContributors(Arrays.asList(contributors));
	}

	private @NotNull Contact useContact() {
		if (this.contact == null) this.contact = new Contact();
		return this.contact;
	}

	@Contract(value = "_ -> this")
	public SELF withContact(@NotNull Consumer<Contact> action) {
		action.accept(this.useContact());
		return this.$self();
	}

	public SELF withLicense(String license) {
		this.license = license;
		return this.$self();
	}

	public void copyTo(@NotNull ModBase<?> target) {
		this.copyBaseTo(target);
	}

	public void copyBaseTo(@NotNull ModBase<?> target) {
		this.copyShellTo(target);
		target.authors.addAll(this.authors);
		target.contributors.addAll(this.contributors);
		target.contact = this.contact != null ? this.contact.copy() : null;
		target.license = this.license;
	}

	public <VARIANT extends ModBase<VARIANT>> VARIANT derive(ModBaseFactory<VARIANT> factory) {
		var variant = factory.create(this.namespace, this.name, this.version);
		this.copyTo(variant);
		return variant;
	}

	public interface ModBaseFactory<SELF extends ModBase<SELF>> {
		SELF create(String namespace, String name, String version);
	}
}
