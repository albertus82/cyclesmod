package it.albertus.cyclesmod.common.data;

import java.util.zip.DataFormatException;

import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultBikes {

	private static final long CRC32 = 0x28A33682L;

	private static final String DEFLATED_BASE64 = "eNpjY1ihv8GsiPEdEwQmAaGMKgMDwwaGmQytDMUMqQyRkpQAGTCQhwBFZWUVZKAIA5L8rAxwgO6mMiAEu2kBQwfQRYkMYQx+qpQALSjQ1QcBIxNzBDCCAn1dDWUZEX4uVribLAxBbuJh5mEGuakLCJVUgBKTGGoYMhkiGfwYXPRJBI7IwBsLCIAAJBE3R3NdZRGoqwArb09n";

	private static final Messages messages = CommonMessages.INSTANCE;

	public static byte[] getByteArray() {
		try {
			return DataUtils.inflate(DEFLATED_BASE64, BikesInf.FILE_SIZE, CRC32);
		}
		catch (final DataFormatException e) {
			throw new VerifyError(messages.get("common.error.original.file.corrupted", BikesInf.FILE_NAME), e);
		}
		catch (final InvalidSizeException e) {
			throw new VerifyError(messages.get("common.error.original.file.corrupted.size", BikesInf.FILE_NAME, e.getExpected(), e.getActual()), e);
		}
		catch (final InvalidChecksumException e) {
			throw new VerifyError(messages.get("common.error.original.file.corrupted.crc", BikesInf.FILE_NAME, String.format("%08X", e.getExpected()), String.format("%08X", e.getActual())), e);
		}
	}

}
