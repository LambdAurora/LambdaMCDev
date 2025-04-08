/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api.manifest;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Nmt extends ModBase<Nmt> {
	private String loaderVersion;
	private boolean blurIcon = true;
	private final Map<String, String> depends = new LinkedHashMap<>();
	private final Map<String, Object> custom = new LinkedHashMap<>();

	public Nmt(String namespace, String name, String version) {
		super(namespace, name, version);
	}

	public Nmt withBlurIcon(boolean blurIcon) {
		this.blurIcon = blurIcon;
		return this;
	}

	public Nmt withLoaderVersion(String loaderVersion) {
		this.loaderVersion = loaderVersion;
		return this;
	}

	public Nmt withDepend(String dependency, String constraint) {
		this.depends.put(dependency, constraint);
		return this;
	}

	public Nmt withCustom(String key, Object value) {
		this.custom.put(key, value);
		return this;
	}

	public String toToml() {
		var builder = new TomlBuilder();

		builder
				.property("modLoader", "javafml")
				.property("loaderVersion", this.loaderVersion)
				.property("license", this.license);

		if (this.contact != null && this.contact.issues() != null) {
			builder.property("issueTrackerURL", this.contact.issues());
		}

		{
			builder.startArray("mods")
					.property("modId", this.namespace)
					.property("version", this.version)
					.property("displayName", this.name)
					.property("description", this.description)
					.property("authors", String.join(", ", this.authors));

			if (!this.contributors.isEmpty()) {
				builder.property("credits", String.join(", ", this.contributors));
			}

			if (this.icon != null) {
				builder.property("logoFile", this.icon);
				builder.property("logoBlur", this.blurIcon);
			}

			if (this.contact != null) {
				if (this.contact.homepage() != null) builder.property("displayURL", this.contact.homepage());
				if (this.contact.issues() != null) builder.property("issueTrackerURL", this.contact.issues());
			}

			builder.endSection();
		}

		this.depends.forEach((id, constraint) -> {
			builder.startArray("dependencies." + this.namespace);
			builder.property("modId", id);
			builder.property("type", "required");
			builder.property("versionRange", constraint);
			builder.property("ordering", "NONE");
			builder.property("side", "BOTH");
			builder.endSection();
		});

		if (this.icon != null || !this.custom.isEmpty()) {
			builder.startSection("modproperties." + this.namespace);

			if (this.icon != null) {
				builder.property("catalogueImageIcon", this.icon);
			}

			this.custom.forEach((key, value) -> builder.property(key, value.toString()));

			builder.endSection();
		}

		return builder.toString();
	}

	private static class TomlBuilder {
		private final StringBuilder builder = new StringBuilder();
		private int indent = 0;

		private void useIndent() {
			if (this.indent > 0) {
				this.builder.append("\t".repeat(this.indent));
			}
		}

		TomlBuilder property(String key, String value) {
			this.useIndent();
			this.builder.append(key).append(" = \"").append(value).append("\"\n");
			return this;
		}

		TomlBuilder property(String key, boolean value) {
			this.useIndent();
			this.builder.append(key).append(" = ").append(value).append("\n");
			return this;
		}

		TomlBuilder startSection(String section) {
			this.builder.append('\n');
			this.useIndent();
			this.builder.append('[').append(section).append("]\n");
			this.indent++;
			return this;
		}

		TomlBuilder startArray(String section) {
			this.builder.append('\n');
			this.useIndent();
			this.builder.append("[[").append(section).append("]]\n");
			this.indent++;
			return this;
		}

		TomlBuilder endSection() {
			this.indent--;
			return this;
		}

		@Override
		public String toString() {
			return this.builder.toString();
		}
	}
}
