package it.albertus.cycles.data;

import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DefaultBikes {

	private static final char PACKAGE_SEPARATOR = '\u002E';
	private static final char FILE_SEPARATOR = '\u002F';

	public static final String FILE_NAME = "bikes.zip";

	private final ZipInputStream inputStream;

	public ZipInputStream getInputStream() {
		return inputStream;
	}

	public DefaultBikes() throws IOException {
		this.inputStream = openBikesInfInputStream();
	}

	private ZipInputStream openBikesInfInputStream() throws IOException {
		System.out.println(Resources.get("msg.opening.original.file", BikesInf.FILE_NAME));
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(getClass().getResourceAsStream(FILE_NAME));
		}
		catch (Exception e) {
			throw new FileNotFoundException(Resources.get("msg.file.not.found", FILE_SEPARATOR + getClass().getPackage().getName().replace(PACKAGE_SEPARATOR, FILE_SEPARATOR) + FILE_SEPARATOR + FILE_NAME));
		}
		ZipEntry ze = zis.getNextEntry();
		if (ze.getCrc() != BikesInf.FILE_CRC) {
			throw new StreamCorruptedException(Resources.get("err.original.file.corrupted.crc", BikesInf.FILE_NAME, String.format("%08X", BikesInf.FILE_CRC), String.format("%08X", ze.getCrc())));
		}
		if (ze.getSize() != BikesInf.FILE_SIZE) {
			throw new StreamCorruptedException(Resources.get("err.original.file.corrupted.size", BikesInf.FILE_NAME, BikesInf.FILE_SIZE, ze.getSize()));
		}
		System.out.println(Resources.get("msg.original.file.opened", BikesInf.FILE_NAME, String.format("%08X", ze.getCrc())));
		return zis;
	}

}
