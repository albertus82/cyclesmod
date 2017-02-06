package it.albertus.cycles.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.cycles.data.DefaultBikes;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.IOUtils;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class CyclesModConsole extends CyclesModEngine {

	private static final Logger logger = LoggerFactory.getLogger(CyclesModConsole.class);

	private static final String DEFAULT_DESTINATION_PATH = "";

	private final String path;

	private CyclesModConsole(String path) {
		this.path = path;
	}

	public static void start(final String providedPath) {
		try {
			System.out.println(getWelcomeMessage());

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

			new CyclesModConsole(path).execute();
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	private static String getWelcomeMessage() throws IOException {
		return Messages.get("msg.welcome", Version.getInstance().getNumber(), Version.getInstance().getDate(), Messages.get("msg.info.site")) + "\r\n";
	}

	private void execute() throws IOException {
		System.out.println(Messages.get("msg.reading.original.file", BikesInf.FILE_NAME));
		InputStream is = null;
		try {
			is = new DefaultBikes().getInputStream();
			setBikesInf(new BikesInf(is));
		}
		finally {
			IOUtils.closeQuietly(is);
		}

		System.out.println(Messages.get("msg.applying.customizations"));
		customize();

		System.out.println(Messages.get("msg.preparing.new.file", BikesInf.FILE_NAME));
		getBikesInf().write(path + BikesInf.FILE_NAME);
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
		System.out.println(Messages.get("msg.customizations.applied", changesCount));
	}

}
