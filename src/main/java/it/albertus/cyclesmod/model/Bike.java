package it.albertus.cyclesmod.model;

import static it.albertus.util.ByteUtils.toInt;
import static it.albertus.util.ByteUtils.toIntArray;
import static it.albertus.util.ByteUtils.toShortArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bike extends BikesInfElement {

	public enum BikeType {
		CLASS_125(125),
		CLASS_250(250),
		CLASS_500(500);

		private int displacement;

		private BikeType(int displacement) {
			this.displacement = displacement;
		}

		public int getDisplacement() {
			return displacement;
		}
	}

	public static final int LENGTH = Settings.LENGTH + Gearbox.LENGTH + Power.LENGTH;

	/** 0-21 */
	private final Settings settings; 
	
	/** 22-41 */
	private final Gearbox gearbox;

	/** 42-147 */
	private final Power power;
	
	private final BikeType type;

	public Bike(final BikeType type, final byte[] inf) {
		this(type, new Settings(toInt(inf[0], inf[1]), toInt(inf[2], inf[3]), toInt(inf[4], inf[5]), toInt(inf[6], inf[7]), toInt(inf[8], inf[9]), toInt(inf[10], inf[11]), toInt(inf[12], inf[13]), toInt(inf[14], inf[15]), toInt(inf[16], inf[17]), toInt(inf[18], inf[19]), toInt(inf[20], inf[21])), new Gearbox(toIntArray(Arrays.copyOfRange(inf, 22, 42))), new Power(toShortArray(Arrays.copyOfRange(inf, 42, 148))));
	}

	public Bike(final BikeType type, final Settings settings, final Gearbox gearbox, final Power power) {
		this.settings = settings;
		this.gearbox = gearbox;
		this.power = power;
		this.type = type;
	}

	@Override
	public List<Byte> toByteList() {
		final List<Byte> byteList = new ArrayList<Byte>(LENGTH);
		byteList.addAll(settings.toByteList());
		byteList.addAll(gearbox.toByteList());
		byteList.addAll(power.toByteList());
		return byteList;
	}

	public Settings getSettings() {
		return settings;
	}

	public Gearbox getGearbox() {
		return gearbox;
	}

	public Power getPower() {
		return power;
	}

	public BikeType getType() {
		return type;
	}

}
