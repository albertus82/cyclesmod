package it.albertus.cyclesmod.common.engine;

import it.albertus.cyclesmod.common.model.Bike;
import it.albertus.cyclesmod.common.model.BikeType;
import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.model.Gearbox;
import it.albertus.cyclesmod.common.model.Power;
import it.albertus.cyclesmod.common.model.Setting;
import it.albertus.cyclesmod.common.model.Settings;
import it.albertus.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CyclesModEngine implements NumeralSystemProvider {

	private NumeralSystem numeralSystem = NumeralSystem.DEFAULT;

	private BikesInf bikesInf;

	public boolean isNumeric(final String value) {
		return isNumeric(value, numeralSystem.getRadix());
	}

	protected boolean applyProperty(final String key, final String value, final boolean lenient) throws UnknownPropertyException, InvalidNumberException, ValueOutOfRangeException {
		boolean applied = false;
		try {
			if (isSettingsProperty(key)) {
				applied = applySettingProperty(key, value);
			}
			else if (isGearboxProperty(key)) {
				applied = applyGearboxProperty(key, value);
			}
			else if (isPowerProperty(key)) {
				applied = applyPowerProperty(key, value);
			}
			else {
				throw new UnknownPropertyException(key);
			}
		}
		catch (final UnknownPropertyException | InvalidNumberException | ValueOutOfRangeException e) {
			if (!lenient) {
				throw e;
			}
		}
		return applied;
	}

	private boolean applyPowerProperty(final String key, final String value) throws InvalidNumberException, ValueOutOfRangeException, UnknownPropertyException {
		boolean applied = false;
		final short newValue = Power.parse(key, value, numeralSystem.getRadix());

		final Bike bike = getBike(key);
		final String suffix = StringUtils.substringAfter(key, Power.PREFIX + '.');
		if (StringUtils.isNotEmpty(suffix) && StringUtils.isNumeric(suffix) && Integer.parseInt(suffix) < bike.getPower().getCurve().length) {
			final int index = Integer.parseInt(suffix);
			final short defaultValue = bike.getPower().getCurve()[index];
			if (defaultValue != newValue) {
				bike.getPower().getCurve()[index] = newValue;
				applied = true;
			}
		}
		else {
			throw new UnknownPropertyException(key);
		}
		return applied;
	}

	private boolean applyGearboxProperty(final String key, final String value) throws ValueOutOfRangeException, InvalidNumberException, UnknownPropertyException {
		boolean applied = false;
		final int newValue = Gearbox.parse(key, value, numeralSystem.getRadix());

		final Bike bike = getBike(key);
		final String suffix = StringUtils.substringAfter(key, Gearbox.PREFIX + '.');
		if (StringUtils.isNotEmpty(suffix) && StringUtils.isNumeric(suffix) && Integer.parseInt(suffix) < bike.getGearbox().getRatios().length) {
			final int index = Integer.parseInt(suffix);
			final int defaultValue = bike.getGearbox().getRatios()[index];
			if (defaultValue != newValue) {
				bike.getGearbox().getRatios()[index] = newValue;
				applied = true;
			}
		}
		else {
			throw new UnknownPropertyException(key);
		}
		return applied;
	}

	private boolean applySettingProperty(final String key, final String value) throws ValueOutOfRangeException, InvalidNumberException, UnknownPropertyException {
		boolean applied = false;
		final int newValue = Settings.parse(key, value, numeralSystem.getRadix());

		final Bike bike = getBike(key);
		final String suffix = StringUtils.substringAfter(key, Settings.PREFIX + '.');
		final Setting setting = Setting.forKey(suffix);
		if (setting != null) {
			final int defaultValue = bike.getSettings().getValues().get(setting);
			if (newValue != defaultValue) {
				bike.getSettings().getValues().put(setting, newValue);
				applied = true;
			}
		}
		else {
			throw new UnknownPropertyException(key);
		}
		return applied;
	}

	private Bike getBike(final String key) throws UnknownPropertyException {
		final int displacement = Integer.parseInt(StringUtils.substringBefore(key, "."));
		final BikeType bikeType = BikeType.forDisplacement(displacement);
		if (bikeType == null) {
			throw new UnknownPropertyException(key);
		}
		return bikesInf.getBikeMap().get(bikeType);
	}

	public static boolean isNumeric(final String value, final int radix) {
		try {
			Long.parseLong(value, radix);
			return true;
		}
		catch (final NumberFormatException e) {
			return false;
		}
	}

	public static boolean isPowerProperty(final String key) {
		return StringUtils.substringAfter(key, ".").startsWith(Power.PREFIX);
	}

	public static boolean isGearboxProperty(final String key) {
		return StringUtils.substringAfter(key, ".").startsWith(Gearbox.PREFIX);
	}

	public static boolean isSettingsProperty(final String key) {
		return StringUtils.substringAfter(key, ".").startsWith(Settings.PREFIX);
	}

}
