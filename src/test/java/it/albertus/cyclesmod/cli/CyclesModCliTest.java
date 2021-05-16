package it.albertus.cyclesmod.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.albertus.cyclesmod.BaseTest;
import lombok.extern.java.Log;

@Log
public class CyclesModCliTest extends BaseTest {

	private static final String BIKES_INF_FILENAME = "BIKES.INF";
	private static final String BIKES_CFG_FILENAME = "BIKES.CFG";
	private static final int BIKES_INF_SIZE_BYTES = 444;

	private static Path outputDir;

	@BeforeClass
	public static void beforeAll() throws IOException {
		outputDir = Paths.get(projectProperties.getProperty("project.build.directory"), "test-output-tmp");
		log.log(Level.INFO, "Creating directory ''{0}''...", outputDir);
		Files.createDirectories(outputDir);
		log.log(Level.INFO, "Created directory ''{0}''.", outputDir);
	}

	@AfterClass
	public static void afterAll() {
		log.log(Level.INFO, "Deleting directory ''{0}''...", outputDir);
		try {
			FileUtils.deleteDirectory(outputDir.toFile());
			log.log(Level.INFO, "Deleted directory ''{0}''.", outputDir);
		}
		catch (final IOException e) {
			log.log(Level.WARNING, e, () -> "Cannot delete directory '" + outputDir + "':");
		}
	}

	@Before
	public void before() throws IOException {
		Files.deleteIfExists(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME));
	}

	@Test
	public void test() throws IOException {
		// Check default
		new CyclesModCli(outputDir, true).call();
		final Properties expected = new Properties();
		final Properties actual = new Properties();
		try (final InputStream is = getClass().getResourceAsStream("/bikes.cfg.default.gz"); final InputStream gzis = new GZIPInputStream(is); final Reader r = Files.newBufferedReader(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			expected.load(gzis);
			actual.load(r);
		}
		Assert.assertEquals(expected, actual);

		// Check custom (all zeros)
		for (final String key : actual.stringPropertyNames()) {
			actual.setProperty(key, "0");
		}
		try (final Writer os = Files.newBufferedWriter(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			actual.store(os, null);
		}
		new CyclesModCli(outputDir, true).call();
		try (final InputStream is = Files.newInputStream(Paths.get(outputDir.toString(), BIKES_INF_FILENAME))) {
			short byteCount = 0;
			int byteValue;
			while ((byteValue = is.read()) != -1) {
				Assert.assertEquals("BIKES.INF does not match BIKES.CFG settings", 0, byteValue);
				byteCount++;
			}
			Assert.assertEquals("Invalid BIKES.INF size", BIKES_INF_SIZE_BYTES, byteCount);
		}

		// Check custom (randon non-zero)
		final short value = 0xBB; // A random byte value
		for (final String key : actual.stringPropertyNames()) {
			actual.setProperty(key, Integer.toString(key.contains("power") ? value : (value << 010) + value));
		}
		try (final Writer os = Files.newBufferedWriter(Paths.get(outputDir.toString(), BIKES_CFG_FILENAME))) {
			actual.store(os, null);
		}
		new CyclesModCli(outputDir, true).call();
		try (final InputStream is = Files.newInputStream(Paths.get(outputDir.toString(), BIKES_INF_FILENAME))) {
			short byteCount = 0;
			int byteValue;
			while ((byteValue = is.read()) != -1) {
				Assert.assertEquals("BIKES.INF does not match BIKES.CFG settings", value, byteValue);
				byteCount++;
			}
			Assert.assertEquals("Invalid BIKES.INF size", BIKES_INF_SIZE_BYTES, byteCount);
		}
	}

}
