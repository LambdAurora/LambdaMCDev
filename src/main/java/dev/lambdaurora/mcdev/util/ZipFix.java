/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev.util;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.GregorianCalendar;

public final class ZipFix {
	// Please see the insane comment on ZipEntryConstants in Gradle
	// https://github.com/gradle/gradle/blob/360f9eab2f6f1595025f746a03ee5895659b0b8c/platforms/core-runtime/files/src/main/java/org/gradle/api/internal/file/archive/ZipEntryConstants.java#L39
	public static long ZIP_EPOCH_TIME = new GregorianCalendar(
			1980, Calendar.FEBRUARY,
			1, 0, 0, 0
	).getTimeInMillis();

	public static void makeZipReproducible(Path path) throws IOException {
		// Cache in-memory the contents of the ZIP so we can safely write back.
		var bytes = Files.readAllBytes(path);
		try (
				var ais = new JarArchiveInputStream(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8.name());
				var out = new JarArchiveOutputStream(Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING));
		) {
			ais.forEach(it -> {
				var entry = new JarArchiveEntry(it.getName());
				entry.setTime(ZIP_EPOCH_TIME);
				if (!it.isDirectory()) {
					entry.setMethod(it.getMethod());
					if (it.getSize() != -1L) entry.setSize(it.getSize());
				}

				entry.setUnixMode(entry.isDirectory()
						? UnixStat.DIR_FLAG | 0b111101101
						: UnixStat.FILE_FLAG | 0b110100100
				);

				out.putArchiveEntry(entry);

				if (!entry.isDirectory()) {
					IOUtils.copy(ais, out);
				}

				out.closeArchiveEntry();
			});
		}
	}
}
