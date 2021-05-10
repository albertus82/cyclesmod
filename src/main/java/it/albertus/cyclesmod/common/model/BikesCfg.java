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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;

import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class BikesCfg {

	public static final String FILE_NAME = "BIKES.CFG";
	public static final Charset CHARSET = StandardCharsets.ISO_8859_1;

	private static final int RADIX = 10;

	private static final Messages messages = CommonMessages.INSTANCE;

	@Getter private final Properties properties = new Properties();

	/**
	 * Creates a new instance containing the provided configuration.
	 * 
	 * @param bikesInf the configuration to map
	 */
	public BikesCfg(@NonNull final BikesInf bikesInf) {
		this(bikesInf.getBikes().values().toArray(new Bike[0]));
	}

	/**
	 * Creates a new instance containing the provided bike configurations.
	 * 
	 * @param bikes the bike configurations to map
	 */
	public BikesCfg(final Bike... bikes) {
		try (final StringReader reader = new StringReader(createProperties(bikes))) {
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
	public BikesCfg(@NonNull final Path bikesCfgFile) throws IOException {
		try (final Reader reader = Files.newBufferedReader(bikesCfgFile, CHARSET)) {
			populateProperties(reader);
		}
	}

	private void populateProperties(@NonNull final Reader reader) throws IOException {
		properties.load(reader);
		manageDeprecatedProperties();
	}

	public static void writeDefault(@NonNull final Path file) throws IOException {
		final String props = createProperties(new BikesInf().getBikes().values().toArray(new Bike[0]));

		// Salvataggio...
		try (final Writer writer = Files.newBufferedWriter(file, CHARSET)) {
			writer.write(props);
		}
	}

	public static String createProperties(final Bike... bikes) {
		final String lineSeparator = System.lineSeparator();
		final StringBuilder props = new StringBuilder(messages.get("common.string.bikes.cfg.header"));

		for (final Bike bike : bikes) {
			props.append(lineSeparator).append(lineSeparator);
			props.append("### ").append(bike.getType().getDisplacement()).append(" cc - ").append(messages.get("common.string.bikes.cfg.begin")).append("... ###");

			// Settings
			props.append(lineSeparator);
			props.append("# ").append(Settings.class.getSimpleName()).append(" #");
			props.append(lineSeparator);
			for (final Entry<Setting, Integer> entry : bike.getSettings().getValues().entrySet()) {
				props.append(buildPropertyKey(bike.getType(), Settings.PREFIX, entry.getKey().getKey()));
				props.append('=');
				props.append(entry.getValue().intValue());
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
					props.append("# ").append(Power.getRpm(index)).append(" RPM");
					props.append(lineSeparator);
				}
				props.append(buildPropertyKey(bike.getType(), Power.PREFIX, index));
				props.append('=');
				props.append(bike.getPower().getCurve()[index]);
				props.append(lineSeparator);
			}

			props.append("### ").append(bike.getType().getDisplacement()).append(" cc - ").append(messages.get("common.string.bikes.cfg.end")).append(". ###");
		}

		props.append(lineSeparator).append(lineSeparator);
		props.append(messages.get("common.string.bikes.cfg.footer")).append(lineSeparator);
		return props.toString();
	}

	public static String buildPropertyKey(final BikeType bikeType, final String prefix, final String suffix) {
		return bikeType.getDisplacement() + "." + prefix + "." + suffix;
	}

	public static String buildPropertyKey(final BikeType bikeType, final String prefix, final int suffix) {
		return buildPropertyKey(bikeType, prefix, Integer.toString(suffix));
	}

	public Map<String, Integer> getMap() {
		final Map<String, Integer> map = new HashMap<>();
		for (final String key : properties.stringPropertyNames()) {
			map.put(key, Integer.valueOf(properties.getProperty(key), RADIX));
		}
		return map;
	}

	private void manageDeprecatedProperties() {
		final Map<String, String> replacements = new HashMap<>();
		replacements.put(".torque.", '.' + Power.PREFIX + '.');
		replacements.put(".overspeedGracePeriod", '.' + Setting.OVERREV_TOLERANCE.getKey());

		for (final String key : properties.stringPropertyNames()) {
			for (final Entry<String, String> replacement : replacements.entrySet()) {
				if (key.toLowerCase(Locale.ROOT).contains(replacement.getKey().toLowerCase(Locale.ROOT))) {
					log.log(Level.INFO, "{0} -> {1}", new String[] { replacement.getKey(), replacement.getValue() });
					properties.setProperty(key.replaceAll("(?i)" + Pattern.quote(replacement.getKey()), replacement.getValue()), properties.remove(key).toString());
					break;
				}
			}
		}
	}

}
