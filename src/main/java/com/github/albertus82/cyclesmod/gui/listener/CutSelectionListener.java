package com.github.albertus82.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.github.albertus82.cyclesmod.gui.CyclesModGui;
import com.github.albertus82.cyclesmod.gui.model.FormProperty;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CutSelectionListener extends SelectionAdapter {

	@NonNull
	private final CyclesModGui gui;

	@Override
	public void widgetSelected(final SelectionEvent se) {
		for (final FormProperty fp : gui.getTabs().getFormProperties().get(gui.getMode()).values()) {
			if (fp != null && fp.getText() != null && fp.getText().isFocusControl() && fp.getText().getSelectionText() != null && !fp.getText().getSelectionText().isEmpty()) {
				fp.getText().cut();
				break;
			}
		}
	}

}
