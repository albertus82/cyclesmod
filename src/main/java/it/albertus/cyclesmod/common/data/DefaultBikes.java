package it.albertus.cyclesmod.common.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.InflaterInputStream;

import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.util.IOUtils;
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
		try (final InputStream is = getInputStream(); final ByteArrayOutputStream os = new ByteArrayOutputStream(BikesInf.FILE_SIZE)) {
			IOUtils.copy(is, os, 512);
			final byte[] bytes = os.toByteArray();
			verify(bytes);
			return bytes;
		}
		catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void verify(final byte[] bytes) {
		final Checksum crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		if (crc.getValue() != CRC) {
			throw new VerifyError(messages.get("common.error.original.file.corrupted.crc", BikesInf.FILE_NAME, String.format("%08X", CRC), String.format("%08X", crc.getValue())));
		}
		if (bytes.length != BikesInf.FILE_SIZE) {
			throw new VerifyError(messages.get("common.error.original.file.corrupted.size", BikesInf.FILE_NAME, BikesInf.FILE_SIZE, bytes.length));
		}
	}

}
