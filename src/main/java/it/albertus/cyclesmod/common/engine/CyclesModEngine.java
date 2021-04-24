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

	protected boolean applyProperty(final String propertyName, final String value, final boolean lenient) throws UnknownPropertyException, InvalidNumberException, ValueOutOfRangeException {
		boolean applied = false;
		try {
			if (isSettingsProperty(propertyName)) {
				applied = applySettingProperty(propertyName, value);
			}
			else if (isGearboxProperty(propertyName)) {
				applied = applyGearboxProperty(propertyName, value);
			}
			else if (isPowerProperty(propertyName)) {
				applied = applyPowerProperty(propertyName, value);
			}
			else {
				throw new UnknownPropertyException(propertyName);
			}
		}
		catch (final UnknownPropertyException | InvalidNumberException | ValueOutOfRangeException e) {
			if (!lenient) {
				throw e;
			}
		}
		return applied;
	}

	private boolean applyPowerProperty(final String propertyName, final String value) throws InvalidNumberException, ValueOutOfRangeException, UnknownPropertyException {
		boolean applied = false;
		final short newValue = Power.parse(propertyName, value, numeralSystem.getRadix());

		final Bike bike = getBike(propertyName);
		final String suffix = StringUtils.substringAfter(propertyName, Power.PREFIX + '.');
		if (StringUtils.isNotEmpty(suffix) && StringUtils.isNumeric(suffix) && Integer.parseInt(suffix) < bike.getPower().getCurve().length) {
			final int index = Integer.parseInt(suffix);
			final short defaultValue = bike.getPower().getCurve()[index];
			if (defaultValue != newValue) {
				bike.getPower().getCurve()[index] = newValue;
				applied = true;
			}
		}
		else {
			throw new UnknownPropertyException(propertyName);
		}
		return applied;
	}

	private boolean applyGearboxProperty(final String propertyName, final String value) throws ValueOutOfRangeException, InvalidNumberException, UnknownPropertyException {
		boolean applied = false;
		final int newValue = Gearbox.parse(propertyName, value, numeralSystem.getRadix());

		final Bike bike = getBike(propertyName);
		final String suffix = StringUtils.substringAfter(propertyName, Gearbox.PREFIX + '.');
		if (StringUtils.isNotEmpty(suffix) && StringUtils.isNumeric(suffix) && Integer.parseInt(suffix) < bike.getGearbox().getRatios().length) {
			final int index = Integer.parseInt(suffix);
			final int defaultValue = bike.getGearbox().getRatios()[index];
			if (defaultValue != newValue) {
				bike.getGearbox().getRatios()[index] = newValue;
				applied = true;
			}
		}
		else {
			throw new UnknownPropertyException(propertyName);
		}
		return applied;
	}

	private boolean applySettingProperty(final String propertyName, final String value) throws ValueOutOfRangeException, InvalidNumberException, UnknownPropertyException {
		boolean applied = false;
		final int newValue = Settings.parse(propertyName, value, numeralSystem.getRadix());

		final Bike bike = getBike(propertyName);
		final String suffix = StringUtils.substringAfter(propertyName, Settings.PREFIX + '.');
		final Setting setting = Setting.forKey(suffix);
		if (setting != null) {
			final int defaultValue = bike.getSettings().getValues().get(setting);
			if (newValue != defaultValue) {
				bike.getSettings().getValues().put(setting, newValue);
				applied = true;
			}
		}
		else {
			throw new UnknownPropertyException(propertyName);
		}
		return applied;
	}

	private Bike getBike(final String propertyName) throws UnknownPropertyException {
		final int displacement = Integer.parseInt(StringUtils.substringBefore(propertyName, "."));
		final BikeType bikeType = BikeType.forDisplacement(displacement);
		if (bikeType == null) {
			throw new UnknownPropertyException(propertyName);
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

	public static boolean isPowerProperty(final String propertyName) {
		return StringUtils.substringAfter(propertyName, ".").startsWith(Power.PREFIX);
	}

	public static boolean isGearboxProperty(final String propertyName) {
		return StringUtils.substringAfter(propertyName, ".").startsWith(Gearbox.PREFIX);
	}

	public static boolean isSettingsProperty(final String propertyName) {
		return StringUtils.substringAfter(propertyName, ".").startsWith(Settings.PREFIX);
	}

}
