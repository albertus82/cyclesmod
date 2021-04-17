package it.albertus.cyclesmod.common.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultBikes {

	public static final int CRC = 0x28A33682;

	private static final String DEFLATE_BASE64 = "eNpjY1ihv8GsiPEdEwQmAaGMKgMDwwaGmQytDMUMqQyRkpQAGTCQhwBFZWUVZKAIA5L8rAxwgO6mMiAEu2kBQwfQRYkMYQx+qpQALSjQ1QcBIxNzBDCCAn1dDWUZEX4uVribLAxBbuJh5mEGuakLCJVUgBKTGGoYMhkiGfwYXPRJBI7IwBsLCIAAJBE3R3NdZRGoqwArb09n";

	private static final Messages messages = CommonMessages.INSTANCE;

	public static InputStream getInputStream() {
		return new InflaterInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(DEFLATE_BASE64)));
	}

	public static byte[] getByteArray() {
		final Inflater inflater = new Inflater();
		inflater.setInput(Base64.getDecoder().decode(DEFLATE_BASE64));
		final byte[] bytes = new byte[BikesInf.FILE_SIZE];
		try {
			final int size = inflater.inflate(bytes);
			if (size != BikesInf.FILE_SIZE) {
				throw new VerifyError(messages.get("common.error.original.file.corrupted.size", BikesInf.FILE_NAME, BikesInf.FILE_SIZE, size));
			}
		}
		catch (final DataFormatException e) {
			throw new IllegalStateException(e);
		}
		finally {
			inflater.end();
		}
		final Checksum crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		if (crc.getValue() != CRC) {
			throw new VerifyError(messages.get("common.error.original.file.corrupted.crc", BikesInf.FILE_NAME, String.format("%08X", CRC), String.format("%08X", crc.getValue())));
		}
		return bytes;
	}

}
