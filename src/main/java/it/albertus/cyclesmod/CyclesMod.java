package it.albertus.cyclesmod;

import it.albertus.cyclesmod.cli.CyclesModCli;
import it.albertus.cyclesmod.cli.VersionProvider;
import it.albertus.cyclesmod.gui.CyclesModGui;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Command;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Command(versionProvider = VersionProvider.class)
public class CyclesMod {

	/* Unique entry point */
	public static final void main(final String... args) {
		final String mode = System.getProperty(CyclesMod.class.getName() + ".main.mode");
		if (mode != null) {
			if ("cli".equalsIgnoreCase(mode)) {
				CyclesModCli.main(args[0]);
			}
			else if ("gui".equalsIgnoreCase(mode)) {
				CyclesModGui.main(args.length != 0 ? args[0] : null);
			}
		}
		else {
			// if args[0] is "File" and exists and  isDirectory then cli else gui
		}
	}
}