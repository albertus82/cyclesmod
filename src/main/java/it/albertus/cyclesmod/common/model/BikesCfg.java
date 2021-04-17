package it.albertus.cyclesmod.common.model;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.util.NewLine;
import lombok.extern.java.Log;

@Log
public class BikesCfg {

	private static final String FILE_NAME = "BIKES.CFG";
	private static final int RADIX = 10;
	private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

	private static final Messages messages = CommonMessages.INSTANCE;

	private final Properties properties = new Properties();

	public BikesCfg(final BikesInf bikesInf) {
		try (final StringReader reader = new StringReader(createProperties(bikesInf))) {
			populateProperties(reader);
		}
		catch (final IOException e) {
			throw new UncheckedIOException(e); // No exception possible with StringReader!
		}
	}

	public BikesCfg(final Path sourceFile) throws IOException {
		log.log(Level.INFO, messages.get("common.message.reading.file"), FILE_NAME);
		try (final Reader reader = Files.newBufferedReader(sourceFile, CHARSET)) {
			populateProperties(reader); // buffered internally
		}
		log.log(Level.INFO, messages.get("common.message.file.read"), FILE_NAME);
	}

	public BikesCfg(final BikesInf originalBikesInf, final Path destDir) throws IOException {
		log.log(Level.INFO, messages.get("common.message.reading.file"), FILE_NAME);
		final Path destFile = Paths.get(destDir.toString(), FILE_NAME);
		if (!destFile.toFile().exists()) {
			log.log(Level.INFO, messages.get("common.message.file.not.found.creating.default"), FILE_NAME);
			writeDefaultBikesCfg(originalBikesInf, destFile);
			log.log(Level.INFO, messages.get("common.message.default.file.created"), FILE_NAME);
		}
		try (final Reader reader = Files.newBufferedReader(destFile)) {
			populateProperties(reader); // buffered internally
		}
		log.log(Level.INFO, messages.get("common.message.file.read"), FILE_NAME);
	}

	private void populateProperties(final Reader reader) throws IOException {
		properties.load(reader);
		final Map<String, String> deprecatedEntries = new HashMap<>();
		final String deprecatedPrefix = "torque";
		for (final String key : properties.stringPropertyNames()) {
			if (key.contains(deprecatedPrefix)) {
				deprecatedEntries.put(key, properties.getProperty(key));
			}
		}
		for (final Entry<String, String> entry : deprecatedEntries.entrySet()) {
			properties.remove(entry.getKey());
			properties.setProperty(entry.getKey().replace(deprecatedPrefix, Power.PREFIX), entry.getValue());
		}
	}

	private void writeDefaultBikesCfg(final BikesInf originalBikesInf, final Path destFile) throws IOException {
		final String props = createProperties(originalBikesInf);

		// Salvataggio...
		final Path directory = destFile.getParent();
		if (!directory.toFile().exists()) {
			Files.createDirectories(directory);
			log.log(Level.INFO, messages.get("common.message.directory.created"), directory.toFile().getCanonicalPath());
		}
		try (final Writer writer = Files.newBufferedWriter(destFile, CHARSET)) {
			writer.write(props);
		}
	}

	private static String createProperties(final BikesInf bikesInf) {
		final String lineSeparator = NewLine.SYSTEM_LINE_SEPARATOR;
		final StringBuilder props = new StringBuilder(messages.get("common.str.cfg.header"));

		for (final Bike bike : bikesInf.getBikes()) {
			props.append(lineSeparator).append(lineSeparator);
			props.append("### ").append(bike.getType().getDisplacement()).append(" cc - " + messages.get("common.str.cfg.begin") + "... ###");

			// Settings
			props.append(lineSeparator);
			props.append("# ").append(Settings.class.getSimpleName()).append(" #");
			props.append(lineSeparator);
			for (final Setting setting : bike.getSettings().getValues().keySet()) {
				props.append(buildPropertyKey(bike.getType(), Settings.PREFIX, setting.getKey()));
				props.append('=');
				props.append(bike.getSettings().getValues().get(setting).intValue());
				props.append(lineSeparator);
			}

			// Gearbox
			props.append(lineSeparator);
			props.append("# ").append(Gearbox.class.getSimpleName()).append(" #");
			props.append(lineSeparator);
			for (int index = 0; index < bike.getGearbox().getRatios().length; index++) {
				props.append(buildPropertyKey(bike.getType(), Gearbox.PREFIX, index));
				props.append('=');
				props.append(bike.getGearbox().getRatios()[index]);
				props.append(lineSeparator);
			}

			// Power
			props.append(lineSeparator);
			props.append("# ").append(Power.class.getSimpleName()).append(" (").append(Power.getRpm(0)).append('-').append(Power.getRpm(Power.LENGTH) - 1).append(" RPM) #");
			props.append(lineSeparator);
			for (int index = 0; index < bike.getPower().getCurve().length; index++) {
				if (index > 0 && index % 8 == 0) {
					props.append("# " + Power.getRpm(index) + " RPM");
					props.append(lineSeparator);
				}
				props.append(buildPropertyKey(bike.getType(), Power.PREFIX, index));
				props.append('=');
				props.append(bike.getPower().getCurve()[index]);
				props.append(lineSeparator);
			}

			props.append("### ").append(bike.getType().getDisplacement()).append(" cc - " + messages.get("common.str.cfg.end") + ". ###");
		}

		props.append(lineSeparator).append(lineSeparator);
		props.append(messages.get("common.str.cfg.footer")).append(lineSeparator);
		return props.toString();
	}

	public static String buildPropertyKey(final BikeType bikeType, final String prefix, final String suffix) {
		return Integer.toString(bikeType.getDisplacement()) + '.' + prefix + '.' + suffix;
	}

	public static String buildPropertyKey(final BikeType bikeType, final String prefix, final int suffix) {
		return buildPropertyKey(bikeType, prefix, Integer.toString(suffix));
	}

	public Properties getProperties() {
		return properties;
	}

	public Map<String, Integer> getMap() {
		final Map<String, Integer> map = new HashMap<>();
		for (final String key : properties.stringPropertyNames()) {
			map.put(key, Integer.valueOf(properties.getProperty(key), RADIX));
		}
		return map;
	}

}
