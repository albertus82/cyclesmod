package io.github.albertus82.cyclesmod.gui.listener;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.TypedEvent;

import io.github.albertus82.cyclesmod.gui.CyclesModGui;
import io.github.albertus82.cyclesmod.gui.model.FormProperty;
import io.github.albertus82.jface.SwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
@RequiredArgsConstructor
public class EditMenuListener implements ArmMenuListener {

	@NonNull
	private final CyclesModGui gui;

	@Override
	public void menuArmed(final TypedEvent e) {
		gui.getMenuBar().getEditCutMenuItem().setEnabled(canCut());
		gui.getMenuBar().getEditCopyMenuItem().setEnabled(canCopy());
		gui.getMenuBar().getEditPasteMenuItem().setEnabled(canPaste());
	}

	private boolean canCut() {
		return canCopy();
	}

	private boolean canCopy() {
		for (final FormProperty fp : gui.getTabs().getFormProperties().get(gui.getMode()).values()) {
			if (fp != null && fp.getText() != null && fp.getText().isFocusControl() && fp.getText().getSelectionText() != null && !fp.getText().getSelectionText().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private boolean canPaste() {
		if (SwtUtils.checkClipboard(TextTransfer.getInstance())) {
			for (final FormProperty fp : gui.getTabs().getFormProperties().get(gui.getMode()).values()) {
				if (fp != null && fp.getText() != null && fp.getText().isFocusControl()) {
					return true;
				}
			}
		}
		return false;
	}

}
