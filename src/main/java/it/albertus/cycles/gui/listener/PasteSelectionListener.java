package it.albertus.cycles.gui.listener;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.gui.FormProperty;
import it.albertus.jface.SwtUtils;

public class PasteSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public PasteSelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (SwtUtils.checkClipboard(TextTransfer.getInstance())) {
			for (final FormProperty fp : gui.getTabs().getFormProperties().values()) {
				if (fp != null && fp.getText() != null && fp.getText().isFocusControl()) {
					fp.getText().paste();
					break;
				}
			}
		}
	}

}
