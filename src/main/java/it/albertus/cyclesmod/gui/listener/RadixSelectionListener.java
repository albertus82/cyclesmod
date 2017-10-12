package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.engine.NumeralSystem;
import it.albertus.cyclesmod.gui.CyclesModGui;

public class RadixSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public RadixSelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final MenuItem menuItem = (MenuItem) se.widget;
		if (menuItem.getSelection() && !menuItem.getData().equals(gui.getNumeralSystem())) {
			gui.setNumeralSystem((NumeralSystem) menuItem.getData());
		}
	}

}
