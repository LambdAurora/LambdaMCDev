/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerVisitor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This is an unsafe converter.
public final class AccessWidenerToTransformer implements AccessWidenerVisitor {
	private final Map<String, Boolean> classes = new HashMap<>();
	private final Map<FieldEntry, Boolean> fields = new HashMap<>();
	private final Map<MethodEntry, Boolean> methods = new HashMap<>();

	@Override
	public void visitClass(String name, AccessWidenerReader.AccessType access, boolean transitive) {
		this.classes.compute(
				name, (key, value) -> value == null
						? this.getAccess(access)
						: (value || this.getAccess(access))
		);
	}

	@Override
	public void visitField(
			String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive
	) {
		this.fields.compute(
				new FieldEntry(owner, name),
				(key, value) -> value == null
						? this.getAccess(access)
						: (value || this.getAccess(access))
		);
	}

	@Override
	public void visitMethod(
			String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive
	) {
		this.methods.compute(
				new MethodEntry(owner, name, descriptor),
				(key, value) -> value == null
						? this.getAccess(access)
						: (value || this.getAccess(access))
		);
	}

	private boolean getAccess(AccessWidenerReader.AccessType accessType) {
		return switch (accessType) {
			case ACCESSIBLE -> false;
			case EXTENDABLE, MUTABLE -> true;
		};
	}

	public List<String> toLines() {
		var at = new ArrayList<String>();

		for (var entry : this.classes.entrySet()) {
			String access = "public" + (entry.getValue() ? "-f" : "");
			at.add(access + " " + entry.getKey().replace('/', '.'));
		}

		for (var entry : this.fields.entrySet()) {
			String access = "public" + (entry.getValue() ? "-f" : "");
			var field = entry.getKey();
			at.add("%s %s %s".formatted(access, field.owner.replace('/', '.'), field.name));
		}

		for (var entry : this.methods.entrySet()) {
			String access = "public" + (entry.getValue() ? "-f" : "");
			var method = entry.getKey();
			at.add("%s %s %s%s".formatted(
					access,
					method.owner.replace('/', '.'),
					method.name, method.descriptor
			));
		}

		return at;
	}

	public static void convert(Path source, Path dest) throws IOException {
		var converter = new AccessWidenerToTransformer();

		try (var reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			var awReader = new AccessWidenerReader(converter);
			awReader.read(reader);
		}

		Files.write(dest, converter.toLines(), StandardCharsets.UTF_8);
	}

	private record FieldEntry(String owner, String name) {
		@Override
		public String toString() {
			return this.owner + " " + this.name;
		}
	}

	private record MethodEntry(String owner, String name, String descriptor) {
		@Override
		public String toString() {
			return this.owner + " " + this.name + this.descriptor;
		}
	}
}
