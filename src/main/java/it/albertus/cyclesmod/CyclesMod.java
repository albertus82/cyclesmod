package it.albertus.cyclesmod;

import java.nio.file.Files;
import java.nio.file.Paths;

import it.albertus.cyclesmod.cli.CyclesModCli;
import it.albertus.cyclesmod.gui.CyclesModGui;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CyclesMod {

	public static void main(final String... args) {
		final String mode = System.getProperty(CyclesMod.class.getName() + ".main.mode");
		if (mode != null) {
			if ("cli".equalsIgnoreCase(mode)) {
				CyclesModCli.main(args);
			}
			else if ("gui".equalsIgnoreCase(mode)) {
				CyclesModGui.main(args[0] != null ? Paths.get(args[0]) : null);
			}
		}
		else {
			if (args[0] != null && Files.isDirectory(Paths.get(args[0]))) {
				CyclesModCli.main(args);
			}
			else {
				CyclesModGui.main(args[0] != null ? Paths.get(args[0]) : null);
			}
		}
	}

}
