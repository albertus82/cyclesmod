package io.github.albertus82.cyclesmod.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.albertus82.BaseTest;
import lombok.extern.java.Log;

@Log
class CyclesModCliTest extends BaseTest {

	private static final String BIKES_INF_FILENAME = "BIKES.INF";
	private static final String BIKES_CFG_FILENAME = "BIKES.CFG";
	private static final int BIKES_INF_SIZE_BYTES = 444;

	private static Path outputDir;

	@BeforeEach
	void beforeEach() throws IOException {
		outputDir = Paths.get(projectProperties.getProperty("project.build.directory"), "test-output-tmp");
		log.log(Level.INFO, "Creating directory ''{0}''...", outputDir);
		Files.createDirectories(outputDir);
		log.log(Level.INFO, "Created directory ''{0}''.", outputDir);
	}

	@AfterEach
	void afterEach() {
		log.log(Level.INFO, "Deleting directory ''{0}''...", outputDir);
		try {
			FileUtils.deleteDirectory(outputDir.toFile());
			log.log(Level.INFO, "Deleted directory ''{0}''.", outputDir);
		}
		catch (final IOException e) {
			log.log(Level.WARNING, e, () -> "Cannot delete directory '" + outputDir + "':");
		}
	}

	@Test
	void testRandoms() throws IOException {
		try (final InputStream is = getClass().getResourceAsStream("/random.cfg.zip"); final ZipInputStream zis = new ZipInputStream(is)) {
			while (true) {
				final ZipEntry e = zis.getNextEntry();
				if (e == null) {
					break;
				}
				final String expectedHash = FilenameUtils.getBaseName(e.getName());
				Files.copy(zis, Paths.get(outputDir.toString(), BIKES_CFG_FILENAME), StandardCopyOption.REPLACE_EXISTING);
				new CyclesModCli(outputDir, true, false).call();
				final String actualHash = DigestUtils.sha256Hex(Files.readAllBytes(Paths.get(outputDir.toString(), BIKES_INF_FILENAME)));
				Assertions.assertEquals(expectedHash, actualHash, e.toString());
			}
		}
	}

	@Test
	void test() throws IOException {
		// Check default
		new CyclesModCli(outputDir, true, false).call();
		final Properties expected = new Properties();
		final Properties actual = new Properties();
		try (final InputStream is = getClass().getResourceAsStream("/bikes.cfg.default.gz"); final InputStream gzis = new GZIPInputStream(is); final Reader r = Files.newBufferedReader(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			expected.load(gzis);
			actual.load(r);
		}
		Assertions.assertEquals(expected, actual);

		// Check custom (all zeros)
		for (final String key : actual.stringPropertyNames()) {
			actual.setProperty(key, "0");
		}
		try (final Writer os = Files.newBufferedWriter(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			actual.store(os, null);
		}
		new CyclesModCli(outputDir, true, false).call();
		try (final InputStream is = Files.newInputStream(Paths.get(outputDir.toString(), BIKES_INF_FILENAME))) {
			short byteCount = 0;
			int byteValue;
			while ((byteValue = is.read()) != -1) {
				Assertions.assertEquals(0, byteValue, "BIKES.INF does not match BIKES.CFG settings");
				byteCount++;
			}
			Assertions.assertEquals(BIKES_INF_SIZE_BYTES, byteCount, "Invalid BIKES.INF size");
		}

		// Check custom (randon non-zero)
		final short value = 0xBB; // A random byte value
		for (final String key : actual.stringPropertyNames()) {
			actual.setProperty(key, Integer.toString(key.contains("power") ? value : (value << 010) + value));
		}
		try (final Writer os = Files.newBufferedWriter(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			actual.store(os, null);
		}
		new CyclesModCli(outputDir, true, false).call();
		try (final InputStream is = Files.newInputStream(Paths.get(outputDir.toString(), BIKES_INF_FILENAME))) {
			short byteCount = 0;
			int byteValue;
			while ((byteValue = is.read()) != -1) {
				Assertions.assertEquals(value, byteValue, "BIKES.INF does not match BIKES.CFG settings");
				byteCount++;
			}
			Assertions.assertEquals(BIKES_INF_SIZE_BYTES, byteCount, "Invalid BIKES.INF size");
		}
	}

}
