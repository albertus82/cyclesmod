package it.albertus.cyclesmod.model;

import java.beans.Introspector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import it.albertus.cyclesmod.model.Bike.BikeType;
import it.albertus.cyclesmod.resources.Messages;
import it.albertus.util.NewLine;

public class BikesCfg {

	private static final String FILE_NAME = "BIKES.CFG";
	private static final int RADIX = 10;

	private final Properties properties = new Properties();

	public BikesCfg(final BikesInf bikesInf) {
		try (final StringReader reader = new StringReader(createProperties(bikesInf))) {
			populateProperties(reader);
		}
		catch (final IOException e) {
			throw new UncheckedIOException(e); // No exception possible with StringReader!
		}
	}

	public BikesCfg(final String fileName) throws IOException {
		try (final FileReader fr = new FileReader(fileName)) {
			populateProperties(fr); // buffered internally
		}
		System.out.println(Messages.get("msg.file.read", FILE_NAME));
	}

	public BikesCfg(final BikesInf originalBikesInf, final String path) throws IOException {
		System.out.println(Messages.get("msg.reading.file", FILE_NAME));
		final File file = new File(path + FILE_NAME);
		if (!file.exists()) {
			System.out.println(Messages.get("msg.file.not.found.creating.default", FILE_NAME));
			writeDefaultBikesCfg(originalBikesInf, file);
			System.out.println(Messages.get("msg.default.file.created", FILE_NAME));
		}
		try (final FileReader fr = new FileReader(file)) {
			populateProperties(fr); // buffered internally
		}
		System.out.println(Messages.get("msg.file.read", FILE_NAME));
	}

	private void populateProperties(final Reader reader) throws IOException {
		this.properties.load(reader);
	}

	private void writeDefaultBikesCfg(final BikesInf originalBikesInf, final File destination) throws IOException {
		final String props = createProperties(originalBikesInf);

		// Salvataggio...
		try (FileWriter fw = new FileWriter(destination); BufferedWriter bw = new BufferedWriter(fw)) {
			bw.write(props);
		}
	}

	private String createProperties(final BikesInf bikesInf) {
		final String lineSeparator = NewLine.SYSTEM_LINE_SEPARATOR;
		final StringBuilder props = new StringBuilder(Messages.get("str.cfg.header"));

		for (final Bike bike : bikesInf.getBikes()) {
			props.append(lineSeparator).append(lineSeparator);
			props.append("### ").append(bike.getType().getDisplacement()).append(" cc - " + Messages.get("str.cfg.begin") + "... ###");

			// Settings
			props.append(lineSeparator);
			props.append("# ").append(Settings.class.getSimpleName()).append(" #");
			props.append(lineSeparator);
			for (final Setting setting : bike.getSettings().getValues().keySet()) {
				props.append(buildPropertyKey(bike.getType(), Settings.class, setting.getKey()));
				props.append('=');
				props.append(bike.getSettings().getValues().get(setting).intValue());
				props.append(lineSeparator);
			}

			// Gearbox
			props.append(lineSeparator);
			props.append("# ").append(Gearbox.class.getSimpleName()).append(" #");
			props.append(lineSeparator);
			for (int index = 0; index < bike.getGearbox().getRatios().length; index++) {
				props.append(buildPropertyKey(bike.getType(), Gearbox.class, index));
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
				props.append(buildPropertyKey(bike.getType(), Power.class, index));
				props.append('=');
				props.append(bike.getPower().getCurve()[index]);
				props.append(lineSeparator);
			}

			props.append("### ").append(bike.getType().getDisplacement()).append(" cc - " + Messages.get("str.cfg.end") + ". ###");
		}

		props.append(lineSeparator).append(lineSeparator);
		props.append(Messages.get("str.cfg.footer")).append(lineSeparator);
		return props.toString();
	}

	public static String buildPropertyKey(final BikeType bikeType, final Class<? extends BikesInfElement> propertyType, final String suffix) {
		return Integer.toString(bikeType.getDisplacement()) + '.' + Introspector.decapitalize(propertyType.getSimpleName()) + '.' + suffix;
	}

	public static String buildPropertyKey(final BikeType bikeType, final Class<? extends BikesInfElement> propertyType, final int suffix) {
		return buildPropertyKey(bikeType, propertyType, Integer.toString(suffix));
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
