package it.albertus.cyclesmod.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import it.albertus.cyclesmod.BaseTest;

public class CyclesModConsoleTest extends BaseTest {

	private static final String BIKES_INF_FILENAME = "BIKES.INF";
	private static final String BIKES_CFG_FILENAME = "BIKES.CFG";
	private static final int BIKES_INF_SIZE_BYTES = 444;

	@Before
	public void before() throws IOException {
		final Path outputDir = Paths.get(projectProperties.getProperty("project.build.directory"), "test-output");
		if (!Files.deleteIfExists(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			Files.createDirectories(outputDir);
		}
	}

	@Test
	public void test() throws IOException {
		final Path outputDir = Paths.get(projectProperties.getProperty("project.build.directory"), "test-output");
		CyclesModConsole.start(outputDir.toString());
		final Properties expected = new Properties();
		final Properties actual = new Properties();
		try (final InputStream is = getClass().getResourceAsStream("/bikes.cfg.default"); final Reader r = Files.newBufferedReader(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			expected.load(is);
			actual.load(r);
		}
		Assert.assertEquals(expected, actual); // Check default

		for (final Object key : Collections.list(actual.propertyNames())) {
			actual.setProperty(key.toString(), "0");
		}
		try (final Writer os = Files.newBufferedWriter(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			actual.store(os, null);
		}
		CyclesModConsole.start(outputDir.toString());

		try (final InputStream is = Files.newInputStream(Paths.get(outputDir.toString(), BIKES_INF_FILENAME))) {
			short byteCount = 0;
			int byteValue;
			while ((byteValue = is.read()) != -1) {
				Assert.assertEquals(0, byteValue);
				byteCount++;
			}
			Assert.assertEquals(BIKES_INF_SIZE_BYTES, byteCount); // Check custom (all zeros)
		}

		final int value = 1 + ThreadLocalRandom.current().nextInt(0xFF);
		for (final Object key : Collections.list(actual.propertyNames())) {
			actual.setProperty(key.toString(), Integer.toString(value));
		}
		try (final Writer os = Files.newBufferedWriter(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			actual.store(os, null);
		}
		CyclesModConsole.start(outputDir.toString());

		try (final InputStream is = Files.newInputStream(Paths.get(outputDir.toString(), BIKES_INF_FILENAME))) {
			short byteCount = 0;
			int byteValue;
			while ((byteValue = is.read()) != -1) {
				Assert.assertTrue(byteValue == value || byteValue == 0);
				byteCount++;
			}
			Assert.assertEquals(BIKES_INF_SIZE_BYTES, byteCount); // Check custom (non zero)
		}
	}

}
