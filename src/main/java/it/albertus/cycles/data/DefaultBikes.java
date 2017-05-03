package it.albertus.cycles.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.xml.bind.DatatypeConverter;

import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Messages;

public class DefaultBikes {

	public static final int CRC = 0x28A33682;

	private static final byte[] DEFAULT = new byte[BikesInf.FILE_SIZE];

	static {
		final Inflater inflater = new Inflater();
		inflater.setInput(DatatypeConverter.parseBase64Binary("eJxjY1ihv8GsiPEdEwQmAaGMKgMDwwaGmQytDMUMqQyRkpQAGTCQhwBFZWUVZKAIA5L8rAxwwIbmpjIgBLtpAUMH0EWJDGEMfqqUAC0o0NUHASMTcwQwggJ9XQ1lGRF+Lla4mywMQW7iYeZhBrmpCwiVVIASkxhqGDIZIhn8GFz0SQSOyMAbCwiAACQRN0dzXWURqKsAK29PZw=="));
		try {
			inflater.inflate(DEFAULT);
		}
		catch (final DataFormatException e) {
			throw new IllegalStateException(e);
		}
		inflater.end();
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

}
