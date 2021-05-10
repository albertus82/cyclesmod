package it.albertus.cyclesmod.common.data;

import java.util.zip.DataFormatException;

import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HiddenBike {

	private static final long CRC32 = 0xEF30BAE6L;

	private static final String DEFLATE_BASE64 = "eNpjY5DQ22DGr57GnMb8jukd0ynmU8zvpBkYGDoYihiiGDwYnBjsNFCBkY0bduCNDYRCQVQ8DKQgAIgbExMVGgBWamOkzM8KtBoAC/woBQ==";

	private static final Messages messages = CommonMessages.INSTANCE;

	public static byte[] getByteArray() {
		try {
			return Loader.load(DEFLATE_BASE64, BikesInf.FILE_SIZE / 3, CRC32);
		}
		catch (final DataFormatException e) {
			throw new VerifyError(messages.get("common.error.hidden.cfg.corrupted", BikesInf.FILE_NAME), e);
		}
		catch (final InvalidSizeException e) {
			throw new VerifyError(messages.get("common.error.hidden.cfg.corrupted.size", BikesInf.FILE_NAME, e.getExpected(), e.getActual()), e);
		}
		catch (final InvalidChecksumException e) {
			throw new VerifyError(messages.get("common.error.hidden.cfg.corrupted.crc", BikesInf.FILE_NAME, String.format("%08X", e.getExpected()), String.format("%08X", e.getActual())), e);
		}
	}

}
