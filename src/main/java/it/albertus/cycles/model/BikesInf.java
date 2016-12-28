package it.albertus.cycles.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import it.albertus.cycles.data.DefaultBikes;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.ByteUtils;

public class BikesInf {

	public static final String FILE_NAME = "BIKES.INF";
	public static final short FILE_SIZE = 444;

	private final Bike[] bikes = new Bike[3];

	public BikesInf(final InputStream bikesInfInputStream) throws IOException {
		read(bikesInfInputStream);
	}

	public BikesInf(final File file) throws IOException {
		read(new BufferedInputStream(new FileInputStream(file)));
	}

	public void reset(final BikeType type) throws IOException {
		read(new DefaultBikes().getInputStream(), type);
	}

	private void read(final InputStream inf, BikeType... types) throws IOException {
		byte[] inf125 = new byte[Bike.LENGTH];
		byte[] inf250 = new byte[Bike.LENGTH];
		byte[] inf500 = new byte[Bike.LENGTH];

		if (inf.read(inf125) != Bike.LENGTH || inf.read(inf250) != Bike.LENGTH || inf.read(inf500) != Bike.LENGTH || inf.read() != -1) {
			inf.close();
			throw new IllegalStateException(Messages.get("err.wrong.file.size"));
		}
		inf.close();
		System.out.println(Messages.get("msg.file.read", FILE_NAME));

		if (types == null || types.length == 0) {
			/* Full reading */
			bikes[0] = new Bike(BikeType.CLASS_125, inf125);
			bikes[1] = new Bike(BikeType.CLASS_250, inf250);
			bikes[2] = new Bike(BikeType.CLASS_500, inf500);
		}
		else {
			/* Replace only selected bikes */
			byte[][] infs = new byte[3][];
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

		final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName), FILE_SIZE);
		write(bos, newBikesInf);
		System.out.println(Messages.get("msg.new.file.written.into.path", FILE_NAME, "".equals(fileName) ? '.' : fileName, String.format("%08X", crc.getValue())));
	}

	private void write(OutputStream outputStream, byte[] bikesInf) throws IOException {
		if (bikesInf == null || outputStream == null) {
			throw new IllegalArgumentException();
		}
		outputStream.write(bikesInf);
		outputStream.flush();
		outputStream.close();
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
