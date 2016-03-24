package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class RadixSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public RadixSelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent se) {
		if (gui.getMenuBar().getViewRadix10MenuItem().getSelection()) {
			if (gui.getRadix() != 10) {
				gui.setRadix(10);
			}
		}
		else if (gui.getMenuBar().getViewRadix16MenuItem().getSelection()) {
			if (gui.getRadix() != 16) {
				gui.setRadix(16);
			}
		}
	}

}
