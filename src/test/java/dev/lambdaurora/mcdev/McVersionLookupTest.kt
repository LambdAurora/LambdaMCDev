/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev

import dev.lambdaurora.mcdev.api.McVersionLookup
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class McVersionLookupTest {
	@Test
	fun `is release`() {
		Assertions.assertTrue(McVersionLookup.isRelease("1.4.7"))
		Assertions.assertTrue(McVersionLookup.isRelease("1.21"))
		Assertions.assertTrue(McVersionLookup.isRelease("1.21.1"))
		Assertions.assertTrue(McVersionLookup.isRelease("1.21.2"))
		Assertions.assertTrue(McVersionLookup.isRelease("1.21.4"))
		Assertions.assertTrue(McVersionLookup.isRelease("1.21.5"))
		Assertions.assertTrue(McVersionLookup.isRelease("1.22"))

		Assertions.assertFalse(McVersionLookup.isRelease("25w05a"))
		Assertions.assertFalse(McVersionLookup.isRelease("1.21.4-pre1"))
		Assertions.assertFalse(McVersionLookup.isRelease("1.21.5-rc1"))
	}

	@Test
	fun `release cycle`() {
		Assertions.assertEquals("1.21.5", McVersionLookup.getRelease("1.21.5-rc1"))
		Assertions.assertEquals("1.21.5", McVersionLookup.getRelease("1.21.5-pre1"))
		Assertions.assertEquals("1.21.5", McVersionLookup.getRelease("25w07a"))
		Assertions.assertEquals("1.21.4", McVersionLookup.getRelease("24w46a"))
	}

	@Test
	fun `normalize version`() {
		Assertions.assertEquals("1.21.5", McVersionLookup.normalizeVersion("1.21.5"))
		Assertions.assertEquals("1.21.5-rc.1", McVersionLookup.normalizeVersion("1.21.5-rc1"))
		Assertions.assertEquals("1.21.5-beta.1", McVersionLookup.normalizeVersion("1.21.5-pre1"))
		Assertions.assertEquals("1.21.5-alpha.25.7.a", McVersionLookup.normalizeVersion("25w07a"))
	}

	@Test
	fun `CurseForge equivalence`() {
		Assertions.assertEquals("1.21.5-Snapshot", McVersionLookup.getCurseForgeEquivalent("1.21.5-rc1"))
		Assertions.assertEquals("1.21.5-Snapshot", McVersionLookup.getCurseForgeEquivalent("1.21.5-pre1"))
		Assertions.assertEquals("1.21.5-Snapshot", McVersionLookup.getCurseForgeEquivalent("25w07a"))
		Assertions.assertEquals("1.21.5", McVersionLookup.getCurseForgeEquivalent("1.21.5"))
	}
}
