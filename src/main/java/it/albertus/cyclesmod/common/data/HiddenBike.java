package it.albertus.cyclesmod.common.data;

import java.util.Base64;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HiddenBike {

	private static final long CRC32 = 0xEF30BAE6L;

	private static final String DEFLATE_BASE64 = "eNpjY5DQ22DGr57GnMb8jukd0ynmU8zvpBkYGDoYihiiGDwYnBjsNFCBkY0bduCNDYRCQVQ8DKQgAIgbExMVGgBWamOkzM8KtBoAC/woBQ==";

	private static final Messages messages = CommonMessages.INSTANCE;

	public static byte[] getByteArray() {
		final short expectedSize = BikesInf.FILE_SIZE / 3;
		final Inflater inflater = new Inflater();
		inflater.setInput(Base64.getDecoder().decode(DEFLATE_BASE64));
		final byte[] bytes = new byte[expectedSize];
		try {
			final int actualSize = inflater.inflate(bytes);
			if (actualSize != expectedSize) {
				throw new VerifyError(messages.get("common.error.hidden.cfg.corrupted.size", expectedSize, actualSize), new InvalidSizeException(expectedSize, actualSize));
			}
		}
		catch (final DataFormatException e) {
			throw new VerifyError(messages.get("common.error.hidden.cfg.corrupted"), e);
		}
		finally {
			inflater.end();
		}
		final long crc32 = computeCrc32(bytes);
		if (crc32 != CRC32) {
			throw new VerifyError(messages.get("common.error.hidden.cfg.corrupted.crc", String.format("%08X", CRC32), String.format("%08X", crc32)));
		}
		return bytes;
	}

	private static long computeCrc32(@NonNull final byte[] bytes) {
		final Checksum crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		return crc.getValue();
	}

}
