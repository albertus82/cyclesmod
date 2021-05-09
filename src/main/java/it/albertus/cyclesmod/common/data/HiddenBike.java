package it.albertus.cyclesmod.common.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

import it.albertus.cyclesmod.common.model.BikeType;
import it.albertus.cyclesmod.common.model.Gearbox;
import it.albertus.cyclesmod.common.model.Power;
import it.albertus.cyclesmod.common.model.Setting;
import it.albertus.cyclesmod.common.model.Settings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HiddenBike {

	private static final String RESOURCE_NAME = "hiddenbike.properties";

	public static Properties getProperties(@NonNull final BikeType target) {
		final Properties internal = new Properties();
		try (final InputStream is = HiddenBike.class.getResourceAsStream(RESOURCE_NAME)) {
			if (is != null) {
				internal.load(is);
			}
			else {
				throw new FileNotFoundException(RESOURCE_NAME);
			}
		}
		catch (final IOException e) {
			throw new UncheckedIOException("Cannot load resource " + '/' + HiddenBike.class.getPackage().getName().replace('.', '/') + RESOURCE_NAME, e);
		}

		final Properties properties = new Properties();

		final String[] settings = internal.getProperty(Settings.PREFIX).split(",");
		if (settings.length != Settings.LENGTH / 2) {
			throw new VerifyError("Invalid " + Settings.PREFIX + " array length, expected " + Settings.LENGTH / 2 + " but was " + settings.length);
		}
		for (final Setting setting : Setting.values()) {
			properties.setProperty(target.getDisplacement() + "." + Settings.PREFIX + "." + setting.getKey(), settings[setting.getIndex()]);
		}

		final String[] gearbox = internal.getProperty(Gearbox.PREFIX).split(",");
		if (gearbox.length != Gearbox.LENGTH / 2) {
			throw new VerifyError("Invalid " + Gearbox.PREFIX + " array length, expected " + Gearbox.LENGTH / 2 + " but was " + gearbox.length);
		}
		for (int i = 0; i < gearbox.length; i++) {
			properties.setProperty(target.getDisplacement() + "." + Gearbox.PREFIX + "." + i, gearbox[i]);
		}

		final String[] power = internal.getProperty(Power.PREFIX).split(",");
		if (power.length != Power.LENGTH) {
			throw new VerifyError("Invalid " + Power.PREFIX + " array length, expected " + Power.LENGTH + " but was " + power.length);
		}
		for (int i = 0; i < power.length; i++) {
			properties.setProperty(target.getDisplacement() + "." + Power.PREFIX + "." + i, power[i]);
		}

		return properties;
	}

}
