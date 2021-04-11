package it.albertus.cyclesmod;

import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.console.CyclesModConsole;
import it.albertus.cyclesmod.gui.CyclesModGui;

public class CyclesMod {

	private static final String ARG_HELP = "--help";
	private static final String ARG_CONSOLE = "-c";

	private CyclesMod() {
		throw new IllegalAccessError();
	}

	/* Unique entry point */
	public static final void main(final String[] args) {
		final String mode = System.getProperty(CyclesMod.class.getName() + ".main.mode");
		if (mode != null) {
			if ("console".equalsIgnoreCase(mode)) {
				if (args.length > 1) {
					printTooManyArgs(args[1]);
				}
				else if (args.length > 0 && args[0].trim().equalsIgnoreCase(ARG_HELP)) {
					printHelp(mode);
				}
				else {
					CyclesModConsole.start(args.length == 1 ? args[0] : null);
				}
			}
			else if ("gui".equalsIgnoreCase(mode)) {
				CyclesModGui.start(args.length != 0 ? args[0] : null);
			}
		}
		else {
			if (args.length != 0) {
				if (args[0].trim().equalsIgnoreCase(ARG_HELP)) {
					printHelp(mode);
				}
				else if (args.length > 2) {
					printTooManyArgs(args[2]);
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

	private static void printTooManyArgs(final String arg) {
		System.err.println(Messages.get("err.too.many.parameters", arg));
		System.out.println(Messages.get("err.try.help", ARG_HELP));
	}

	private static void printHelp(final String mode) {
		System.out.println(CyclesModConsole.getWelcomeMessage());
		if ("console".equalsIgnoreCase(mode)) {
			System.out.println(Messages.get("msg.help.usage.console", ARG_HELP));
		}
		else {
			System.out.println(Messages.get("msg.help.usage.generic", ARG_CONSOLE, ARG_HELP));
		}
		System.out.println();
		if (!"console".equalsIgnoreCase(mode)) {
			System.out.println("  " + Messages.get("msg.help.option.sourcefile"));
			System.out.println("  " + Messages.get("msg.help.option.console", ARG_CONSOLE));
		}
		System.out.println("  " + Messages.get("msg.help.option.destination"));
		System.out.println("  " + Messages.get("msg.help.option.help", ARG_HELP));
	}

}
