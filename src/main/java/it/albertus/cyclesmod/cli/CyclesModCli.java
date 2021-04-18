package it.albertus.cyclesmod.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import it.albertus.cyclesmod.CyclesMod;
import it.albertus.cyclesmod.cli.resources.ConsoleMessages;
import it.albertus.cyclesmod.cli.resources.Picocli;
import it.albertus.cyclesmod.common.engine.CyclesModEngine;
import it.albertus.cyclesmod.common.engine.InvalidNumberException;
import it.albertus.cyclesmod.common.engine.UnknownPropertyException;
import it.albertus.cyclesmod.common.engine.ValueOutOfRangeException;
import it.albertus.cyclesmod.common.model.BikesCfg;
import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.util.IOUtils;
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
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("java:S106") // Replace this use of System.out or System.err by a logger. Standard outputs should not be used directly to log anything (java:S106)
public class CyclesModCli extends CyclesModEngine implements Callable<Integer> {

	@Parameters(arity = "0..1", descriptionKey = "parameter.path", defaultValue = "") private Path path;

	@Option(names = { "-e", "--errors" }, descriptionKey = "option.errors") private boolean errors;

	private static final Messages messages = ConsoleMessages.INSTANCE;

	public static void main(final String... args) {
		System.exit(new CommandLine(new CyclesModCli()).setCommandName(CyclesMod.class.getSimpleName().toLowerCase(Locale.ROOT)).setOptionsCaseInsensitive(true).setResourceBundle(ResourceBundle.getBundle(Picocli.class.getName().toLowerCase(Locale.ROOT))).execute(args));
	}

	@Override
	public Integer call() {
		try {
			if (!Paths.get("").equals(path)) {
				prepareWorkingDirectory();
			}
			loadOriginalConfiguration();
			final Path bikesCfgFile = Paths.get(path.toString(), BikesCfg.FILE_NAME);
			if (!bikesCfgFile.toFile().exists()) {
				createBikesCfg(bikesCfgFile);
			}
			else {
				applyCustomizations(bikesCfgFile);
			}
			final Path bikesInfFile = Paths.get(path.toString(), BikesInf.FILE_NAME);
			createBikesInf(bikesInfFile);
			return ExitCode.OK;
		}
		catch (final Exception e) {
			if (errors) {
				e.printStackTrace();
			}
			return ExitCode.SOFTWARE;
		}
	}

	private void createBikesInf(@NonNull final Path bikesInfFile) throws IOException {
		System.out.print(messages.get("console.message.preparing.new.file", BikesInf.FILE_NAME) + ' ');
		final byte[] bytes = getBikesInf().toByteArray();
		if (bikesInfFile.toFile().exists()) {
			if (Files.isDirectory(bikesInfFile)) {
				System.out.println(messages.get("console.message.error"));
				System.err.println(messages.get("console.error.cannot.open.file.directory", BikesInf.FILE_NAME));
				throw new IOException();
			}
			final ByteArrayOutputStream baos = new ByteArrayOutputStream(BikesInf.FILE_SIZE);
			try (final InputStream is = Files.newInputStream(bikesInfFile)) {
				IOUtils.copy(is, baos, BikesInf.FILE_SIZE);
				System.out.println(messages.get("console.message.done"));
			}
			catch (final IOException e) {
				System.out.println(messages.get("console.message.error"));
				System.err.println(messages.get("console.error.cannot.read.file", BikesInf.FILE_NAME, e));
				throw e;
			}
			if (!Arrays.equals(bytes, baos.toByteArray())) {
				backupBikesInf(bikesInfFile);
				writeBikesInf(bytes, bikesInfFile);
			}
			else {
				System.out.println(messages.get("console.message.already.uptodate", BikesInf.FILE_NAME));
			}
		}
		else {
			System.out.println(messages.get("console.message.done"));
			writeBikesInf(bytes, bikesInfFile);
		}
	}

	private void applyCustomizations(@NonNull final Path bikesCfgFile) throws IOException, UnknownPropertyException, InvalidNumberException, ValueOutOfRangeException {
		System.out.print(messages.get("console.message.applying.customizations") + ' ');
		if (Files.isDirectory(bikesCfgFile)) {
			System.out.println(messages.get("console.message.error"));
			System.err.println(messages.get("console.error.cannot.open.file.directory", BikesCfg.FILE_NAME));
			throw new IOException();
		}
		try {
			final BikesCfg bikesCfg = new BikesCfg(bikesCfgFile);
			short changesCount = 0;
			for (final String name : bikesCfg.getProperties().stringPropertyNames()) {
				final String value = bikesCfg.getProperties().getProperty(name);
				if (applyCustomization(name, value)) {
					changesCount++;
				}
			}
			System.out.println(messages.get("console.message.applying.customizations.done", changesCount));
		}
		catch (final IOException e) {
			System.out.println(messages.get("console.message.error"));
			System.err.println(messages.get("console.error.cannot.read.file", BikesCfg.FILE_NAME, e));
			throw e;
		}
	}

	private boolean applyCustomization(@NonNull final String name, final String value) throws UnknownPropertyException, InvalidNumberException, ValueOutOfRangeException {
		try {
			return applyProperty(name, value, false);
		}
		catch (final UnknownPropertyException e) {
			System.out.println(messages.get("console.message.error"));
			System.err.println(messages.get("console.error.unknown.property", name));
			throw e;
		}
		catch (final InvalidNumberException e) {
			System.out.println(messages.get("console.message.error"));
			System.err.println(messages.get("console.error.invalid.number", name, e.getValue()));
			throw e;
		}
		catch (final ValueOutOfRangeException e) {
			System.out.println(messages.get("console.message.error"));
			System.err.println(messages.get("console.error.value.out.of.range", name, e.getValue(), e.getMinValue(), e.getMaxValue()));
			throw e;
		}
	}

	private void createBikesCfg(@NonNull final Path bikesCfgFile) throws IOException {
		System.out.print(messages.get("console.message.creating.default.file", BikesCfg.FILE_NAME) + ' ');
		try {
			BikesCfg.writeDefault(bikesCfgFile);
			System.out.println(messages.get("console.message.done"));
		}
		catch (final IOException e) {
			System.out.println(messages.get("console.message.error"));
			System.err.println(messages.get("console.error.cannot.create.default.file", BikesCfg.FILE_NAME, e));
			throw e;
		}
	}

	private void backupBikesInf(@NonNull final Path bikesInfFile) throws IOException {
		System.out.print(messages.get("console.message.backup.file", BikesInf.FILE_NAME) + ' ');
		try {
			final Path backupFile = BikesInf.backup(bikesInfFile);
			System.out.println(messages.get("console.message.backup.file.done", backupFile.getFileName()));
		}
		catch (final IOException e) {
			System.out.println(messages.get("console.message.error"));
			System.err.println(messages.get("console.error.cannot.backup.file", BikesInf.FILE_NAME, e));
			throw e;
		}
	}

	private void writeBikesInf(@NonNull final byte[] bytes, @NonNull final Path bikesInfFile) throws IOException {
		System.out.print(messages.get("console.message.writing.new.file", BikesInf.FILE_NAME) + ' ');
		try {
			BikesInf.write(bytes, bikesInfFile);
			System.out.println(messages.get("console.message.done"));
		}
		catch (final IOException e) {
			System.out.println(messages.get("console.message.error"));
			System.err.println(messages.get("console.error.cannot.write.file", BikesInf.FILE_NAME, e));
			throw e;
		}
	}

	private void loadOriginalConfiguration() {
		System.out.print(messages.get("console.message.reading.original.configuration") + ' ');
		setBikesInf(new BikesInf());
		System.out.println(messages.get("console.message.done"));
	}

	private void prepareWorkingDirectory() throws IOException {
		if (path.toFile().exists()) {
			if (!Files.isDirectory(path)) {
				System.err.println(messages.get("console.error.invalid.directory"));
				throw new IOException();
			}
		}
		else {
			System.out.print(messages.get("console.message.creating.working.directory") + ' ');
			try {
				path = Files.createDirectories(path);
				System.out.println(messages.get("console.message.done"));
			}
			catch (final IOException e) {
				System.out.println(messages.get("console.message.error"));
				System.err.println(messages.get("console.error.creating.working.directory", e));
				throw e;
			}
		}
	}

}
