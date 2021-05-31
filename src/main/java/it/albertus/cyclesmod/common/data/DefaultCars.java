package it.albertus.cyclesmod.common.data;

import java.util.zip.DataFormatException;

import it.albertus.cyclesmod.common.model.Game;
import it.albertus.cyclesmod.common.model.VehiclesInf;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultCars {

	private static final String FILE_NAME = VehiclesInf.getFileName(Game.GPC);

	private static final long CRC32 = Integer.toUnsignedLong(0x8B31837B);

	private static final String DEFLATED_BASE64 = "eNpjZTDQeqCXwnCA+QBzGkcaBxMzEzObBAMDQxNDKoMfgy2DMQODBgwY2bhhA97YQCgURMXDQAoCgLgxMVGhAWClMHMYoICN4Y3OFYMKBgVmBWY+Nj62C0wXmHxkgRK9DHkMUQxeIFfB3aRhjgps4MARCKAme3j5+QUgAMxlKZmZeRVNPU0VFcVgUFCQl5OZEhWA1U0dOg/0poHddI3tGts7pncQN/Ux5DNEg91kRI6bgoJCQsIioqKiYoAAFkQFQLfk5WUCQXpKUjwsmDDcBAA7dXI/";

	private static final Messages messages = CommonMessages.INSTANCE;

	public static byte[] getByteArray() {
		try {
			return DataUtils.inflate(DEFLATED_BASE64, VehiclesInf.FILE_SIZE, CRC32);
		}
		catch (final IllegalArgumentException | DataFormatException e) {
			throw new VerifyError(messages.get("common.error.original.file.corrupted", FILE_NAME), e);
		}
		catch (final InvalidSizeException e) {
			throw new VerifyError(messages.get("common.error.original.file.corrupted.size", FILE_NAME, e.getExpected(), e.getActual()), e);
		}
		catch (final InvalidChecksumException e) {
			throw new VerifyError(messages.get("common.error.original.file.corrupted.crc", FILE_NAME, String.format("%08X", e.getExpected()), String.format("%08X", e.getActual())), e);
		}
	}

}
