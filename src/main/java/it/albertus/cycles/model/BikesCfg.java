package it.albertus.cycles.model;

import it.albertus.cycles.resources.Resources;
import it.albertus.util.NewLine;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BikesCfg {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String FILE_NAME = "BIKES.CFG";
	private static final int RADIX = 10;

	private final Properties properties = new Properties();

	public BikesCfg(final BikesInf bikesInf) {
		StringReader reader = new StringReader(createProperties(bikesInf));
		try {
			populateProperties(reader);
		}
		catch (IOException ioe) {} // No exception possible with StringReader!
	}

	private void populateProperties(Reader reader) throws IOException {
		this.properties.load(reader);
		reader.close();
	}

	public BikesCfg(final String fileName) throws IOException {
		populateProperties(new BufferedReader(new FileReader(fileName)));
		System.out.println(Resources.get("msg.file.read", FILE_NAME));
	}

	public BikesCfg(final BikesInf originalBikesInf, final String path) throws IOException {
		System.out.println(Resources.get("msg.reading.file", FILE_NAME));
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path + FILE_NAME));
		}
		catch (FileNotFoundException fnfe) {
			System.out.println(Resources.get("msg.file.not.found.creating.default", FILE_NAME));
			writeDefaultBikesCfg(originalBikesInf, path);
			System.out.println(Resources.get("msg.default.file.created", FILE_NAME));
			reader = new BufferedReader(new FileReader(path + FILE_NAME));
		}
		populateProperties(reader);
		System.out.println(Resources.get("msg.file.read", FILE_NAME));
	}

	private void writeDefaultBikesCfg(final BikesInf originalBikesInf, final String path) throws IOException {
		final String properties = createProperties(originalBikesInf);

		// Salvataggio...
		BufferedWriter bw = new BufferedWriter(new FileWriter(path + FILE_NAME));
		bw.write(properties.toString());
		bw.flush();
		bw.close();
	}

	private String createProperties(final BikesInf bikesInf) {
		final String lineSeparator = LINE_SEPARATOR != null ? LINE_SEPARATOR : NewLine.CRLF.toString();
		final StringBuilder properties = new StringBuilder(Resources.get("str.cfg.header"));

		for (Bike bike : bikesInf.getBikes()) {
			properties.append(lineSeparator).append(lineSeparator);
			properties.append("### ").append(bike.getType().getDisplacement()).append(" cc - " + Resources.get("str.cfg.begin") + "... ###");

			// Settings
			properties.append(lineSeparator);
			properties.append("# ").append(Settings.class.getSimpleName()).append(" #");
			properties.append(lineSeparator);
			for (Setting setting : bike.getSettings().getValues().keySet()) {
				properties.append(buildPropertyKey(bike.getType(), Settings.class, setting.toString()));
				properties.append('=');
				properties.append(bike.getSettings().getValues().get(setting).intValue());
				properties.append(lineSeparator);
			}

			// Gearbox
			properties.append(lineSeparator);
			properties.append("# ").append(Gearbox.class.getSimpleName()).append(" #");
			properties.append(lineSeparator);
			for (int index = 0; index < bike.getGearbox().getRatios().length; index++) {
				properties.append(buildPropertyKey(bike.getType(), Gearbox.class, index));
				properties.append('=');
				properties.append(bike.getGearbox().getRatios()[index]);
				properties.append(lineSeparator);
			}

			// Torque
			properties.append(lineSeparator);
			properties.append("# ").append(Torque.class.getSimpleName()).append(" (").append(Torque.getRpm(0)).append('-').append(Torque.getRpm(Torque.LENGTH) - 1).append(" RPM) #");
			properties.append(lineSeparator);
			for (int index = 0; index < bike.getTorque().getCurve().length; index++) {
				if (index > 0 && index % 8 == 0) {
					properties.append("# " + Torque.getRpm(index) + " RPM");
					properties.append(lineSeparator);
				}
				properties.append(buildPropertyKey(bike.getType(), Torque.class, index));
				properties.append('=');
				properties.append(bike.getTorque().getCurve()[index]);
				properties.append(lineSeparator);
			}

			properties.append("### ").append(bike.getType().getDisplacement()).append(" cc - " + Resources.get("str.cfg.end") + ". ###");
		}

		properties.append(lineSeparator).append(lineSeparator);
		properties.append(Resources.get("str.cfg.footer"));
		return properties.toString();
	}

	public static String buildPropertyKey(final Bike.Type bikeType, final Class<? extends BikesInfElement> propertyType, final String suffix) {
		return Integer.toString(bikeType.getDisplacement()) + '.' + Introspector.decapitalize(propertyType.getSimpleName()) + '.' + suffix;
	}

	public static String buildPropertyKey(final Bike.Type bikeType, final Class<? extends BikesInfElement> propertyType, final int suffix) {
		return buildPropertyKey(bikeType, propertyType, Integer.toString(suffix));
	}

	public Properties getProperties() {
		return properties;
	}

	public Map<String, Integer> getMap() {
		final Map<String, Integer> map = new HashMap<String, Integer>();
		for (final String key : properties.stringPropertyNames()) {
			map.put(key, Integer.valueOf(properties.getProperty(key), RADIX));
		}
		return map;
	}

}
