/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.api

import org.gradle.api.Project
import java.util.regex.Pattern

public object ModUtils {
	private val README_LINK_REGEX: Pattern = Pattern.compile("!\\[(.+?)]\\((assets/[A-z./_]+)\\)")

	@JvmStatic
	public fun fetchVersionType(version: String, mcVersion: String): String {
		return if ("-alpha." in version) {
			"alpha"
		} else if ("-pre" in mcVersion) {
			"beta"
		} else if (!McVersionLookup.isAlmostRelease(mcVersion)) {
			"alpha"
		} else if ("-beta." in version) {
			"beta"
		} else {
			"release"
		}
	}

	@JvmStatic
	public fun formatReadme(readme: String, assetLinkReplacement: String): String {
		var result = readme
		val lines = result.split("\n").toMutableList()
		val it = lines.listIterator()

		var shouldRemove = false
		while (it.hasNext()) {
			val line = it.next()

			if (line == "<!-- modrinth_exclude.long_start -->") {
				shouldRemove = true
			}

			if (shouldRemove) {
				it.remove()
			}

			if (line == "<!-- modrinth_exclude.long_end -->") {
				shouldRemove = false
			}
		}

		result = lines.joinToString("\n")
		result = README_LINK_REGEX.matcher(result).replaceAll("![$1]($assetLinkReplacement)")
		result = result.replace("<!-- modrinth_only.start ", "")
		result = result.replace(" modrinth_only.end -->", "")
		return result
	}

	@JvmStatic
	public fun parseReadme(project: Project, assetLinkReplacement: String): String {
		return this.formatReadme(project.rootProject.file("README.md").readText(), assetLinkReplacement)
	}

	@JvmStatic
	public fun fetchChangelog(version: String, changelogs: String): String? {
		val regexVersion = version.replace("\\.".toRegex(), "\\.").replace("\\+".toRegex(), "\\+")
		val changelogRegex = "###? ${regexVersion}\\n\\n(( *- .+\\n)+)".toRegex()
		val matcher = changelogRegex.find(changelogs)

		if (matcher != null) {
			var changelogContent = matcher.groupValues[1]

			val changelogLines = changelogs.substring(matcher.range.last).split("\n")
			val linkRefRegex = "^\\[([A-z\\d _\\-/+.#]+)]: .+$".toRegex()
			for (line in changelogLines) {
				if (line matches linkRefRegex)
					changelogContent += "\n" + line
			}
			return changelogContent
		} else {
			return null;
		}
	}

	@JvmStatic
	public fun fetchChangelog(project: Project, version: String): String? {
		return this.fetchChangelog(version, project.rootProject.file("CHANGELOG.md").readText())
	}
}
