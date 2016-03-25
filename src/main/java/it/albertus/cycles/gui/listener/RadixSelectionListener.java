package it.albertus.cycles.gui.listener;

import it.albertus.cycles.engine.NumeralSystem;
import it.albertus.cycles.gui.CyclesModGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;

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
