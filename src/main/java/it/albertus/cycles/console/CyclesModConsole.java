package it.albertus.cycles.console;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Resources;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.Version;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

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
		return Resources.get("msg.welcome", Version.getInstance().getNumber(), Version.getInstance().getDate()) + "\r\n";
	}

	private void execute() throws IOException {
		System.out.println(Resources.get("msg.reading.original.file", BikesInf.FILE_NAME));
		bikesInf = new BikesInf(new BikesZip().getInputStream());

		System.out.println(Resources.get("msg.applying.customizations"));
		customize();

		System.out.println(Resources.get("msg.preparing.new.file", BikesInf.FILE_NAME));
		bikesInf.write(path + BikesInf.FILE_NAME);
	}

	private void customize() throws IOException {
		// Lettura del file di properties BIKES.CFG...
		bikesCfg = new BikesCfg(bikesInf, path);

		// Elaborazione delle properties...
		short changesCount = 0;
		for (Object objectKey : bikesCfg.getProperties().keySet()) {
			String key = (String) objectKey;
			if (applyProperty(key, bikesCfg.getProperties().getProperty(key))) {
				changesCount++;
			}
		}
		System.out.println(Resources.get("msg.customizations.applied", changesCount));
	}

}
