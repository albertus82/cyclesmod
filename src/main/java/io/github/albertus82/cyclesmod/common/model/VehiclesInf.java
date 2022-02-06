package io.github.albertus82.cyclesmod.common.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import io.github.albertus82.cyclesmod.common.data.DefaultBikes;
import io.github.albertus82.cyclesmod.common.data.DefaultCars;
import io.github.albertus82.cyclesmod.common.data.InvalidSizeException;
import lombok.Getter;
import lombok.NonNull;

public class VehiclesInf {

	public static final short FILE_SIZE = 444;

	private final Map<VehicleType, Vehicle> vehicles = new EnumMap<>(VehicleType.class);

	@NonNull
	@Getter
	private Game game;

	/** Creates a new instance based on the default configuration. */
	public VehiclesInf(@NonNull final Game game) {
		this.game = game;
		reset(game);
	}

	/**
	 * Creates a new instance based on the provided INF file.
	 * 
	 * @param sourceInfFile the file to read
	 * 
	 * @throws IOException if an I/O error occurs
	 * @throws InvalidSizeException if the size of provided file is not acceptable
	 */
	public VehiclesInf(@NonNull final Game game, @NonNull final Path sourceInfFile) throws InvalidSizeException, IOException {
		final long fileSize = Files.readAttributes(sourceInfFile, BasicFileAttributes.class).size();
		if (fileSize != FILE_SIZE) {
			throw new InvalidSizeException(FILE_SIZE, fileSize);
		}
		final byte[] bytes = Files.readAllBytes(sourceInfFile);
		if (bytes.length != FILE_SIZE) {
			throw new InvalidSizeException(FILE_SIZE, bytes.length);
		}
		this.game = game;
		parse(bytes);
	}

	public VehiclesInf(@NonNull final Game game, @NonNull final byte[] bytes, VehicleType... vehicleTypes) {
		this.game = game;
		parse(bytes, vehicleTypes);
	}

	public void reset(@NonNull final Game game, final VehicleType... vehicleTypes) {
		final byte[] bytes;
		switch (game) {
		case CYCLES:
			bytes = DefaultBikes.getByteArray();
			break;
		case GPC:
			bytes = DefaultCars.getByteArray();
			break;
		default:
			throw new IllegalArgumentException("Unknown game: " + game);
		}
		this.game = game;
		parse(bytes, vehicleTypes);
	}

	private void parse(@NonNull final byte[] bytes, VehicleType... vehicleTypes) {
		if (vehicleTypes == null || vehicleTypes.length == 0) {
			vehicleTypes = VehicleType.values();
		}
		for (final VehicleType vehicleType : vehicleTypes) {
			final int index = vehicleType.getIndex();
			vehicles.put(vehicleType, new Vehicle(vehicleType, Arrays.copyOfRange(bytes, Vehicle.LENGTH * index, Vehicle.LENGTH * (index + 1))));
		}
	}

	/**
	 * Rebuilds the BIKES.INF from the three configurations contained in this object
	 * (125, 250, 500).
	 * 
	 * @return A new byte array representing the BIKES.INF file.
	 */
	public byte[] toByteArray() {
		final ByteBuffer buf = ByteBuffer.allocate(FILE_SIZE);
		for (final ByteArray vehicle : vehicles.values()) {
			buf.put(vehicle.toByteArray());
		}
		if (buf.position() != buf.capacity()) {
			throw new IllegalStateException(new InvalidSizeException(buf.capacity(), buf.position()));
		}
		return buf.array();
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
