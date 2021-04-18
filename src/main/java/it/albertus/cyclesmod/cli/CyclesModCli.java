package it.albertus.cyclesmod.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import it.albertus.cyclesmod.cli.resources.ConsoleMessages;
import it.albertus.cyclesmod.common.engine.CyclesModEngine;
import it.albertus.cyclesmod.common.model.BikesCfg;
import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.util.IOUtils;
import picocli.CommandLine.ExitCode;

@SuppressWarnings("java:S106") // Replace this use of System.out or System.err by a logger. Standard outputs should not be used directly to log anything (java:S106)
public class CyclesModCli extends CyclesModEngine {

	private static final Path DEFAULT_WORKING_DIRECTORY = Paths.get("");

	private static final Messages messages = ConsoleMessages.INSTANCE;

	public int execute(Path path) {
		path = path != null ? path : DEFAULT_WORKING_DIRECTORY;
		if (!DEFAULT_WORKING_DIRECTORY.equals(path)) {
			if (path.toFile().exists()) {
				if (!Files.isDirectory(path)) {
					System.err.println(messages.get("console.error.invalid.directory"));
					return ExitCode.SOFTWARE;
				}
			}
			else {
				System.out.print(messages.get("console.message.creating.working.directory") + ' ');
				try {
					path = Files.createDirectories(path);
				}
				catch (final IOException e) {
					System.out.println(messages.get("console.message.error"));
					System.err.println(messages.get("console.error.creating.working.directory", e));
					return ExitCode.SOFTWARE;
				}
				System.out.println(messages.get("console.message.done"));
			}
		}

		System.out.print(messages.get("console.message.reading.original.configuration") + ' ');
		setBikesInf(new BikesInf());
		System.out.println(messages.get("console.message.done"));

		final Path bikesCfgFile = Paths.get(path.toString(), BikesCfg.FILE_NAME);
		if (!bikesCfgFile.toFile().exists()) {
			System.out.print(messages.get("console.message.creating.default.file", BikesCfg.FILE_NAME) + ' ');
			try {
				BikesCfg.writeDefault(bikesCfgFile);
			}
			catch (final IOException e) {
				System.out.println(messages.get("console.message.error"));
				System.err.println(messages.get("console.error.cannot.create.default.file", BikesCfg.FILE_NAME, e));
				return ExitCode.SOFTWARE;
			}
			System.out.println(messages.get("console.message.done"));
		}
		else {
			System.out.print(messages.get("console.message.applying.customizations") + ' ');
			if (Files.isDirectory(bikesCfgFile)) {
				System.out.println(messages.get("console.message.error"));
				System.err.println(messages.get("console.error.cannot.open.file.directory", BikesCfg.FILE_NAME));
				return ExitCode.SOFTWARE;
			}
			try {
				final BikesCfg bikesCfg = new BikesCfg(bikesCfgFile);
				short changesCount = 0;
				for (final String key : bikesCfg.getProperties().stringPropertyNames()) {
					if (applyProperty(key, bikesCfg.getProperties().getProperty(key), false)) {
						changesCount++;
					}
				}
				System.out.println(messages.get("console.message.applying.customizations.done", changesCount));
			}
			catch (final IOException e) {
				System.out.println(messages.get("console.message.error"));
				System.err.println(messages.get("console.error.cannot.read.file", BikesCfg.FILE_NAME, e));
				return ExitCode.SOFTWARE;
			}
		}

		System.out.print(messages.get("console.message.preparing.new.file", BikesInf.FILE_NAME) + ' ');
		final byte[] bytes = getBikesInf().toByteArray();
		final Path bikesInfFile = Paths.get(path.toString(), BikesInf.FILE_NAME);
		if (bikesInfFile.toFile().exists()) {
			if (Files.isDirectory(bikesInfFile)) {
				System.out.println(messages.get("console.message.error"));
				System.err.println(messages.get("console.error.cannot.open.file.directory", BikesInf.FILE_NAME));
				return ExitCode.SOFTWARE;
			}
			try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(BikesInf.FILE_SIZE)) {
				try (final InputStream is = Files.newInputStream(bikesInfFile)) {
					IOUtils.copy(is, baos, BikesInf.FILE_SIZE);
				}
				catch (final IOException e) {
					System.out.println(messages.get("console.message.error"));
					System.err.println(messages.get("console.error.cannot.read.file", BikesInf.FILE_NAME, e));
					return ExitCode.SOFTWARE;
				}
				System.out.println(messages.get("console.message.done"));
				if (!Arrays.equals(bytes, baos.toByteArray())) {
					System.out.print(messages.get("console.message.backup.file", BikesInf.FILE_NAME) + ' ');
					final Path backupFile = BikesInf.backup(bikesInfFile);
					System.out.println(messages.get("console.message.backup.file.done", backupFile.getFileName()));

					System.out.print(messages.get("console.message.writing.new.file", BikesInf.FILE_NAME) + ' ');
					BikesInf.write(bytes, bikesInfFile);
					System.out.println(messages.get("console.message.done"));
				}
				else {
					System.out.println(messages.get("console.message.already.uptodate", BikesInf.FILE_NAME));
				}
			}
			catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		else {
			System.out.println(messages.get("console.message.done"));

			System.out.print(messages.get("console.message.writing.new.file", BikesInf.FILE_NAME) + ' ');
			try {
				BikesInf.write(bytes, bikesInfFile);
			}
			catch (final IOException e) {
				System.out.println(messages.get("console.message.error"));
				System.err.println(messages.get("console.error.cannot.write.file", BikesInf.FILE_NAME, e));
				return ExitCode.SOFTWARE;
			}
			System.out.println(messages.get("console.message.done"));
		}
		return ExitCode.OK;
	}

}
