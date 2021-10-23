package io.github.albertus82.cyclesmod.common.data;

import java.util.zip.DataFormatException;

import io.github.albertus82.cyclesmod.common.model.VehiclesInf;
import io.github.albertus82.cyclesmod.common.resources.CommonMessages;
import io.github.albertus82.cyclesmod.common.resources.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HiddenBike {

	private static final long CRC32 = Integer.toUnsignedLong(0xEF30BAE6);

	private static final String DEFLATED_BASE64 = "eNpjY5DQ22DGr57GnMb8jukd0ynmU8zvpBkYGDoYihiiGDwYnBjsNFCBkY0bduCNDYRCQVQ8DKQgAIgbExMVGgBWamOkzM8KtBoAC/woBQ==";

	private static final Messages messages = CommonMessages.INSTANCE;

	public static byte[] getByteArray() {
		try {
			return DataUtils.inflate(DEFLATED_BASE64, VehiclesInf.FILE_SIZE / 3, CRC32);
		}
		catch (final IllegalArgumentException | DataFormatException e) {
			throw new VerifyError(messages.get("common.error.hidden.cfg.corrupted"), e);
		}
		catch (final InvalidSizeException e) {
			throw new VerifyError(messages.get("common.error.hidden.cfg.corrupted.size", e.getExpected(), e.getActual()), e);
		}
		catch (final InvalidChecksumException e) {
			throw new VerifyError(messages.get("common.error.hidden.cfg.corrupted.crc", String.format("%08X", e.getExpected()), String.format("%08X", e.getActual())), e);
		}
	}

}
