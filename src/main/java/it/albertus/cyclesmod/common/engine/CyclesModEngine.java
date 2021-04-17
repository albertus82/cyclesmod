package it.albertus.cyclesmod.common.engine;

import java.io.Serializable;
import java.util.logging.Level;

import it.albertus.cyclesmod.common.model.Bike;
import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.model.Gearbox;
import it.albertus.cyclesmod.common.model.Power;
import it.albertus.cyclesmod.common.model.Setting;
import it.albertus.cyclesmod.common.model.Settings;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
public abstract class CyclesModEngine implements NumeralSystemProvider {

	private static final String COMMON_ERROR_UNSUPPORTED_PROPERTY = "common.error.unsupported.property";

	private static final Messages messages = CommonMessages.INSTANCE;

	private NumeralSystem numeralSystem = NumeralSystem.DEFAULT;
	private BikesInf bikesInf;

	public static boolean isNumeric(final String value, final int radix) {
		try {
			Long.parseLong(value, radix);
			return true;
		}
		catch (final NumberFormatException e) {
			return false;
		}
	}

	public boolean isNumeric(final String value) {
		return isNumeric(value, numeralSystem.getRadix());
	}

	protected boolean applyProperty(final String key, final String value, final boolean lenient) {
		boolean applied = false;
		try {
			if (value == null || value.trim().length() == 0 || !isNumeric(value.trim())) {
				throw new InvalidPropertyException(messages.get(COMMON_ERROR_UNSUPPORTED_PROPERTY, key, value));
			}

			// Settings
			if (isSettingsProperty(key)) {
				applied = applySettingProperty(key, value);
			}

			// Gearbox
			else if (isGearboxProperty(key)) {
				applied = applyGearboxProperty(key, value);
			}

			// Power
			else if (isPowerProperty(key)) {
				applied = applyPowerProperty(key, value);
			}

			else {
				throw new InvalidPropertyException(messages.get(COMMON_ERROR_UNSUPPORTED_PROPERTY, key, value));
			}
		}
		catch (final InvalidPropertyException e) {
			if (!lenient) {
				throw e;
			}
		}
		return applied;
	}

	public boolean isPowerProperty(final String key) {
		return StringUtils.substringAfter(key, ".").startsWith(Power.PREFIX);
	}

	public boolean isGearboxProperty(final String key) {
		return StringUtils.substringAfter(key, ".").startsWith(Gearbox.PREFIX);
	}

	public boolean isSettingsProperty(final String key) {
		return StringUtils.substringAfter(key, ".").startsWith(Settings.PREFIX);
	}

	private boolean applyPowerProperty(final String key, final String value) {
		boolean applied = false;
		final short newValue = Power.parse(key, value, numeralSystem.getRadix());

		final Bike bike = getBike(key, value);
		final String suffix = StringUtils.substringAfter(key, Power.PREFIX + '.');
		if (StringUtils.isNotEmpty(suffix) && StringUtils.isNumeric(suffix) && Integer.parseInt(suffix) < bike.getPower().getCurve().length) {
			final int index = Integer.parseInt(suffix);
			final short defaultValue = bike.getPower().getCurve()[index];
			if (defaultValue != newValue) {
				bike.getPower().getCurve()[index] = newValue;
				applied = true;
				logChange(key, defaultValue, newValue);
			}
		}
		else {
			throw new InvalidPropertyException(messages.get(COMMON_ERROR_UNSUPPORTED_PROPERTY, key, value));
		}
		return applied;
	}

	private boolean applyGearboxProperty(final String key, final String value) {
		boolean applied = false;
		final int newValue = Gearbox.parse(key, value, numeralSystem.getRadix());

		final Bike bike = getBike(key, value);
		final String suffix = StringUtils.substringAfter(key, Gearbox.PREFIX + '.');
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
			throw new InvalidPropertyException(messages.get(COMMON_ERROR_UNSUPPORTED_PROPERTY, key, value));
		}
		return applied;
	}

	private boolean applySettingProperty(final String key, final String value) {
		boolean applied = false;
		final int newValue = Settings.parse(key, value, numeralSystem.getRadix());

		final Bike bike = getBike(key, value);
		final String suffix = StringUtils.substringAfter(key, Settings.PREFIX + '.');
		final Setting setting = Setting.forKey(suffix);
		if (setting != null) {
			final int defaultValue = bike.getSettings().getValues().get(setting);
			if (newValue != defaultValue) {
				bike.getSettings().getValues().put(setting, newValue);
				applied = true;
				logChange(key, defaultValue, newValue);
			}
		}
		else {
			throw new InvalidPropertyException(messages.get(COMMON_ERROR_UNSUPPORTED_PROPERTY, key, value));
		}
		return applied;
	}

	private void logChange(final String key, final int defaultValue, final int newValue) {
		log.log(Level.INFO, messages.get("common.message.custom.value.detected"), new Serializable[] { key, newValue, String.format("%X", newValue), defaultValue, String.format("%X", defaultValue) });
	}

	private Bike getBike(final String key, final String value) {
		final Bike bike = bikesInf.getBike(Integer.parseInt(StringUtils.substringBefore(key, ".")));
		if (bike == null) {
			throw new InvalidPropertyException(messages.get(COMMON_ERROR_UNSUPPORTED_PROPERTY, key, value));
		}
		return bike;
	}

}
