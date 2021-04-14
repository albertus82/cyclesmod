package it.albertus.cyclesmod.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CyclesModConsoleTest {

	private static final Properties testProperties = new Properties();

	@BeforeClass
	public static void beforeAll() throws IOException {
		try (final InputStream is = CyclesModConsoleTest.class.getResourceAsStream("/test.properties")) {
			testProperties.load(is);
		}
	}

	@Test
	public void test() throws IOException {
		final Path basedir = Paths.get(testProperties.getProperty("project.basedir"));
		CyclesModConsole.start(basedir.toString());
		final Properties expected = new Properties();
		final Properties actual = new Properties();
		try (final InputStream is = getClass().getResourceAsStream("/bikes.cfg.default"); final Reader r = Files.newBufferedReader(Paths.get(basedir.toString(), "BIKES.CFG"))) {
			expected.load(is);
			actual.load(r);
		}
		Assert.assertEquals(expected, actual);
	}

}
