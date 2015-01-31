package it.albertus.cycles;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.ExceptionUtils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclesModCmd extends CyclesModEngine {

	private static final Logger log = LoggerFactory.getLogger(CyclesModCmd.class);

	private static final String DEFAULT_DESTINATION_PATH = "";

	private BikesCfg bikesCfg;
	private final String path;

	private CyclesModCmd(String path) {
		this.path = path;
	}

	public static void main(final String... args) throws Exception {
		try {
			log.info(getWelcomeMessage());

			// Gestione parametri da riga di comando...
			if (args.length > 1) {
				throw new IllegalArgumentException(Messages.get("err.too.many.parameters") + ' ' + Messages.get("msg.command.line.help", CyclesModCmd.class.getSimpleName()));
			}
			String path = args.length == 1 ? args[0] : DEFAULT_DESTINATION_PATH;

			if (path.contains("?") || StringUtils.startsWithIgnoreCase(path, "-help") || StringUtils.startsWithIgnoreCase(path, "/help")) {
				log.info(Messages.get("msg.command.line.help", CyclesModCmd.class.getSimpleName()));
				return;
			}
			if (!"".equals(path) && !path.endsWith("/") && !path.endsWith("\\") && !path.endsWith(File.separator)) {
				path += File.separator;
			}

			new CyclesModCmd(path).execute();
		}
		catch (Exception e) {
			if (StringUtils.isNotBlank(e.getLocalizedMessage()) || StringUtils.isNotBlank(e.getMessage())) {
				log.error(ExceptionUtils.getLogMessage(e));
			}
			else {
				throw e; // Exceptions without message are thrown by default.
			}
		}
	}

	private static String getWelcomeMessage() throws IOException {
		return Messages.get("msg.welcome", version.get("version.number"), version.get("version.date")) + "\r\n";
	}

	private void execute() throws IOException {
		log.info(Messages.get("msg.reading.original.file", BikesInf.FILE_NAME));
		bikesInf = new BikesInf(new BikesZip().getInputStream());

		log.info(Messages.get("msg.applying.customizations"));
		customize();

		log.info(Messages.get("msg.preparing.new.file", BikesInf.FILE_NAME));
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
		log.info(Messages.get("msg.customizations.applied", changesCount));
	}

}