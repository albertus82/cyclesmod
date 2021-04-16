package it.albertus.cyclesmod.common.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import it.albertus.cyclesmod.common.data.DefaultBikes;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.util.ByteUtils;
import it.albertus.util.IOUtils;
import lombok.extern.java.Log;

@Log
public class BikesInf {

	public static final String FILE_NAME = "BIKES.INF";
	public static final short FILE_SIZE = 444;

	private final Bike[] bikes = new Bike[BikeType.values().length];

	private static final Messages messages = CommonMessages.INSTANCE;

	public BikesInf(final InputStream bikesInfInputStream) throws IOException {
		read(bikesInfInputStream);
	}

	public BikesInf(final Path sourceFile) throws IOException {
		try (final InputStream fis = Files.newInputStream(sourceFile); final InputStream bis = new BufferedInputStream(fis)) {
			read(bis);
		}
	}

	public void reset(final BikeType type) throws IOException {
		try (final InputStream is = new DefaultBikes().getInputStream()) {
			read(is, type);
		}
	}

	private void read(final InputStream inf, BikeType... types) throws IOException {
		final byte[] inf125 = new byte[Bike.LENGTH];
		final byte[] inf250 = new byte[Bike.LENGTH];
		final byte[] inf500 = new byte[Bike.LENGTH];

		final boolean wrongFileSize = inf.read(inf125) != Bike.LENGTH || inf.read(inf250) != Bike.LENGTH || inf.read(inf500) != Bike.LENGTH || inf.read() != -1;
		inf.close();
		if (wrongFileSize) {
			throw new IllegalStateException(messages.get("common.error.wrong.file.size"));
		}
		log.info(messages.get("common.message.file.read", FILE_NAME));

		if (types == null || types.length == 0) {
			/* Full reading */
			bikes[0] = new Bike(BikeType.CLASS_125, inf125);
			bikes[1] = new Bike(BikeType.CLASS_250, inf250);
			bikes[2] = new Bike(BikeType.CLASS_500, inf500);
		}
		else {
			/* Replace only selected bikes */
			final byte[][] infs = new byte[3][];
			infs[0] = inf125;
			infs[1] = inf250;
			infs[2] = inf500;
			for (final BikeType type : types) {
				bikes[type.ordinal()] = new Bike(type, infs[type.ordinal()]);
			}
		}
		log.info(messages.get("common.message.file.parsed", FILE_NAME));
	}

	public void write(final Path fileName, final boolean backupExisting) throws IOException {
		final byte[] newBikesInf = this.toByteArray();
		final Checksum crc = new CRC32();
		crc.update(newBikesInf, 0, newBikesInf.length);
		log.info(messages.get("common.message.configuration.changed", crc.getValue() == DefaultBikes.CRC ? ' ' + messages.get("common.message.not") + ' ' : ' ', String.format("%08X", crc.getValue())));

		final File file = fileName.toFile();
		if (file.exists() && !file.isDirectory()) {
			try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				try (final InputStream is = Files.newInputStream(fileName)) {
					IOUtils.copy(is, os, FILE_SIZE);
				}
				if (Arrays.equals(os.toByteArray(), newBikesInf)) {
					log.info(messages.get("common.message.already.uptodate", FILE_NAME));
				}
				else {
					if (backupExisting) {
						backup(fileName);
					}
					doWrite(fileName, newBikesInf, crc);
				}
			}
		}
		else {
			doWrite(fileName, newBikesInf, crc);
		}
	}

	private void doWrite(final Path fileName, final byte[] newBikesInf, final Checksum crc) throws IOException {
		try (final OutputStream fos = Files.newOutputStream(fileName); final OutputStream bos = new BufferedOutputStream(fos, FILE_SIZE)) {
			bos.write(newBikesInf);
			log.info(messages.get("common.message.new.file.written.into.path", FILE_NAME, "".equals(fileName.toString()) ? '.' : fileName, String.format("%08X", crc.getValue())));
		}
	}

	private void backup(final Path existingFile) throws IOException {
		File backupFile;
		int i = 0;
		final String parent = existingFile.toFile().getParent();
		final String prefix = parent != null ? parent + File.separator : "";
		do {
			backupFile = new File(prefix + "BIKES" + String.format("%03d", i++) + ".ZIP");
		}
		while (backupFile.exists());

		try (final InputStream fis = Files.newInputStream(existingFile); final OutputStream fos = new FileOutputStream(backupFile); final ZipOutputStream zos = new ZipOutputStream(fos)) {
			zos.setLevel(Deflater.BEST_COMPRESSION);
			zos.putNextEntry(new ZipEntry(existingFile.toFile().getName()));
			IOUtils.copy(fis, zos, FILE_SIZE);
			zos.closeEntry();
			log.info(messages.get("common.message.old.file.backed.up", FILE_NAME, backupFile));
		}
	}

	/**
	 * Ricostruisce il file BIKES.INF a partire dalle 3 configurazioni contenute
	 * nell'oggetto (125, 250, 500).
	 * 
	 * @return L'array di byte corrispondente al file BIKES.INF.
	 */
	private byte[] toByteArray() {
		final List<Byte> byteList = new ArrayList<>(FILE_SIZE);
		for (final Bike bike : bikes) {
			byteList.addAll(bike.toByteList());
		}
		if (byteList.size() != FILE_SIZE) {
			throw new IllegalStateException(messages.get("common.error.wrong.file.size.detailed", FILE_NAME, FILE_SIZE, byteList.size()));
		}
		return ByteUtils.toByteArray(byteList);
	}

	public Bike getBike(int displacement) {
		for (final Bike bike : bikes) {
			if (bike.getType().getDisplacement() == displacement) {
				return bike;
			}
		}
		return null;
	}

	public Bike[] getBikes() {
		return bikes;
	}

}
