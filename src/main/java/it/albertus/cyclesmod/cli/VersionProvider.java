package it.albertus.cyclesmod.cli;

import java.text.DateFormat;
import java.text.ParseException;

import it.albertus.cyclesmod.cli.resources.ConsoleMessages;
import it.albertus.util.Version;
import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {

	private static final ConsoleMessages messages = ConsoleMessages.INSTANCE;

	@Override
	public String[] getVersion() throws ParseException {
		return new String[] { messages.get("console.version", Version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM).format(Version.getDate())) };
	}

}
