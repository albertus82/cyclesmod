package it.albertus.cyclesmod.cli;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import it.albertus.cyclesmod.cli.resources.ConsoleMessages;
import it.albertus.cyclesmod.common.data.DefaultBikes;
import it.albertus.cyclesmod.common.engine.CyclesModEngine;
import it.albertus.cyclesmod.common.model.BikesCfg;
import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.util.logging.LoggingSupport;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
@SuppressWarnings("java:S106") // Replace this use of System.out or System.err by a logger. Standard outputs should not be used directly to log anything (java:S106)
public class CyclesModCli extends CyclesModEngine {

	private static final String DEFAULT_DESTINATION_PATH = "";

	private static final Messages messages = ConsoleMessages.INSTANCE;

	@NonNull private final Path path;

	public static void main(final Path path) {
		if (LoggingSupport.getFormat() == null) {
			LoggingSupport.setFormat("%5$s%6$s%n");
		}
		try {
			new CyclesModCli(path).execute();
		}
		catch (final Exception e) {
			log.log(Level.SEVERE, e.toString(), e);
		}
	}

	CyclesModCli(final Path path) {
		this.path = path != null ? path : Paths.get(DEFAULT_DESTINATION_PATH);
	}

	void execute() throws IOException {
		System.out.println(messages.get("console.message.reading.original.file", BikesInf.FILE_NAME));
		try (final InputStream is = new DefaultBikes().getInputStream()) {
			setBikesInf(new BikesInf(is));
		}

		System.out.println(messages.get("console.message.applying.customizations"));
		customize();

		System.out.println(messages.get("console.message.preparing.new.file", BikesInf.FILE_NAME));
		getBikesInf().write(Paths.get(path.toString(), BikesInf.FILE_NAME), true);
	}

	private void customize() throws IOException {
		// Lettura del file di properties BIKES.CFG...
		final BikesCfg bikesCfg = new BikesCfg(getBikesInf(), path);

		// Elaborazione delle properties...
		short changesCount = 0;
		for (final String key : bikesCfg.getProperties().stringPropertyNames()) {
			if (applyProperty(key, bikesCfg.getProperties().getProperty(key), false)) {
				changesCount++;
			}
		}
		System.out.println(messages.get("console.message.customizations.applied", changesCount));
	}

}
