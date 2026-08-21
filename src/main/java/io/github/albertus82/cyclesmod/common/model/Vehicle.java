package io.github.albertus82.cyclesmod.common.model;

import static io.github.albertus82.util.ByteUtils.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Vehicle implements ByteArray {

	public static final int LENGTH = Settings.LENGTH + Gearbox.LENGTH + Torque.LENGTH;

	private final VehicleType type;

	/** 0-21 */
	private final Settings settings;

	/** 22-41 */
	private final Gearbox gearbox;

	/** 42-147 */
	private final Torque torque;

	public Vehicle(final VehicleType type, final byte[] inf) {
		this(type, new Settings(toInt(inf[0], inf[1]), toInt(inf[2], inf[3]), toInt(inf[4], inf[5]), toInt(inf[6], inf[7]), toInt(inf[8], inf[9]), toInt(inf[10], inf[11]), toInt(inf[12], inf[13]), toInt(inf[14], inf[15]), toInt(inf[16], inf[17]), toInt(inf[18], inf[19]), toInt(inf[20], inf[21])), new Gearbox(toIntArray(Arrays.copyOfRange(inf, 22, 42))), new Torque(toShortArray(Arrays.copyOfRange(inf, 42, 148))));
	}

	@Override
	public byte[] toByteArray() {
		final ByteBuffer buf = ByteBuffer.allocate(LENGTH);
		buf.put(settings.toByteArray());
		buf.put(gearbox.toByteArray());
		buf.put(torque.toByteArray());
		return buf.array();
	}

}
