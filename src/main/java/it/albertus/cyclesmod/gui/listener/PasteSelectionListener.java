package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.model.FormProperty;
import it.albertus.jface.SwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PasteSelectionListener extends SelectionAdapter {

	@NonNull private final CyclesModGui gui;

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (SwtUtils.checkClipboard(TextTransfer.getInstance())) {
			for (final FormProperty fp : gui.getTabs().getFormProperties().get(gui.getMode().getGame()).values()) {
				if (fp != null && fp.getText() != null && fp.getText().isFocusControl()) {
					fp.getText().paste();
					break;
				}
			}
		}
	}

}
