package it.albertus.cyclesmod.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import it.albertus.cyclesmod.cli.resources.ConsoleMessages;
import it.albertus.cyclesmod.common.data.DefaultBikes;
import it.albertus.cyclesmod.common.engine.CyclesModEngine;
import it.albertus.cyclesmod.common.model.BikesCfg;
import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.util.IOUtils;
import it.albertus.util.logging.LoggingSupport;
import lombok.extern.java.Log;

@Log
public class CyclesModCli extends CyclesModEngine {

	static {
		if (LoggingSupport.getFormat() == null) {
			LoggingSupport.setFormat("%5$s%6$s%n");
		}
	}

	private static final String DEFAULT_DESTINATION_PATH = "";

	private static final Messages messages = ConsoleMessages.INSTANCE;

	private final String path;

	private CyclesModCli(String path) {
		this.path = path;
	}

	public static void main(final String providedPath) {
		try {
			final String path;
			if (providedPath == null) {
				path = DEFAULT_DESTINATION_PATH;
			}
			else if (!providedPath.isEmpty() && !providedPath.endsWith("/") && !providedPath.endsWith("\\") && !providedPath.endsWith(File.separator)) {
				path = providedPath + File.separator;
			}
			else {
				path = providedPath;
			}

			new CyclesModCli(path).execute();
		}
		catch (final Exception e) {
			log.log(Level.SEVERE, e.toString(), e);
		}
	}

	private void execute() throws IOException {
		System.out.println(messages.get("console.message.reading.original.file", BikesInf.FILE_NAME));
		InputStream is = null;
		try {
			is = new DefaultBikes().getInputStream();
			setBikesInf(new BikesInf(is));
		}
		finally {
			IOUtils.closeQuietly(is);
		}

		System.out.println(messages.get("console.message.applying.customizations"));
		customize();

		System.out.println(messages.get("console.message.preparing.new.file", BikesInf.FILE_NAME));
		getBikesInf().write(path + BikesInf.FILE_NAME, true);
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
