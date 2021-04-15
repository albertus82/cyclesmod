package it.albertus.cyclesmod.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import it.albertus.cyclesmod.BaseTest;
import it.albertus.cyclesmod.CyclesMod;
import it.albertus.cyclesmod.cli.resources.ConsoleMessages;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.util.StringUtils;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class MessagesTest extends BaseTest {

	@Test
	public void checkMessageFiles() throws IOException {
		checkMessageFiles(getResourceNames(ConsoleMessages.class), "console.");
		checkMessageFiles(getResourceNames(GuiMessages.class), "gui.");
		checkMessageFiles(getResourceNames(CommonMessages.class), "common.");
	}

	private void checkMessageFiles(@NonNull final Iterable<String> resourceNames, final String prefix) throws IOException {
		final Collection<Properties> pp = new ArrayList<>();
		for (final String resourceName : resourceNames) {
			final Properties p = new Properties();
			pp.add(p);
			try (final InputStream is = getClass().getResourceAsStream('/' + resourceName)) {
				Assert.assertNotNull("Missing resource file: " + resourceName, is);
				p.load(is);
			}
			log.log(Level.INFO, "{0} messages found in: {1}", new Serializable[] { p.size(), resourceName });
			Assert.assertFalse("Empty resource file: " + resourceName, p.isEmpty());
		}
		pp.stream().reduce((p1, p2) -> {
			if (prefix != null) {
				p1.keySet().forEach(e -> Assert.assertTrue("Invalid property key '" + e + "': expected prefix '" + prefix + "'!", e.toString().startsWith(prefix)));
				p2.keySet().forEach(e -> Assert.assertTrue("Invalid property key '" + e + "': expected prefix '" + prefix + "'!", e.toString().startsWith(prefix)));
			}
			Assert.assertTrue("Uneven resource files: " + resourceNames, p1.keySet().containsAll(p2.keySet()));
			Assert.assertTrue("Uneven resource files: " + resourceNames, p2.keySet().containsAll(p1.keySet()));
			return p1;
		});
	}

	@Test
	public void checkMissingMessages() throws IOException {
		final Set<String> keys = new TreeSet<>();
		try (final Stream<Path> paths = newSourceStream()) {
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
		log.log(Level.INFO, "Found {0} message keys referenced in sources", keys.size());
		Assert.assertFalse("No message keys found in sources", keys.isEmpty());
		final Collection<String> consoleKeys = ConsoleMessages.INSTANCE.getKeys();
		log.log(Level.INFO, "{0} message keys available in resource bundle: {1}", new Serializable[] { consoleKeys.size(), ConsoleMessages.class.getSimpleName() });
		Assert.assertFalse("No message keys found in resource bundle: " + ConsoleMessages.class.getSimpleName(), consoleKeys.isEmpty());
		final Collection<String> guiKeys = GuiMessages.INSTANCE.getKeys();
		log.log(Level.INFO, "{0} message keys available in resource bundle: {1}", new Serializable[] { guiKeys.size(), GuiMessages.class.getSimpleName() });
		Assert.assertFalse("No message keys found in resource bundle: " + GuiMessages.class.getSimpleName(), guiKeys.isEmpty());

		for (final String key : new TreeSet<>(keys)) {
			if (key.startsWith("console.")) {
				Assert.assertTrue("Missing message key '" + key + "' in " + ConsoleMessages.class.getSimpleName(), consoleKeys.contains(key));
			}
			else if (key.startsWith("gui.")) {
				Assert.assertTrue("Missing message key '" + key + "' in " + GuiMessages.class.getSimpleName(), guiKeys.contains(key));
			}
			else if (key.startsWith("common.")) {
				Assert.assertTrue("Missing message key '" + key + "' in " + ConsoleMessages.class.getSimpleName(), consoleKeys.contains(key));
				Assert.assertTrue("Missing message key '" + key + "' in " + GuiMessages.class.getSimpleName(), guiKeys.contains(key));
			}
			else {
				Assert.assertTrue("Invalid message key prefix: '" + key + "'", false);
			}
		}
	}

	@Test
	public void checkUnreferencedMessages() throws IOException {
		checkUnreferencedMessages(getResourceNames(ConsoleMessages.class).iterator().next());
		checkUnreferencedMessages(getResourceNames(GuiMessages.class).iterator().next());
		checkUnreferencedMessages(getResourceNames(CommonMessages.class).iterator().next());
	}

	private void checkUnreferencedMessages(@NonNull final String resourceName) throws IOException {
		final Properties p = new Properties();
		try (final InputStream is = getClass().getResourceAsStream('/' + resourceName)) {
			Assert.assertNotNull("Missing resource file: " + resourceName, is);
			p.load(is);
		}
		log.log(Level.INFO, "{0} messages found in: {1}", new Serializable[] { p.size(), resourceName });
		Assert.assertFalse("Empty resource file: " + resourceName, p.isEmpty());
		final Set<String> usedKeys = new TreeSet<>();
		final Set<String> allKeys = new TreeSet<>(Collections.list(p.propertyNames()).stream().map(Object::toString).collect(Collectors.toSet()));
		try (final Stream<Path> paths = newSourceStream()) {
			for (final Path path : paths.collect(Collectors.toSet())) {
				try (final BufferedReader br = Files.newBufferedReader(path)) {
					String line;
					while ((line = br.readLine()) != null) {
						final String clean = line.trim().replace(" ", "").replace("\"", "");
						for (final String key : allKeys) {
							if (clean.contains(key)) {
								usedKeys.add(key);
							}
						}
					}
				}
			}
		}

		log.log(Level.INFO, "Found {0} message keys referenced in sources", usedKeys.size());

		final Set<String> unreferencedKeys = new TreeSet<>(allKeys.stream().filter(key -> !usedKeys.contains(key)).collect(Collectors.toSet()));
		if (!unreferencedKeys.isEmpty()) {
			log.log(Level.WARNING, "Unreferenced message keys: {0}", unreferencedKeys);
		}
	}

	private static Stream<Path> newSourceStream() throws IOException {
		final Path sourcesPath = Paths.get(projectProperties.getProperty("project.build.sourceDirectory"), CyclesMod.class.getPackage().getName().replace('.', File.separatorChar));
		log.log(Level.INFO, "Sources path: {0}", sourcesPath);
		return Files.walk(sourcesPath).filter(Files::isRegularFile).filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".java"));
	}

	private static Set<String> getResourceNames(final Class<?> messagesClass) {
		final Reflections reflections = new Reflections(messagesClass.getPackage().getName(), new ResourcesScanner());
		final Set<String> resourceNames = reflections.getResources(name -> name.contains(messagesClass.getSimpleName().toLowerCase(Locale.ROOT)) && name.endsWith(".properties"));
		log.log(Level.INFO, "Resources found: {0}", resourceNames);
		return resourceNames;
	}

}
