package com.github.albertus82.cyclesmod;

import com.github.albertus82.cyclesmod.cli.CyclesModCli;
import com.github.albertus82.cyclesmod.gui.CyclesModGui;

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
