/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api.manifest;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

public final class Fmj extends ModBase<Fmj> {
	private String environment;
	private final Map<String, List<String>> entrypoints = new LinkedHashMap<>();
	private String accessWidener;
	private final List<String> mixins = new ArrayList<>();
	private final Map<String, List<String>> depends = new LinkedHashMap<>();
	private final Map<String, List<String>> recommends = new LinkedHashMap<>();
	private final Map<String, List<String>> breaks = new LinkedHashMap<>();
	private final Map<String, Object> custom = new LinkedHashMap<>();
	private final List<String> jars = new ArrayList<>();

	public Fmj(String namespace, String name, String version) {
		super(namespace, name, version);
	}

	public Fmj withEnvironment(String environment) {
		this.environment = environment;
		return this;
	}

	public String getEnvironment() {
		return this.environment;
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

	public Fmj withDepend(String dependency, String... constraints) {
		this.depends.put(dependency, List.of(constraints));
		return this;
	}

	public Fmj withDepend(String dependency, String constraint) {
		this.depends.put(dependency, List.of(constraint));
		return this;
	}

	public Fmj withRecommend(String dependency, String... constraints) {
		this.recommends.put(dependency, List.of(constraints));
		return this;
	}

	public Fmj withRecommend(String dependency, String constraint) {
		this.recommends.put(dependency, List.of(constraint));
		return this;
	}

	public Fmj withBreak(String dependency, String... constraints) {
		this.breaks.put(dependency, List.of(constraints));
		return this;
	}

	public Fmj withBreak(String dependency, String constraint) {
		this.breaks.put(dependency, List.of(constraint));
		return this;
	}

	public Fmj withCustom(String key, Object value) {
		this.custom.put(key, value);
		return this;
	}

	public Fmj withModMenu(ModMenu modMenu) {
		this.custom.put("modmenu", modMenu);
		return this;
	}

	public Fmj withModMenu(Consumer<ModMenu> action) {
		action.accept((ModMenu) this.custom.computeIfAbsent("modmenu", k -> new ModMenu()));
		return this;
	}

	public ModMenu getModMenu() {
		return this.custom.get("modmenu") instanceof ModMenu modMenu ? modMenu : null;
	}

	public Fmj withJar(String jar) {
		this.jars.add(jar);
		return this;
	}

	public void copyTo(@NotNull Fmj target) {
		this.copyBaseTo(target);
		target.environment = this.environment;
		target.entrypoints.clear();
		this.entrypoints.forEach((k, v) -> {
			target.entrypoints.put(k, new ArrayList<>(v));
		});
		target.accessWidener = this.accessWidener;
		target.mixins.clear();
		target.mixins.addAll(this.mixins);
		target.depends.clear();
		this.depends.forEach((k, v) -> {
			target.depends.put(k, new ArrayList<>(v));
		});
		target.recommends.clear();
		this.recommends.forEach((k, v) -> {
			target.recommends.put(k, new ArrayList<>(v));
		});
		target.breaks.clear();
		this.breaks.forEach((k, v) -> {
			target.breaks.put(k, new ArrayList<>(v));
		});
		target.custom.clear();
		target.custom.putAll(this.custom);
		target.jars.clear();
		target.jars.addAll(this.jars);
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

		public ModMenu copy() {
			var copy = new ModMenu();
			if (this.links != null) {
				copy.links = new HashMap<>(this.links);
			}
			if (this.badges != null) {
				copy.badges = new ArrayList<>(this.badges);
			}
			if (this.parent != null) {
				copy.parent = this.parent.copy();
			}
			return copy;
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

			public void copyTo(@NotNull ParentMod target) {
				super.copyTo(target);
				if (this.badges != null) {
					target.badges = new ArrayList<>(this.badges);
				}
			}

			public ParentMod copy() {
				var copy = new ParentMod(this.namespace, this.name);
				this.copyTo(copy);
				return copy;
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
			if (!src.depends.isEmpty()) json.add("depends", this.serializeDependencyMap(src.depends, context));
			if (!src.recommends.isEmpty()) json.add("recommends", this.serializeDependencyMap(src.recommends, context));
			if (!src.breaks.isEmpty()) json.add("breaks", this.serializeDependencyMap(src.breaks, context));
			if (!src.custom.isEmpty()) json.add("custom", context.serialize(src.custom));
			if (!src.jars.isEmpty()) json.add("jars", this.serializeJars(src.jars));
			return json;
		}

		private JsonObject serializeDependencyMap(Map<String, List<String>> dependencies, JsonSerializationContext context) {
			var json = new JsonObject();
			dependencies.forEach((dependency, constraints) -> {
				if (constraints.size() == 1) {
					json.addProperty(dependency, constraints.getFirst());
				} else {
					json.add(dependency, context.serialize(constraints));
				}
			});
			return json;
		}

		private JsonArray serializeJars(List<String> jars) {
			var json = new JsonArray();
			jars.forEach(jar -> {
				var jarJson = new JsonObject();
				jarJson.addProperty("file", jar);
				json.add(jarJson);
			});
			return json;
		}
	}
}
