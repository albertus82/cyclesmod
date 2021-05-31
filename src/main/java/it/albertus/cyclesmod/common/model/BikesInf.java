package it.albertus.cyclesmod.common.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import it.albertus.cyclesmod.common.data.DefaultBikes;
import it.albertus.cyclesmod.common.data.InvalidSizeException;
import it.albertus.util.ByteUtils;
import lombok.NonNull;

public class BikesInf {

	public static final String FILE_NAME = "BIKES.INF";
	public static final short FILE_SIZE = 444;

	private final Map<VehicleType, Vehicle> vehicles = new EnumMap<>(VehicleType.class);

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
	 * @throws InvalidSizeException if the size of provided file is not acceptable
	 */
	public BikesInf(@NonNull final Path bikesInfFile) throws InvalidSizeException, IOException {
		final long fileSize = Files.readAttributes(bikesInfFile, BasicFileAttributes.class).size();
		if (fileSize != FILE_SIZE) {
			throw new InvalidSizeException(FILE_SIZE, fileSize);
		}
		final byte[] bytes = Files.readAllBytes(bikesInfFile);
		if (bytes.length != FILE_SIZE) {
			throw new InvalidSizeException(FILE_SIZE, bytes.length);
		}
		parse(bytes);
	}

	public BikesInf(@NonNull final byte[] bytes, VehicleType... bikeTypes) {
		parse(bytes, bikeTypes);
	}

	public void reset(final VehicleType... bikeTypes) {
		parse(DefaultBikes.getByteArray(), bikeTypes);
	}

	private void parse(@NonNull final byte[] bytes, VehicleType... bikeTypes) {
		if (bikeTypes == null || bikeTypes.length == 0) {
			bikeTypes = VehicleType.values();
		}
		for (final VehicleType bikeType : bikeTypes) {
			final int ordinal = bikeType.ordinal();
			vehicles.put(bikeType, new Vehicle(bikeType, Arrays.copyOfRange(bytes, Vehicle.LENGTH * ordinal, Vehicle.LENGTH * (ordinal + 1))));
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
		for (final ByteList bike : vehicles.values()) {
			byteList.addAll(bike.toByteList());
		}
		if (byteList.size() != FILE_SIZE) {
			throw new IllegalStateException(new InvalidSizeException(FILE_SIZE, byteList.size()));
		}
		return ByteUtils.toByteArray(byteList);
	}

	public Map<VehicleType, Vehicle> getVehicles() {
		return Collections.unmodifiableMap(vehicles);
	}

}
