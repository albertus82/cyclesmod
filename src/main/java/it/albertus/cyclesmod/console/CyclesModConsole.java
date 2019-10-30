package it.albertus.cyclesmod.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.cyclesmod.data.DefaultBikes;
import it.albertus.cyclesmod.engine.CyclesModEngine;
import it.albertus.cyclesmod.model.BikesCfg;
import it.albertus.cyclesmod.model.BikesInf;
import it.albertus.cyclesmod.resources.Messages;
import it.albertus.util.IOUtils;
import it.albertus.util.NewLine;
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

	public static String getWelcomeMessage() {
		return Messages.get("msg.welcome", Version.getInstance().getNumber(), getFormattedVersionDate(), Messages.get("msg.info.site")) + NewLine.SYSTEM_LINE_SEPARATOR;
	}

	private static String getFormattedVersionDate() {
		Date date;
		try {
			date = Version.getInstance().getDate();
		}
		catch (final Exception e) {
			date = new Date();
		}
		return DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(date);
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
		System.out.println(Messages.get("msg.customizations.applied", changesCount));
	}

}
