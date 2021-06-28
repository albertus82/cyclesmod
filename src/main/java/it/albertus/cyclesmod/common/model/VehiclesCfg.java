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
public class VehiclesCfg {

	public static final Charset CHARSET = StandardCharsets.ISO_8859_1;

	private static final int RADIX = 10;

	private static final Messages messages = CommonMessages.INSTANCE;

	@Getter
	private final Properties properties = new Properties();

	/**
	 * Creates a new instance containing the provided configuration.
	 * 
	 * @param vehiclesInf the configuration to map
	 */
	public VehiclesCfg(@NonNull final Game game, @NonNull final VehiclesInf vehiclesInf) {
		this(game, vehiclesInf.getVehicles().values().toArray(new Vehicle[0]));
	}

	/**
	 * Creates a new instance containing the provided vehicle configurations.
	 * 
	 * @param vehicles the vehicle configurations to map
	 */
	public VehiclesCfg(@NonNull final Game game, final Vehicle... vehicles) {
		try (final StringReader reader = new StringReader(createProperties(game, vehicles))) {
			populateProperties(reader);
		}
		catch (final IOException e) {
			throw new UncheckedIOException(e); // No exception possible with StringReader!
		}
	}

	/**
	 * Creates a new instance reading the values from the provided CFG file.
	 * 
	 * @param sourceCfgFile the file to read
	 */
	public VehiclesCfg(@NonNull final Path sourceCfgFile) throws IOException {
		try (final Reader reader = Files.newBufferedReader(sourceCfgFile, CHARSET)) {
			populateProperties(reader);
		}
	}

	private void populateProperties(@NonNull final Reader reader) throws IOException {
		properties.load(reader);
		manageDeprecatedProperties();
	}

	public static void writeDefault(@NonNull final Game game, @NonNull final Path destCfgFile) throws IOException {
		final String props = createProperties(game, new VehiclesInf(game).getVehicles().values().toArray(new Vehicle[0]));

		// Salvataggio...
		try (final Writer writer = Files.newBufferedWriter(destCfgFile, CHARSET)) {
			writer.write(props);
		}
	}

	public static String createProperties(@NonNull final Game game, final Vehicle... vehicles) {
		final String lineSeparator = System.lineSeparator();
		final StringBuilder props = new StringBuilder(messages.get("common.string.vehicles.cfg.header." + game.toString().toLowerCase(Locale.ROOT)));

		for (final Vehicle vehicle : vehicles) {
			props.append(lineSeparator).append(lineSeparator);
			props.append(messages.get("common.string.vehicles.cfg.begin", vehicle.getType().getDescription(game)));

			// Settings
			props.append(lineSeparator);
			props.append("# ").append(Settings.class.getSimpleName()).append(" #");
			props.append(lineSeparator);
			for (final Entry<Setting, Integer> entry : vehicle.getSettings().getValues().entrySet()) {
				props.append(buildPropertyKey(game, vehicle.getType(), Settings.PREFIX, entry.getKey().getKey()));
				props.append('=');
				props.append(entry.getValue().intValue());
				props.append(lineSeparator);
			}

			// Gearbox
			props.append(lineSeparator);
			props.append("# ").append(Gearbox.class.getSimpleName()).append(" #");
			props.append(lineSeparator);
			for (int index = 0; index < vehicle.getGearbox().getRatios().length; index++) {
				props.append(buildPropertyKey(game, vehicle.getType(), Gearbox.PREFIX, index));
				props.append('=');
				props.append(vehicle.getGearbox().getRatios()[index]);
				props.append(lineSeparator);
			}

			// Power
			props.append(lineSeparator);
			props.append("# ").append(Power.class.getSimpleName()).append(" (").append(Power.getRpm(0)).append('-').append(Power.getRpm(Power.LENGTH) - 1).append(" RPM) #");
			props.append(lineSeparator);
			for (int index = 0; index < vehicle.getPower().getCurve().length; index++) {
				if (index > 0 && index % 8 == 0) {
					props.append("# ").append(Power.getRpm(index)).append(" RPM");
					props.append(lineSeparator);
				}
				props.append(buildPropertyKey(game, vehicle.getType(), Power.PREFIX, index));
				props.append('=');
				props.append(vehicle.getPower().getCurve()[index]);
				props.append(lineSeparator);
			}

			props.append(messages.get("common.string.vehicles.cfg.end", vehicle.getType().getDescription(game)));
		}

		props.append(lineSeparator).append(lineSeparator);
		props.append(messages.get("common.string.vehicles.cfg.footer")).append(lineSeparator);
		return props.toString();
	}

	public static String buildPropertyKey(@NonNull final Game game, @NonNull final VehicleType vehicleType, @NonNull final String prefix, @NonNull final String suffix) {
		return vehicleType.getKey(game) + "." + prefix + "." + suffix;
	}

	public static String buildPropertyKey(@NonNull final Game game, final VehicleType vehicleType, final String prefix, final int suffix) {
		return buildPropertyKey(game, vehicleType, prefix, Integer.toString(suffix));
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
		replacements.put(".unknown1", '.' + Setting.GRIP_0.getKey());
		replacements.put(".unknown2", '.' + Setting.BRAKING_SPEED_0.getKey());
		replacements.put(".unknown3", '.' + Setting.SPIN_THRESHOLD_0.getKey());

		for (final String key : properties.stringPropertyNames()) {
			for (final Entry<String, String> replacement : replacements.entrySet()) {
				if (key.toLowerCase(Locale.ROOT).contains(replacement.getKey().toLowerCase(Locale.ROOT))) {
					log.log(Level.FINE, "{0} -> {1}", new String[] { replacement.getKey(), replacement.getValue() });
					properties.setProperty(key.replaceAll("(?i)" + Pattern.quote(replacement.getKey()), replacement.getValue()), properties.remove(key).toString());
					break;
				}
			}
		}
	}

	public static String getFileName(@NonNull final Game game) {
		switch (game) {
		case CYCLES:
			return "BIKES.CFG";
		case GPC:
			return "CARS.CFG";
		default:
			throw new IllegalArgumentException("Unknown game: " + game);
		}
	}

	public static String getFileName(@NonNull final Game game, @NonNull final VehicleType vehicleType) {
		switch (game) {
		case CYCLES:
			return "BIKE" + (vehicleType.getIndex() + 1) + ".CFG";
		case GPC:
			return "CAR" + (vehicleType.getIndex() + 1) + ".CFG";
		default:
			throw new IllegalArgumentException("Unknown game: " + game);
		}
	}

}
