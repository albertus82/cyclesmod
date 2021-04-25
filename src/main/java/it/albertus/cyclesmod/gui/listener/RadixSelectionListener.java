package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.common.engine.NumeralSystem;
import it.albertus.cyclesmod.gui.CyclesModGui;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RadixSelectionListener extends SelectionAdapter {

	@NonNull private final CyclesModGui gui;

	@Override
	public void widgetSelected(@NonNull final SelectionEvent se) {
		if (se.widget instanceof MenuItem) {
			final MenuItem menuItem = (MenuItem) se.widget;
			if (menuItem.getSelection() && !menuItem.getData().equals(gui.getNumeralSystem()) && menuItem.getData() instanceof NumeralSystem) {
				gui.setNumeralSystem((NumeralSystem) menuItem.getData());
			}
		}
	}

}
