package it.albertus.cyclesmod.common.model;

import static it.albertus.util.ByteUtils.toInt;
import static it.albertus.util.ByteUtils.toIntArray;
import static it.albertus.util.ByteUtils.toShortArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Bike implements ByteList {

	public static final int LENGTH = Settings.LENGTH + Gearbox.LENGTH + Power.LENGTH;

	private final BikeType type;

	/** 0-21 */
	private final Settings settings;

	/** 22-41 */
	private final Gearbox gearbox;

	/** 42-147 */
	private final Power power;

	public Bike(final BikeType type, final byte[] inf) {
		this(type, new Settings(toInt(inf[0], inf[1]), toInt(inf[2], inf[3]), toInt(inf[4], inf[5]), toInt(inf[6], inf[7]), toInt(inf[8], inf[9]), toInt(inf[10], inf[11]), toInt(inf[12], inf[13]), toInt(inf[14], inf[15]), toInt(inf[16], inf[17]), toInt(inf[18], inf[19]), toInt(inf[20], inf[21])), new Gearbox(toIntArray(Arrays.copyOfRange(inf, 22, 42))), new Power(toShortArray(Arrays.copyOfRange(inf, 42, 148))));
	}

	@Override
	public List<Byte> toByteList() {
		final List<Byte> byteList = new ArrayList<>(LENGTH);
		byteList.addAll(settings.toByteList());
		byteList.addAll(gearbox.toByteList());
		byteList.addAll(power.toByteList());
		return byteList;
	}

}
