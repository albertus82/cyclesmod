package it.albertus.cyclesmod.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.cyclesmod.BaseTest;

public class CyclesModConsoleTest extends BaseTest {

	@Test
	public void test() throws IOException {
		final Path outputDir = Paths.get(projectProperties.getProperty("project.build.directory"), "test-output");
		if (!Files.deleteIfExists(Paths.get(outputDir.toString(), "BIKES.CFG"))) {
			Files.createDirectories(outputDir);
		}
		CyclesModConsole.start(outputDir.toString());
		final Properties expected = new Properties();
		final Properties actual = new Properties();
		try (final InputStream is = getClass().getResourceAsStream("/bikes.cfg.default"); final Reader r = Files.newBufferedReader(Paths.get(outputDir.toString(), "BIKES.CFG"))) {
			expected.load(is);
			actual.load(r);
		}
		Assert.assertEquals(expected, actual);
	}

}
