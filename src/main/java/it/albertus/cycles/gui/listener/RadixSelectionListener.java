package it.albertus.cycles.gui.listener;

import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.gui.CyclesModGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class RadixSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public RadixSelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (gui.getMenuBar().getViewRadix10MenuItem().getSelection()) {
			if (CyclesModEngine.getRadix() != 10) {
				CyclesModEngine.setRadix(10);
				gui.updateFormValues();
			}
		}
		else if (gui.getMenuBar().getViewRadix16MenuItem().getSelection()) {
			if (CyclesModEngine.getRadix() != 16) {
				CyclesModEngine.setRadix(16);
				gui.updateFormValues();
			}
		}
	}

}
