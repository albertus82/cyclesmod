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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import lombok.extern.java.Log;

@Log
public class BikesCfg {

	public static final String FILE_NAME = "BIKES.CFG";

	private static final int RADIX = 10;
	private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

	private static final Messages messages = CommonMessages.INSTANCE;

	private final Properties properties = new Properties();

	/**
	 * Creates a new instance mapping the provided configuration.
	 * 
	 * @param bikesInf the configuration to map
	 */
	public BikesCfg(final BikesInf bikesInf) {
		try (final StringReader reader = new StringReader(createProperties(bikesInf))) {
			populateProperties(reader);
		}
		catch (final IOException e) {
			throw new UncheckedIOException(e); // No exception possible with StringReader!
		}
	}

	/**
	 * Creates a new instance reading the values from the provided CFG file.
	 * 
	 * @param bikesCfgFile the file to read
	 */
	public BikesCfg(final Path bikesCfgFile) throws IOException {
		log.log(Level.FINE, messages.get("common.message.reading.file"), FILE_NAME);
		try (final Reader reader = Files.newBufferedReader(bikesCfgFile, CHARSET)) {
			populateProperties(reader);
		}
		log.log(Level.FINE, messages.get("common.message.file.read"), FILE_NAME);
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

	public static void writeDefault(final Path file) throws IOException {
		final String props = createProperties(new BikesInf());

		// Salvataggio...
		try (final Writer writer = Files.newBufferedWriter(file, CHARSET)) {
			writer.write(props);
		}
	}

	private static String createProperties(final BikesInf bikesInf) {
		final String lineSeparator = System.lineSeparator();
		final StringBuilder props = new StringBuilder(messages.get("common.str.cfg.header"));

		for (final Bike bike : bikesInf.getBikeMap().values()) {
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
