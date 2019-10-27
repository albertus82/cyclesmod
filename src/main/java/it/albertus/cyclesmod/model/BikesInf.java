package it.albertus.cyclesmod.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import it.albertus.cyclesmod.data.DefaultBikes;
import it.albertus.cyclesmod.model.Bike.BikeType;
import it.albertus.cyclesmod.resources.Messages;
import it.albertus.util.ByteUtils;
import it.albertus.util.IOUtils;

public class BikesInf {

	public static final String FILE_NAME = "BIKES.INF";
	public static final short FILE_SIZE = 444;

	private final Bike[] bikes = new Bike[BikeType.values().length];

	public BikesInf(final InputStream bikesInfInputStream) throws IOException {
		read(bikesInfInputStream);
	}

	public BikesInf(final File file) throws IOException {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			read(bis);
		}
		finally {
			IOUtils.closeQuietly(bis, fis);
		}
	}

	public void reset(final BikeType type) throws IOException {
		InputStream is = null;
		try {
			is = new DefaultBikes().getInputStream();
			read(is, type);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	private void read(final InputStream inf, BikeType... types) throws IOException {
		final byte[] inf125 = new byte[Bike.LENGTH];
		final byte[] inf250 = new byte[Bike.LENGTH];
		final byte[] inf500 = new byte[Bike.LENGTH];

		final boolean wrongFileSize = inf.read(inf125) != Bike.LENGTH || inf.read(inf250) != Bike.LENGTH || inf.read(inf500) != Bike.LENGTH || inf.read() != -1;
		inf.close();
		if (wrongFileSize) {
			throw new IllegalStateException(Messages.get("err.wrong.file.size"));
		}
		System.out.println(Messages.get("msg.file.read", FILE_NAME));

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
		System.out.println(Messages.get("msg.file.parsed", FILE_NAME));
	}

	public void write(final String fileName) throws IOException {
		final byte[] newBikesInf = this.toByteArray();
		final Checksum crc = new CRC32();
		crc.update(newBikesInf, 0, newBikesInf.length);
		System.out.println(Messages.get("msg.configuration.changed", crc.getValue() == DefaultBikes.CRC ? ' ' + Messages.get("msg.not") + ' ' : ' ', String.format("%08X", crc.getValue())));

		final File file = new File(fileName);
		if (file.exists() && !file.isDirectory()) {
			InputStream is = null;
			ByteArrayOutputStream os = null;
			try {
				is = new FileInputStream(file);
				os = new ByteArrayOutputStream();
				IOUtils.copy(is, os, FILE_SIZE);
			}
			finally {
				IOUtils.closeQuietly(os, is);
			}
			final Checksum crcExistingFile = new CRC32();
			crcExistingFile.update(os.toByteArray(), 0, os.size());
			if (os.size() == FILE_SIZE && crcExistingFile.getValue() == crc.getValue()) {
				System.out.println(Messages.get("msg.already.uptodate", FILE_NAME));
			}
			else {
				backup(fileName);
				doWrite(fileName, newBikesInf, crc);
			}
		}
		else {
			doWrite(fileName, newBikesInf, crc);
		}
	}

	private void doWrite(final String fileName, final byte[] newBikesInf, final Checksum crc) throws IOException {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(fileName);
			bos = new BufferedOutputStream(fos, FILE_SIZE);
			bos.write(newBikesInf);
			System.out.println(Messages.get("msg.new.file.written.into.path", FILE_NAME, "".equals(fileName) ? '.' : fileName, String.format("%08X", crc.getValue())));
		}
		finally {
			IOUtils.closeQuietly(bos, fos);
		}
	}

	private void backup(final String existingFile) throws IOException {
		File backupFile;
		int i = 1;
		final String parent = new File(existingFile).getParent();
		do {
			backupFile = new File(parent != null ? parent : "" + "BIKESINF." + String.format("%03d", i++));
		}
		while (backupFile.exists());

		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(existingFile);
			fos = new FileOutputStream(backupFile);
			IOUtils.copy(fis, fos, FILE_SIZE);
			System.out.println(Messages.get("msg.old.file.backed.up", FILE_NAME, backupFile));
		}
		finally {
			IOUtils.closeQuietly(fos, fis);
		}
	}

	/**
	 * Ricostruisce il file BIKES.INF a partire dalle 3 configurazioni contenute
	 * nell'oggetto (125, 250, 500).
	 * 
	 * @return L'array di byte corrispondente al file BIKES.INF.
	 */
	private byte[] toByteArray() {
		final List<Byte> byteList = new ArrayList<Byte>(FILE_SIZE);
		for (final Bike bike : bikes) {
			byteList.addAll(bike.toByteList());
		}
		if (byteList.size() != FILE_SIZE) {
			throw new IllegalStateException(Messages.get("err.wrong.file.size.detailed", FILE_NAME, FILE_SIZE, byteList.size()));
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
