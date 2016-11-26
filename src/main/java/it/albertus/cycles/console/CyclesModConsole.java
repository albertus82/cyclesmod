package it.albertus.cycles.console;

import java.io.File;
import java.io.IOException;

import it.albertus.cycles.data.DefaultBikes;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.StringUtils;
import it.albertus.util.Version;

public class CyclesModConsole extends CyclesModEngine {

	private static final String DEFAULT_DESTINATION_PATH = "";

	private BikesCfg bikesCfg;
	private final String path;

	private CyclesModConsole(String path) {
		this.path = path;
	}

	public static void start(String path) throws Exception {
		try {
			System.out.println(getWelcomeMessage());

			if (path == null) {
				path = DEFAULT_DESTINATION_PATH;
			}

			if (!"".equals(path) && !path.endsWith("/") && !path.endsWith("\\") && !path.endsWith(File.separator)) {
				path += File.separator;
			}

			new CyclesModConsole(path).execute();
		}
		catch (Exception e) {
			if (StringUtils.isNotBlank(e.getLocalizedMessage()) || StringUtils.isNotBlank(e.getMessage())) {
				System.err.println(ExceptionUtils.getLogMessage(e));
			}
			else {
				throw e; // Exceptions without message are thrown by default.
			}
		}
	}

	private static String getWelcomeMessage() throws IOException {
		return Messages.get("msg.welcome", Version.getInstance().getNumber(), Version.getInstance().getDate(), Messages.get("msg.info.site")) + "\r\n";
	}

	private void execute() throws IOException {
		System.out.println(Messages.get("msg.reading.original.file", BikesInf.FILE_NAME));
		setBikesInf(new BikesInf(new DefaultBikes().getInputStream()));

		System.out.println(Messages.get("msg.applying.customizations"));
		customize();

		System.out.println(Messages.get("msg.preparing.new.file", BikesInf.FILE_NAME));
		getBikesInf().write(path + BikesInf.FILE_NAME);
	}

	private void customize() throws IOException {
		// Lettura del file di properties BIKES.CFG...
		bikesCfg = new BikesCfg(getBikesInf(), path);

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
