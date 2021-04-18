package it.albertus.cyclesmod.common.model;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import it.albertus.cyclesmod.common.data.DefaultBikes;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.util.ByteUtils;
import it.albertus.util.IOUtils;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class BikesInf {

	public static final String FILE_NAME = "BIKES.INF";
	public static final short FILE_SIZE = 444;

	private static final Messages messages = CommonMessages.INSTANCE;

	private final Map<BikeType, Bike> bikeMap = new EnumMap<>(BikeType.class);

	/** Creates a new instance based on the default configuration. */
	public BikesInf() {
		parse(DefaultBikes.getByteArray());
	}

	/**
	 * Creates a new instance based on the provided INF file.
	 * 
	 * @param bikesInfFile the file to read
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public BikesInf(@NonNull final Path bikesInfFile) throws IOException {
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (final InputStream fis = Files.newInputStream(bikesInfFile)) {
				IOUtils.copy(fis, baos, FILE_SIZE);
			}
			log.log(Level.FINE, messages.get("common.message.file.read"), FILE_NAME);
			final byte[] bytes = baos.toByteArray();
			if (bytes.length != FILE_SIZE) {
				throw new IllegalStateException(messages.get("common.error.wrong.file.size", FILE_NAME, FILE_SIZE, bytes.length));
			}
			parse(bytes);
		}
	}

	public void reset(final BikeType... bikeTypes) {
		parse(DefaultBikes.getByteArray(), bikeTypes);
	}

	private void parse(@NonNull final byte[] bytes, BikeType... bikeTypes) {
		if (bikeTypes == null || bikeTypes.length == 0) {
			bikeTypes = BikeType.values();
		}
		for (final BikeType bikeType : bikeTypes) {
			final int ordinal = bikeType.ordinal();
			bikeMap.put(bikeType, new Bike(bikeType, Arrays.copyOfRange(bytes, Bike.LENGTH * ordinal, Bike.LENGTH * (ordinal + 1))));
		}
	}

	public boolean write(@NonNull final Path file) throws IOException { // TODO move into GUI
		final byte[] bytes = toByteArray();
		if (file.toFile().exists() && !Files.isDirectory(file)) {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			try (final InputStream is = Files.newInputStream(file)) {
				IOUtils.copy(is, os, FILE_SIZE);
			}
			if (Arrays.equals(os.toByteArray(), bytes)) {
				return false;
			}
			else {
				write(bytes, file);
				return true;
			}
		}
		else {
			write(bytes, file);
			return true;
		}
	}

	public static void write(@NonNull final byte[] contents, @NonNull final Path file) throws IOException {
		try (final OutputStream fos = Files.newOutputStream(file); final OutputStream bos = new BufferedOutputStream(fos, FILE_SIZE)) {
			bos.write(contents);
		}
	}

	public static Path backup(@NonNull final Path file) throws IOException {
		int i = 0;
		final String parent = file.toFile().getParent();
		final String prefix = parent != null ? parent + File.separator : "";
		File backupFile;
		do {
			backupFile = new File(prefix + "BIKES" + String.format("%03d", i++) + ".ZIP");
		}
		while (backupFile.exists());

		try (final InputStream fis = Files.newInputStream(file); final OutputStream fos = Files.newOutputStream(backupFile.toPath()); final ZipOutputStream zos = new ZipOutputStream(fos)) {
			zos.setLevel(Deflater.BEST_COMPRESSION);
			zos.putNextEntry(new ZipEntry(file.toFile().getName()));
			IOUtils.copy(fis, zos, FILE_SIZE);
			zos.closeEntry();
			return backupFile.toPath();
		}
	}

	/**
	 * Rebuilds the BIKES.INF from the three configurations contained in this object
	 * (125, 250, 500).
	 * 
	 * @return A new byte array representing the BIKES.INF file.
	 */
	public byte[] toByteArray() {
		final List<Byte> byteList = new ArrayList<>(FILE_SIZE);
		for (final ByteList bike : bikeMap.values()) {
			byteList.addAll(bike.toByteList());
		}
		if (byteList.size() != FILE_SIZE) {
			throw new IllegalStateException(messages.get("common.error.wrong.file.size", FILE_NAME, FILE_SIZE, byteList.size()));
		}
		return ByteUtils.toByteArray(byteList);
	}

	public Map<BikeType, Bike> getBikeMap() {
		return Collections.unmodifiableMap(bikeMap);
	}

}
