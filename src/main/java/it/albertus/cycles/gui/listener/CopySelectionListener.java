package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.gui.FormProperty;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CopySelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public CopySelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		copySelection();
	}

	public void copySelection() {
		for (final FormProperty fp : gui.getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().getSelectionText() != null && fp.getText().getSelectionText().length() != 0) {
				final Clipboard clipboard = new Clipboard(gui.getShell().getDisplay());
				clipboard.setContents(new String[] { fp.getText().getSelectionText() }, new TextTransfer[] { TextTransfer.getInstance() });
				clipboard.dispose();
				break;
			}
		}
	}

}
