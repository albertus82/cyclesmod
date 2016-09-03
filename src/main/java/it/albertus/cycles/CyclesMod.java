package it.albertus.cycles;

import it.albertus.cycles.console.CyclesModConsole;
import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.Version;

public class CyclesMod {

	private static final String ARG_HELP = "--help";
	private static final String ARG_CONSOLE = "-c";

	/** Unique entry point */
	public static final void main(final String args[]) throws Exception {
		if (args.length != 0) {
			if (args[0].trim().equalsIgnoreCase(ARG_HELP)) {
				final Version version = Version.getInstance();
				System.out.println(Messages.get("msg.welcome", version.getNumber(), version.getDate(), Messages.get("msg.info.site")));
				System.out.println();
				System.out.println(Messages.get("msg.help.usage", ARG_CONSOLE, ARG_HELP));
				System.out.println();
				System.out.println("  " + Messages.get("msg.help.option.sourcefile"));
				System.out.println("  " + Messages.get("msg.help.option.console", ARG_CONSOLE));
				System.out.println("  " + Messages.get("msg.help.option.destination"));
				System.out.println("  " + Messages.get("msg.help.option.help", ARG_HELP));
			}
			else if (args.length > 2) {
				System.err.println(Messages.get("err.too.many.parameters", args[2]));
				System.out.println(Messages.get("err.try.help", ARG_HELP));
			}
			else if (args[0].trim().equalsIgnoreCase(ARG_CONSOLE)) {
				CyclesModConsole.start(args.length == 2 ? args[1] : null);
			}
			else {
				CyclesModGui.start(args.length != 0 ? args[0] : null);
			}
		}
		else {
			CyclesModGui.start(null);
		}
	}

}
