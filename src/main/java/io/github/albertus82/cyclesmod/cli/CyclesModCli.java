package io.github.albertus82.cyclesmod.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.github.albertus82.cyclesmod.CyclesMod;
import io.github.albertus82.cyclesmod.cli.resources.ConsoleMessages;
import io.github.albertus82.cyclesmod.cli.resources.PicocliMessages;
import io.github.albertus82.cyclesmod.common.engine.CyclesModEngine;
import io.github.albertus82.cyclesmod.common.engine.InvalidNumberException;
import io.github.albertus82.cyclesmod.common.engine.InvalidPropertyException;
import io.github.albertus82.cyclesmod.common.engine.UnknownPropertyException;
import io.github.albertus82.cyclesmod.common.engine.ValueOutOfRangeException;
import io.github.albertus82.cyclesmod.common.model.Game;
import io.github.albertus82.cyclesmod.common.model.VehiclesCfg;
import io.github.albertus82.cyclesmod.common.model.VehiclesInf;
import io.github.albertus82.cyclesmod.common.resources.Messages;
import it.albertus.util.logging.LoggingSupport;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(versionProvider = VersionProvider.class, mixinStandardHelpOptions = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE) // test
@SuppressWarnings("java:S106") // Replace this use of System.out or System.err by a logger. Standard outputs should not be used directly to log anything (java:S106)
public class CyclesModCli implements Callable<Integer> {

	private static final Messages messages = ConsoleMessages.INSTANCE;

	private static final String DONE = messages.get("console.message.done");
	private static final String ERROR = messages.get("console.message.error");

	private static final Game GAME = Game.CYCLES;

	@Parameters(descriptionKey = "parameter.path", defaultValue = "")
	private Path path;

	@Option(names = { "-e", "--errors" }, descriptionKey = "option.errors")
	private boolean errors;

	@Option(names = { "-d", "--debug" }, descriptionKey = "option.debug")
	private boolean debug;

	private final CyclesModEngine engine = new CyclesModEngine();

	public static void main(final String... args) {
		System.exit(new CommandLine(new CyclesModCli()).setCommandName(CyclesMod.class.getSimpleName().toLowerCase(Locale.ROOT)).setOut(new PrintWriter(System.out, true) {
			@Override // Fix encoding problems in Windows Command Prompt with special characters
			public void write(final String s) {
				synchronized (lock) {
					System.out.print(s);
				}
			}
		}).setResourceBundle(ResourceBundle.getBundle(PicocliMessages.class.getName().toLowerCase(Locale.ROOT))).setOptionsCaseInsensitive(true).execute(args));
	}

	@Override
	public Integer call() {
		if (debug) {
			LoggingSupport.setRootLevel(Level.FINE);
			errors = true;
		}
		else {
			LoggingSupport.setRootLevel(Level.WARNING);
		}

		try {
			if (!Paths.get("").equals(path)) {
				path = prepareWorkingDirectory(path);
			}
			loadOriginalConfiguration();
			final Path bikesCfgFile = Paths.get(path.toString(), VehiclesCfg.getFileName(GAME));
			if (!bikesCfgFile.toFile().exists()) {
				createBikesCfg(bikesCfgFile);
			}
			else {
				applyCustomizations(bikesCfgFile);
			}
			final Path bikesInfFile = Paths.get(path.toString(), VehiclesInf.getFileName(GAME));
			createBikesInf(bikesInfFile);
			return ExitCode.OK;
		}
		catch (final InvalidPropertyException | IOException | RuntimeException e) {
			if (errors) {
				e.printStackTrace();
			}
			return ExitCode.SOFTWARE;
		}
	}

	private static Path prepareWorkingDirectory(@NonNull Path path) throws IOException {
		if (path.toFile().exists()) {
			if (!Files.isDirectory(path)) {
				System.err.println(messages.get("console.error.invalid.directory"));
				throw new IOException(path + " is not a directory");
			}
			else {
				return path;
			}
		}
		else {
			System.out.print(messages.get("console.message.creating.working.directory") + ' ');
			try {
				path = Files.createDirectories(path);
				System.out.println(DONE);
				return path;
			}
			catch (final IOException e) {
				System.out.println(ERROR);
				System.err.println(messages.get("console.error.creating.working.directory", e));
				throw e;
			}
		}
	}

	private void loadOriginalConfiguration() {
		System.out.print(messages.get("console.message.reading.original.configuration") + ' ');
		engine.setVehiclesInf(new VehiclesInf(GAME));
		System.out.println(DONE);
	}

	private void createBikesCfg(@NonNull final Path bikesCfgFile) throws IOException {
		System.out.print(messages.get("console.message.creating.default.file", VehiclesCfg.getFileName(GAME)) + ' ');
		try {
			VehiclesCfg.writeDefault(GAME, bikesCfgFile);
			System.out.println(DONE);
		}
		catch (final IOException e) {
			System.out.println(ERROR);
			System.err.println(messages.get("console.error.cannot.create.default.file", VehiclesCfg.getFileName(GAME), e));
			throw e;
		}
	}

	private void applyCustomizations(@NonNull final Path bikesCfgFile) throws IOException, InvalidPropertyException {
		System.out.print(messages.get("console.message.applying.customizations", VehiclesCfg.getFileName(GAME)) + ' ');
		if (Files.isDirectory(bikesCfgFile)) {
			System.out.println(ERROR);
			System.err.println(messages.get("console.error.cannot.open.file.directory", VehiclesCfg.getFileName(GAME)));
			throw new IOException(bikesCfgFile + "is a directory");
		}
		final Properties properties;
		try {
			properties = new VehiclesCfg(bikesCfgFile).getProperties();
		}
		catch (final IOException e) {
			System.out.println(ERROR);
			System.err.println(messages.get("console.error.cannot.read.file", VehiclesCfg.getFileName(GAME), e));
			throw e;
		}
		short changesCount = 0;
		for (final String name : properties.stringPropertyNames()) {
			final String value = properties.getProperty(name);
			if (applyProperty(name, value)) {
				changesCount++;
			}
		}
		System.out.println(messages.get("console.message.applying.customizations.done", changesCount));
	}

	private boolean applyProperty(@NonNull final String name, final String value) throws InvalidPropertyException {
		try {
			return engine.applyProperty(name, value);
		}
		catch (final UnknownPropertyException e) {
			System.out.println(ERROR);
			System.err.println(messages.get("console.error.unknown.property", e.getPropertyName()));
			throw e;
		}
		catch (final InvalidNumberException e) {
			System.out.println(ERROR);
			System.err.println(messages.get("console.error.invalid.number", e.getPropertyName(), e.getValue()));
			throw e;
		}
		catch (final ValueOutOfRangeException e) {
			System.out.println(ERROR);
			System.err.println(messages.get("console.error.value.out.of.range", e.getPropertyName(), e.getValue(), e.getMinValue(), e.getMaxValue()));
			throw e;
		}
	}

	private void createBikesInf(@NonNull final Path bikesInfFile) throws IOException {
		System.out.print(messages.get("console.message.preparing.new.file", VehiclesInf.getFileName(GAME)) + ' ');
		final byte[] currentBytes = engine.getVehiclesInf().toByteArray();
		if (bikesInfFile.toFile().exists()) {
			if (Files.isDirectory(bikesInfFile)) {
				System.out.println(ERROR);
				System.err.println(messages.get("console.error.cannot.open.file.directory", VehiclesInf.getFileName(GAME)));
				throw new IOException(bikesInfFile + " is a directory");
			}
			if (bikesInfFile.toFile().length() == VehiclesInf.FILE_SIZE) {
				final byte[] existingBytes;
				try {
					existingBytes = Files.readAllBytes(bikesInfFile);
					System.out.println(DONE);
				}
				catch (final IOException e) {
					System.out.println(ERROR);
					System.err.println(messages.get("console.error.cannot.read.file", VehiclesInf.getFileName(GAME), e));
					throw e;
				}
				if (!Arrays.equals(currentBytes, existingBytes)) {
					backupBikesInf(bikesInfFile);
					writeBikesInf(currentBytes, bikesInfFile);
				}
				else {
					System.out.println(messages.get("console.message.already.uptodate", VehiclesInf.getFileName(GAME)));
				}
			}
			else {
				System.out.println(DONE);
				backupBikesInf(bikesInfFile);
				writeBikesInf(currentBytes, bikesInfFile);
			}
		}
		else {
			System.out.println(DONE);
			writeBikesInf(currentBytes, bikesInfFile);
		}
	}

	private void backupBikesInf(@NonNull final Path bikesInfFile) throws IOException {
		System.out.print(messages.get("console.message.backup.file", VehiclesInf.getFileName(GAME)) + ' ');
		try {
			int i = 0;
			final String parent = bikesInfFile.toFile().getParent();
			final String prefix = parent != null ? parent + File.separator : "";
			File backupFile;
			do {
				backupFile = new File(prefix + "BIKES" + String.format("%03d", i++) + ".ZIP");
			}
			while (backupFile.exists());
			try (final OutputStream fos = Files.newOutputStream(backupFile.toPath()); final ZipOutputStream zos = new ZipOutputStream(fos)) {
				zos.setLevel(Deflater.BEST_COMPRESSION);
				zos.putNextEntry(new ZipEntry(bikesInfFile.toFile().getName()));
				Files.copy(bikesInfFile, zos);
				zos.closeEntry();
			}
			System.out.println(messages.get("console.message.backup.file.done", backupFile.toPath().getFileName()));
		}
		catch (final IOException e) {
			System.out.println(ERROR);
			System.err.println(messages.get("console.error.cannot.backup.file", VehiclesInf.getFileName(GAME), e));
			throw e;
		}
	}

	private void writeBikesInf(@NonNull final byte[] bytes, @NonNull final Path bikesInfFile) throws IOException {
		System.out.print(messages.get("console.message.writing.new.file", VehiclesInf.getFileName(GAME)) + ' ');
		try {
			Files.write(bikesInfFile, bytes);
			System.out.println(DONE);
		}
		catch (final IOException e) {
			System.out.println(ERROR);
			System.err.println(messages.get("console.error.cannot.write.file", VehiclesInf.getFileName(GAME), e));
			throw e;
		}
	}

}
