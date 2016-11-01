package it.albertus.cycles.gui.listener;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.gui.FormProperty;
import it.albertus.jface.SwtUtils;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
public class EditMenuBarArmListener implements ArmListener {

	private final CyclesModGui gui;

	public EditMenuBarArmListener(final CyclesModGui gui) {
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

	protected boolean canCut() {
		return canCopy();
	}

	protected boolean canCopy() {
		for (final FormProperty fp : gui.getTabs().getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().isFocusControl() && fp.getText().getSelectionText() != null && fp.getText().getSelectionText().length() != 0) {
				return true;
			}
		}
		return false;
	}

	protected boolean canPaste() {
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
