package it.albertus.cyclesmod.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.cyclesmod.CyclesMod;
import it.albertus.jface.JFaceMessages;
import it.albertus.util.StringUtils;
import lombok.extern.java.Log;

@Log
public class MessagesTest {

	@Test
	public void checkMissing() throws IOException {
		final Properties testProperties = new Properties();
		try (final InputStream is = MessagesTest.class.getResourceAsStream("/test.properties")) {
			testProperties.load(is);
		}
		final Path sourcesPath = Paths.get(testProperties.getProperty("project.build.sourceDirectory"), CyclesMod.class.getPackage().getName().replace('.', File.separatorChar));
		log.log(Level.INFO, "Sources path: {0}", sourcesPath);
		final Set<String> keys = new TreeSet<>();
		try (final Stream<Path> paths = Files.walk(sourcesPath).filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java"))) {
			paths.forEach(path -> {
				log.log(Level.FINE, "{0}", path);
				try {
					// @formatter:off
					keys.addAll(Files.readAllLines(path).stream()
							.map(line -> line.trim().replace(" ", ""))
							.filter(e -> e.toLowerCase(Locale.ROOT).contains("messages.get(\""))
							.flatMap(e -> Arrays.stream(e.split("(?i)(?>=messages\\.get\\(\")|(?=messages\\.get\\(\")")))
							.filter(e -> e.toLowerCase(Locale.ROOT).startsWith("messages"))
							.map(e -> StringUtils.substringBefore(StringUtils.substringAfter(e, "\""), "\""))
							.filter(key -> !key.endsWith("."))
							.collect(Collectors.toSet()));
					// @formatter:on
				}
				catch (final IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		}
		Assert.assertNotEquals("No message keys found.", 0, keys.size());
		log.log(Level.INFO, "Found {0} message keys referenced in sources.", keys.size());
		final Set<String> allKeys = new HashSet<>();
		allKeys.addAll(Messages.getKeys());
		allKeys.addAll(JFaceMessages.getKeys());
		for (final String key : new TreeSet<>(keys)) {
			Assert.assertTrue("Missing message key '" + key + "'!", allKeys.contains(key));
		}
	}

}
