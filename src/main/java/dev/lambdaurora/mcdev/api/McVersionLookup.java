/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

// Based off Fabric Loader's https://github.com/FabricMC/fabric-loader/blob/fda9a7b84f0898f57b46fbbfda58795e7fa31cef/minecraft/src/main/java/net/fabricmc/loader/impl/game/minecraft/McVersionLookup.java
public final class McVersionLookup {
	private static final Pattern RELEASE_PATTERN = Pattern.compile("\\d+\\.\\d+(?:\\.\\d+)?");
	private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("(?:Snapshot )?(\\d+)w0?(0|[1-9]\\d*)([a-z])");

	private static final Pattern ALMOST_RELEASE_PATTERN = Pattern.compile("\\d+\\.\\d+(?:\\.\\d+)?(?:-rc\\d+)?");

	private McVersionLookup() {
		throw new UnsupportedOperationException("McVersionLookup contains only static definitions.");
	}

	/**
	 * {@return {@code true} if the given Minecraft version is a release, or {@code false} otherwise}
	 *
	 * @param version the Minecraft version
	 */
	public static boolean isRelease(@NotNull String version) {
		return RELEASE_PATTERN.matcher(version).matches();
	}

	/**
	 * {@return {@code true} if the given Minecraft version is a release or a release candidate, or {@code false} otherwise}
	 *
	 * @param version the Minecraft version
	 */
	public static boolean isAlmostRelease(@NotNull String version) {
		return ALMOST_RELEASE_PATTERN.matcher(version).matches();
	}

	/**
	 * Gets the release cycle associated with the given Minecraft version.
	 *
	 * @param version the Minecraft version
	 * @return the release cycle associated
	 */
	public static @NotNull String getRelease(@NotNull String version) {
		if (isRelease(version)) return version;

		int index = version.indexOf("-pre");
		if (index >= 0) return version.substring(0, index);

		index = version.indexOf("-rc");
		if (index >= 0) return version.substring(0, index);

		var matcher = SNAPSHOT_PATTERN.matcher(version);

		if (matcher.matches()) {
			int year = Integer.parseInt(matcher.group(1));
			int week = Integer.parseInt(matcher.group(2));

			if (year == 25 && week >= 31 || year > 25) {
				return "1.21.9";
			} else if (year == 25 && week >= 15) {
				return "1.21.6";
			} else if (year == 25 && week >= 2) {
				return "1.21.5";
			} else if (year == 24 && week >= 44) {
				return "1.21.4";
			} else if (year == 24 && week >= 33 && week <= 40) {
				return "1.21.2";
			} else if (year == 24 && week >= 18 && week <= 21) {
				return "1.21";
			} else if (year == 23 && week >= 51 || year == 24 && week <= 14) {
				return "1.20.5";
			} else if (year == 23 && week >= 40 && week <= 46) {
				return "1.20.3";
			} else if (year == 23 && week >= 31 && week <= 35) {
				return "1.20.2";
			} else if (year == 23 && week >= 12 && week <= 18) {
				return "1.20";
			}

			// Only support as far back as 1.20 for now.
			// If there's really a need consider going back further.
		}

		throw new IllegalArgumentException("Unrecognized Minecraft version: " + version);
	}

	/**
	 * {@return the Minecraft version a mod user will be likely to focus on}
	 * <p>
	 * Mods having a tendency to update during release candidates of Minecraft releases,
	 * such updates will most likely end up used for the actual release.
	 * This method aims to get the most likely Minecraft release target.
	 * <p>
	 * This means that for {@code 1.21.5-rc1} the version tag will be {@code 1.21.5}.
	 *
	 * @param version the Minecraft version
	 */
	public static @NotNull String getVersionTag(String version) {
		if (isAlmostRelease(version)) return getRelease(version);
		else return version;
	}

	public static @NotNull String normalizeVersion(@NotNull String version) {
		String releaseCycle = getRelease(version);
		if (version.equals(releaseCycle)) return version;

		int index = version.indexOf("-rc");
		if (index >= 0) return releaseCycle + "-rc." + version.substring(index + 3);

		index = version.indexOf("-pre");
		if (index >= 0) return releaseCycle + "-beta." + version.substring(index + 4);

		var matcher = SNAPSHOT_PATTERN.matcher(version);

		if (matcher.matches()) {
			return releaseCycle + "-alpha.%s.%s.%s".formatted(matcher.group(1), matcher.group(2), matcher.group(3));
		}

		throw new IllegalArgumentException("Unrecognized Minecraft version: " + version);
	}

	/**
	 * {@return the CurseForge equivalent of a given Minecraft version}
	 * <p>
	 * This means for release it will return the same version.
	 * But for snapshots, pre-releases, or release candidates it will return the version cycle appended with "{@code -Snapshot}".
	 *
	 * @param version the Minecraft version
	 */
	public static @NotNull String getCurseForgeEquivalent(@NotNull String version) {
		String cycle = getRelease(version);

		if (version.equals(cycle)) return version;
		else return cycle + "-Snapshot";
	}
}
