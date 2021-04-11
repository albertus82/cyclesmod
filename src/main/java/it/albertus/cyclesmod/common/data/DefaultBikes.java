package it.albertus.cyclesmod.common.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.Messages;
import jakarta.xml.bind.DatatypeConverter;

public class DefaultBikes {

	public static final int CRC = 0x28A33682;

	private static final byte[] DEFAULT = new byte[BikesInf.FILE_SIZE];

	static {
		final Inflater inflater = new Inflater();
		inflater.setInput(DatatypeConverter.parseBase64Binary("eNpjY1ihv8GsiPEdEwQmAaGMKgMDwwaGmQytDMUMqQyRkpQAGTCQhwBFZWUVZKAIA5L8rAxwgO6mMiAEu2kBQwfQRYkMYQx+qpQALSjQ1QcBIxNzBDCCAn1dDWUZEX4uVribLAxBbuJh5mEGuakLCJVUgBKTGGoYMhkiGfwYXPRJBI7IwBsLCIAAJBE3R3NdZRGoqwArb09n"));
		try {
			inflater.inflate(DEFAULT);
		}
		catch (final DataFormatException e) {
			throw new IllegalStateException(e);
		}
		finally {
			inflater.end();
		}
	}

	public DefaultBikes() throws StreamCorruptedException {
		final Checksum crc = new CRC32();
		crc.update(DEFAULT, 0, DEFAULT.length);
		if (crc.getValue() != CRC) {
			throw new StreamCorruptedException(Messages.get("err.original.file.corrupted.crc", BikesInf.FILE_NAME, String.format("%08X", CRC), String.format("%08X", crc.getValue())));
		}
		if (DEFAULT.length != BikesInf.FILE_SIZE) {
			throw new StreamCorruptedException(Messages.get("err.original.file.corrupted.size", BikesInf.FILE_NAME, BikesInf.FILE_SIZE, DEFAULT.length));
		}
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(DEFAULT);
	}

	byte[] getByteArray() {
		return Arrays.copyOf(DEFAULT, DEFAULT.length);
	}

}
