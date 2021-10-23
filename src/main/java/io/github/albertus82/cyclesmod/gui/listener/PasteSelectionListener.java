package io.github.albertus82.cyclesmod.gui.listener;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import io.github.albertus82.cyclesmod.gui.CyclesModGui;
import io.github.albertus82.cyclesmod.gui.model.FormProperty;
import io.github.albertus82.jface.SwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PasteSelectionListener extends SelectionAdapter {

	@NonNull
	private final CyclesModGui gui;

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (SwtUtils.checkClipboard(TextTransfer.getInstance())) {
			for (final FormProperty fp : gui.getTabs().getFormProperties().get(gui.getMode()).values()) {
				if (fp != null && fp.getText() != null && fp.getText().isFocusControl()) {
					fp.getText().paste();
					break;
				}
			}
		}
	}

}
