package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.gui.FormProperty;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.MenuItem;

public class EditArmListener implements ArmListener {

	private final CyclesModGui gui;

	public EditArmListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(ArmEvent e) {
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
		for (final FormProperty fp : gui.getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().getSelectionText() != null && fp.getText().getSelectionText().length() != 0) {
				return true;
			}
		}
		return false;
	}

	private boolean canPaste() {
		for (final FormProperty fp : gui.getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().isFocusControl()) {
				return true;
			}
		}
		return false;
	}

}
