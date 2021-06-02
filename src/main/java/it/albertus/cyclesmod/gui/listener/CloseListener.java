package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.cyclesmod.gui.CyclesModGui;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CloseListener extends SelectionAdapter {

	@NonNull private final CyclesModGui gui;

	private boolean canClose() {
		return gui.askForSavingAndExport();
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (canClose()) {
			gui.close();
		}
	}

}
