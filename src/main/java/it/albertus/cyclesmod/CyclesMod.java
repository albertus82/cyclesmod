package it.albertus.cyclesmod;

import it.albertus.cyclesmod.cli.CyclesModCli;
import it.albertus.cyclesmod.gui.CyclesModGui;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CyclesMod {

	public static void main(final String... args) {
		if (System.getProperty("C") != null || System.getProperty("c") != null) { // -DC
			CyclesModCli.main(args);
		}
		else {
			CyclesModGui.main(args);
		}
	}

}
