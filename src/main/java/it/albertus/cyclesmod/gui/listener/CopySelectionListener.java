package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.FormProperty;

public class CopySelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public CopySelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		for (final FormProperty fp : gui.getTabs().getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().isFocusControl() && fp.getText().getSelectionText() != null && !fp.getText().getSelectionText().isEmpty()) {
				fp.getText().copy();
				break;
			}
		}
	}

}
