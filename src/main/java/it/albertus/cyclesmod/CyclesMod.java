package it.albertus.cyclesmod;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.logging.Level;

import it.albertus.cyclesmod.cli.CyclesModCli;
import it.albertus.cyclesmod.gui.CyclesModGui;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CyclesMod {

	public static void main(final String... args) {
		if (System.getProperty("C") != null || System.getProperty("c") != null) {
			CyclesModCli.main(args);
		}
		else {
			if (args.length > 0) {
				try {
					CyclesModGui.main(args[0] != null ? Paths.get(args[0]) : null);
				}
				catch (final InvalidPathException e) {
					log.log(Level.FINE, "Invalid path provided: " + args[0], e);
					CyclesModGui.main(null);
				}
			}
			else {
				CyclesModGui.main(null);
			}
		}
	}

}
