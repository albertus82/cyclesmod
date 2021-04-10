package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.FormProperty;
import it.albertus.jface.SwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
@RequiredArgsConstructor
public class EditMenuListener implements ArmMenuListener {

	@NonNull private final CyclesModGui gui;

	@Override
	public void menuArmed(final TypedEvent e) {
		final MenuItem cutMenuItem = gui.getMenuBar().getEditCutMenuItem();
		cutMenuItem.setEnabled(canCut());

		final MenuItem copyMenuItem = gui.getMenuBar().getEditCopyMenuItem();
		copyMenuItem.setEnabled(canCopy());

		final MenuItem pasteMenuItem = gui.getMenuBar().getEditPasteMenuItem();
		pasteMenuItem.setEnabled(canPaste());
	}

	private boolean canCut() {
		return canCopy();
	}

	private boolean canCopy() {
		for (final FormProperty fp : gui.getTabs().getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().isFocusControl() && fp.getText().getSelectionText() != null && !fp.getText().getSelectionText().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private boolean canPaste() {
		if (SwtUtils.checkClipboard(TextTransfer.getInstance())) {
			for (final FormProperty fp : gui.getTabs().getFormProperties().values()) {
				if (fp != null && fp.getText() != null && fp.getText().isFocusControl()) {
					return true;
				}
			}
		}
		return false;
	}

}
