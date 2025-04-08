/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api.manifest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

public final class Fmj extends ModBase<Fmj> {
	private String environment;
	private final Map<String, List<String>> entrypoints = new LinkedHashMap<>();
	private String accessWidener;
	private final List<String> mixins = new ArrayList<>();
	private final Map<String, String> depends = new LinkedHashMap<>();
	private final Map<String, String> recommends = new LinkedHashMap<>();
	private final Map<String, String> breaks = new LinkedHashMap<>();
	private final Map<String, Object> custom = new LinkedHashMap<>();

	public Fmj(String namespace, String name, String version) {
		super(namespace, name, version);
	}

	public Fmj withEnvironment(String environment) {
		this.environment = environment;
		return this;
	}

	public Fmj withEntrypoints(String entrypointName, String... entrypoints) {
		this.entrypoints.computeIfAbsent(entrypointName, k -> new ArrayList<>()).addAll(Arrays.asList(entrypoints));
		return this;
	}

	public Fmj withAccessWidener(String accessWidener) {
		this.accessWidener = accessWidener;
		return this;
	}

	public Fmj withMixins(String... mixins) {
		this.mixins.addAll(Arrays.asList(mixins));
		return this;
	}

	public Fmj withDepend(String dependency, String constraint) {
		this.depends.put(dependency, constraint);
		return this;
	}

	public Fmj withRecommend(String dependency, String constraint) {
		this.recommends.put(dependency, constraint);
		return this;
	}

	public Fmj withBreak(String dependency, String constraint) {
		this.breaks.put(dependency, constraint);
		return this;
	}

	public Fmj withCustom(String key, Object value) {
		this.custom.put(key, value);
		return this;
	}

	public Fmj withModMenu(Consumer<ModMenu> action) {
		action.accept((ModMenu) this.custom.computeIfAbsent("modmenu", k -> new ModMenu()));
		return this;
	}

	public static final class ModMenu implements Serializable {
		private Map<String, String> links;
		private List<String> badges;
		private ParentMod parent;

		private Map<String, String> useLinks() {
			if (this.links == null) this.links = new LinkedHashMap<>();
			return this.links;
		}

		public ModMenu withLink(String key, String value) {
			this.useLinks().put(key, value);
			return this;
		}

		public ModMenu withCurseForge(String url) {
			return this.withLink("modmenu.curseforge", url);
		}

		public ModMenu withModrinth(String url) {
			return this.withLink("modmenu.modrinth", url);
		}

		public ModMenu withGitHubReleases(String url) {
			return this.withLink("modmenu.github_releases", url);
		}

		public ModMenu withDiscord(String url) {
			return this.withLink("modmenu.discord", url);
		}

		private List<String> useBadges() {
			if (this.badges == null) this.badges = new ArrayList<>();
			return this.badges;
		}

		public ModMenu withBadges(String... badges) {
			this.useBadges().addAll(Arrays.asList(badges));
			return this;
		}

		public ModMenu withParent(ParentMod parent) {
			this.parent = parent;
			return this;
		}

		public ModMenu withParent(String namespace, String name, Consumer<ParentMod> action) {
			var mod = new ParentMod(namespace, name);
			action.accept(mod);
			return this.withParent(mod);
		}

		public static final class ParentMod extends ModShell<ParentMod> {
			private List<String> badges;

			public ParentMod(String namespace, String name) {
				super(namespace, name);
			}

			private List<String> useBadges() {
				if (this.badges == null) this.badges = new ArrayList<>();
				return this.badges;
			}

			public ParentMod withBadges(String... badges) {
				this.useBadges().addAll(Arrays.asList(badges));
				return this;
			}
		}
	}

	public static final class Serializer implements JsonSerializer<Fmj> {
		@Override
		public JsonElement serialize(Fmj src, Type typeOfSrc, JsonSerializationContext context) {
			var json = new JsonObject();
			json.addProperty("schemaVersion", 1);
			json.addProperty("id", src.namespace);
			json.addProperty("name", src.name);
			json.addProperty("version", src.version);
			if (src.description != null) json.addProperty("description", src.description);
			if (!src.authors.isEmpty()) json.add("authors", context.serialize(src.authors));
			if (!src.contributors.isEmpty()) json.add("contributors", context.serialize(src.contributors));
			if (src.contact != null) json.add("contact", context.serialize(src.contact));
			if (src.license != null) json.addProperty("license", src.license);
			if (src.icon != null) json.addProperty("icon", src.icon);
			if (src.environment != null) json.addProperty("environment", src.environment);
			if (!src.entrypoints.isEmpty()) json.add("entrypoints", context.serialize(src.entrypoints));
			if (src.accessWidener != null) json.addProperty("accessWidener", src.accessWidener);
			if (!src.mixins.isEmpty()) json.add("mixins", context.serialize(src.mixins));
			if (!src.depends.isEmpty()) json.add("depends", context.serialize(src.depends));
			if (!src.recommends.isEmpty()) json.add("recommends", context.serialize(src.recommends));
			if (!src.breaks.isEmpty()) json.add("breaks", context.serialize(src.breaks));
			if (!src.custom.isEmpty()) json.add("custom", context.serialize(src.custom));
			return json;
		}
	}
}
