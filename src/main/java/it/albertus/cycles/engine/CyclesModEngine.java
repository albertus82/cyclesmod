package it.albertus.cycles.engine;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Resources;

import java.beans.Introspector;

import org.apache.commons.lang.StringUtils;

public abstract class CyclesModEngine {

	private NumeralSystem numeralSystem = NumeralSystem.DEFAULT;

	public NumeralSystem getNumeralSystem() {
		return numeralSystem;
	}

	public void setNumeralSystem(NumeralSystem numeralSystem) {
		this.numeralSystem = numeralSystem;
	}

	private BikesInf bikesInf;

	public BikesInf getBikesInf() {
		return bikesInf;
	}

	public void setBikesInf(BikesInf bikesInf) {
		this.bikesInf = bikesInf;
	}

	public static boolean isNumeric(final String value, final int radix) {
		try {
			Long.parseLong(value, radix);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean isNumeric(final String value) {
		return isNumeric(value, numeralSystem.getRadix());
	}

	protected boolean applyProperty(String key, String value, boolean lenient) {
		boolean applied = false;
		try {
			if (StringUtils.isBlank(value) || !isNumeric(value)) {
				throw new InvalidPropertyException(Resources.get("err.unsupported.property", key, value));
			}

			// Settings
			if (isSettingsProperty(key)) {
				applied = applySettingProperty(key, value);
			}

			// Gearbox
			else if (isGearboxProperty(key)) {
				applied = applyGearboxProperty(key, value);
			}

			// Torque
			else if (isTorqueProperty(key)) {
				applied = applyTorqueProperty(key, value);
			}

			else {
				throw new InvalidPropertyException(Resources.get("err.unsupported.property", key, value));
			}
		}
		catch (InvalidPropertyException ipe) {
			if (!lenient) {
				throw ipe;
			}
		}
		return applied;
	}

	protected boolean isTorqueProperty(String key) {
		return StringUtils.substringAfter(key, ".").startsWith(Introspector.decapitalize(Torque.class.getSimpleName()));
	}

	protected boolean isGearboxProperty(String key) {
		return StringUtils.substringAfter(key, ".").startsWith(Introspector.decapitalize(Gearbox.class.getSimpleName()));
	}

	protected boolean isSettingsProperty(String key) {
		return StringUtils.substringAfter(key, ".").startsWith(Introspector.decapitalize(Settings.class.getSimpleName()));
	}

	private boolean applyTorqueProperty(final String key, final String value) {
		boolean applied = false;
		final short newValue = Torque.parse(key, value, numeralSystem.getRadix());

		final Bike bike = getBike(key, value);
		final String suffix = StringUtils.substringAfter(key, Introspector.decapitalize(Torque.class.getSimpleName()) + '.');
		if (StringUtils.isNotEmpty(suffix) && StringUtils.isNumeric(suffix) && Integer.parseInt(suffix) < bike.getTorque().getCurve().length) {
			final int index = Integer.parseInt(suffix);
			final short defaultValue = bike.getTorque().getCurve()[index];
			if (defaultValue != newValue) {
				bike.getTorque().getCurve()[index] = newValue;
				applied = true;
				logChange(key, defaultValue, newValue);
			}
		}
		else {
			throw new InvalidPropertyException(Resources.get("err.unsupported.property", key, value));
		}
		return applied;
	}

	private boolean applyGearboxProperty(final String key, final String value) {
		boolean applied = false;
		final int newValue = Gearbox.parse(key, value, numeralSystem.getRadix());

		final Bike bike = getBike(key, value);
		final String suffix = StringUtils.substringAfter(key, Introspector.decapitalize(Gearbox.class.getSimpleName()) + '.');
		if (StringUtils.isNotEmpty(suffix) && StringUtils.isNumeric(suffix) && Integer.parseInt(suffix) < bike.getGearbox().getRatios().length) {
			final int index = Integer.parseInt(suffix);
			final int defaultValue = bike.getGearbox().getRatios()[index];
			if (defaultValue != newValue) {
				bike.getGearbox().getRatios()[index] = newValue;
				applied = true;
				logChange(key, defaultValue, newValue);
			}
		}
		else {
			throw new InvalidPropertyException(Resources.get("err.unsupported.property", key, value));
		}
		return applied;
	}

	private boolean applySettingProperty(final String key, final String value) {
		boolean applied = false;
		final int newValue = Settings.parse(key, value, numeralSystem.getRadix());

		final Bike bike = getBike(key, value);
		final String suffix = StringUtils.substringAfter(key, Introspector.decapitalize(Settings.class.getSimpleName()) + '.');
		final Setting setting = Setting.getSetting(suffix);
		if (setting != null) {
			final int defaultValue = bike.getSettings().getValues().get(setting);
			if (newValue != defaultValue) {
				bike.getSettings().getValues().put(setting, newValue);
				applied = true;
				logChange(key, defaultValue, newValue);
			}
		}
		else {
			throw new InvalidPropertyException(Resources.get("err.unsupported.property", key, value));
		}
		return applied;
	}

	private void logChange(final String key, final int defaultValue, final int newValue) {
		System.out.println(Resources.get("msg.custom.value.detected", key, newValue, String.format("%X", newValue), defaultValue, String.format("%X", defaultValue)));
	}

	private Bike getBike(final String key, final String value) {
		final Bike bike = bikesInf.getBike(Integer.parseInt(StringUtils.substringBefore(key, ".")));
		if (bike == null) {
			throw new InvalidPropertyException(Resources.get("err.unsupported.property", key, value));
		}
		return bike;
	}

}
