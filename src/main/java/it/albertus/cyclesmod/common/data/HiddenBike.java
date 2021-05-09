package it.albertus.cyclesmod.common.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

import it.albertus.cyclesmod.common.model.BikeType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HiddenBike {

	private static final String RESOURCE_NAME = "hiddenbike.properties";

	private static final Properties internal = createProperties();

	private static Properties createProperties() {
		final Properties properties = new Properties();
		try (final InputStream is = HiddenBike.class.getResourceAsStream(RESOURCE_NAME)) {
			if (is != null) {
				properties.load(is);
			}
			else {
				throw new FileNotFoundException(RESOURCE_NAME);
			}
		}
		catch (final IOException e) {
			throw new UncheckedIOException("Cannot load resource " + '/' + HiddenBike.class.getPackage().getName().replace('.', '/') + RESOURCE_NAME, e);
		}
		return properties;
	}

	public static Properties getProperties(final BikeType target) {
		final Properties properties = new Properties();
		
		return properties;
	}

}
