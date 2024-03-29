package io.github.albertus82.cyclesmod.common.engine;

import java.util.Arrays;
import java.util.logging.Level;

import io.github.albertus82.cyclesmod.common.model.Gearbox;
import io.github.albertus82.cyclesmod.common.model.Power;
import io.github.albertus82.cyclesmod.common.model.Setting;
import io.github.albertus82.cyclesmod.common.model.Settings;
import io.github.albertus82.cyclesmod.common.model.Vehicle;
import io.github.albertus82.cyclesmod.common.model.VehicleType;
import io.github.albertus82.cyclesmod.common.model.VehiclesInf;
import io.github.albertus82.util.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
@NoArgsConstructor
public class CyclesModEngine {

	private NumeralSystem numeralSystem = NumeralSystem.DEFAULT;

	private VehiclesInf vehiclesInf;

	public CyclesModEngine(final VehiclesInf vehiclesInf) {
		this.vehiclesInf = vehiclesInf;
	}

	public boolean isNumeric(final String value) {
		return isNumeric(value, numeralSystem.getRadix());
	}

	public boolean applyProperty(final String propertyName, final String value) throws UnknownPropertyException, InvalidNumberException, ValueOutOfRangeException {
		final Vehicle vehicle = getVehicle(propertyName);
		boolean applied = false;
		if (isSettingsProperty(propertyName)) {
			applied = applySettingProperty(propertyName, value, vehicle);
		}
		else if (isGearboxProperty(propertyName)) {
			applied = applyGearboxProperty(propertyName, value, vehicle);
		}
		else if (isPowerProperty(propertyName)) {
			applied = applyPowerProperty(propertyName, value, vehicle);
		}
		else {
			throw new UnknownPropertyException(propertyName);
		}
		return applied;
	}

	private boolean applyPowerProperty(final String propertyName, final String value, @NonNull final Vehicle vehicle) throws InvalidNumberException, ValueOutOfRangeException, UnknownPropertyException {
		boolean applied = false;
		final short newValue = Power.parse(propertyName, value, numeralSystem.getRadix());
		final String suffix = StringUtils.substringAfter(propertyName, Power.PREFIX + '.');
		if (StringUtils.isNotEmpty(suffix) && StringUtils.isNumeric(suffix) && Integer.parseInt(suffix) < vehicle.getPower().getCurve().length) {
			final int index = Integer.parseInt(suffix);
			final short defaultValue = vehicle.getPower().getCurve()[index];
			if (defaultValue != newValue) {
				vehicle.getPower().getCurve()[index] = newValue;
				applied = true;
			}
		}
		else {
			throw new UnknownPropertyException(propertyName);
		}
		return applied;
	}

	private boolean applyGearboxProperty(final String propertyName, final String value, @NonNull final Vehicle vehicle) throws ValueOutOfRangeException, InvalidNumberException, UnknownPropertyException {
		boolean applied = false;
		final int newValue = Gearbox.parse(propertyName, value, numeralSystem.getRadix());
		final String suffix = StringUtils.substringAfter(propertyName, Gearbox.PREFIX + '.');
		if (StringUtils.isNotEmpty(suffix) && StringUtils.isNumeric(suffix) && Integer.parseInt(suffix) < vehicle.getGearbox().getRatios().length) {
			final int index = Integer.parseInt(suffix);
			final int defaultValue = vehicle.getGearbox().getRatios()[index];
			if (defaultValue != newValue) {
				vehicle.getGearbox().getRatios()[index] = newValue;
				applied = true;
			}
		}
		else {
			throw new UnknownPropertyException(propertyName);
		}
		return applied;
	}

	private boolean applySettingProperty(final String propertyName, final String value, @NonNull final Vehicle vehicle) throws ValueOutOfRangeException, InvalidNumberException, UnknownPropertyException {
		boolean applied = false;
		final int newValue = Settings.parse(propertyName, value, numeralSystem.getRadix());
		final String suffix = StringUtils.substringAfter(propertyName, Settings.PREFIX + '.');
		final Setting setting = Setting.forKey(suffix);
		if (setting != null) {
			final int defaultValue = vehicle.getSettings().getValues().get(setting);
			if (newValue != defaultValue) {
				vehicle.getSettings().getValues().put(setting, newValue);
				applied = true;
			}
		}
		else {
			throw new UnknownPropertyException(propertyName);
		}
		return applied;
	}

	private Vehicle getVehicle(final String propertyName) throws UnknownPropertyException {
		final String prefix = StringUtils.substringBefore(propertyName, ".");
		switch (vehiclesInf.getGame()) {
		case CYCLES:
			try {
				return vehiclesInf.getVehicles().get(VehicleType.forDisplacement(Integer.parseInt(prefix)));
			}
			catch (final IllegalArgumentException e) {
				throw new UnknownPropertyException(propertyName, e);
			}
		case GPC:
			try {
				return vehiclesInf.getVehicles().get(VehicleType.forTeam(prefix));
			}
			catch (final IllegalArgumentException e) {
				throw new UnknownPropertyException(propertyName, e);
			}
		default:
			throw new IllegalArgumentException("Unknown or unsupported game: " + vehiclesInf.getGame());
		}
	}

	public static boolean isNumeric(final String value, final int radix) {
		try {
			Long.parseLong(value, radix);
			return true;
		}
		catch (final NumberFormatException e) {
			log.log(Level.FINEST, e, () -> Arrays.toString(new Object[] { value, radix }));
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
