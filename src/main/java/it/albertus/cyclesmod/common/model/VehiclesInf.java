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

public class VehiclesInf {

	public static final short FILE_SIZE = 444;

	private final Map<VehicleType, Vehicle> vehicles = new EnumMap<>(VehicleType.class);

	/** Creates a new instance based on the default configuration. */
	public VehiclesInf() {
		parse(DefaultBikes.getByteArray());
	}

	/**
	 * Creates a new instance based on the provided INF file.
	 * 
	 * @param sourceInfFile the file to read
	 * 
	 * @throws IOException if an I/O error occurs
	 * @throws InvalidSizeException if the size of provided file is not acceptable
	 */
	public VehiclesInf(@NonNull final Path sourceInfFile) throws InvalidSizeException, IOException {
		final long fileSize = Files.readAttributes(sourceInfFile, BasicFileAttributes.class).size();
		if (fileSize != FILE_SIZE) {
			throw new InvalidSizeException(FILE_SIZE, fileSize);
		}
		final byte[] bytes = Files.readAllBytes(sourceInfFile);
		if (bytes.length != FILE_SIZE) {
			throw new InvalidSizeException(FILE_SIZE, bytes.length);
		}
		parse(bytes);
	}

	public VehiclesInf(@NonNull final byte[] bytes, VehicleType... vehicleTypes) {
		parse(bytes, vehicleTypes);
	}

	public void reset(final VehicleType... vehicleTypes) {
		parse(DefaultBikes.getByteArray(), vehicleTypes);
	}

	private void parse(@NonNull final byte[] bytes, VehicleType... vehicleTypes) {
		if (vehicleTypes == null || vehicleTypes.length == 0) {
			vehicleTypes = VehicleType.values();
		}
		for (final VehicleType vehicleType : vehicleTypes) {
			final int ordinal = vehicleType.getIndex();
			vehicles.put(vehicleType, new Vehicle(vehicleType, Arrays.copyOfRange(bytes, Vehicle.LENGTH * (ordinal - 1), Vehicle.LENGTH * ordinal)));
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
		for (final ByteList vehicle : vehicles.values()) {
			byteList.addAll(vehicle.toByteList());
		}
		if (byteList.size() != FILE_SIZE) {
			throw new IllegalStateException(new InvalidSizeException(FILE_SIZE, byteList.size()));
		}
		return ByteUtils.toByteArray(byteList);
	}

	public Map<VehicleType, Vehicle> getVehicles() {
		return Collections.unmodifiableMap(vehicles);
	}

	public static String getFileName(@NonNull final Game game) {
		switch (game) {
		case CYCLES:
			return "BIKES.INF";
		case GPC:
			return "CARS.INF";
		default:
			throw new IllegalArgumentException("Unknown game: " + game);
		}
	}

}
