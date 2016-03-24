package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.gui.FormProperty;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CutSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public CutSelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canCut()) {
			cut();
		}
	}

	public void cut() {
		for (final FormProperty fp : gui.getTabs().getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().getSelectionText() != null && fp.getText().getSelectionText().length() != 0) {
				fp.getText().cut();
				break;
			}
		}
	}

}
