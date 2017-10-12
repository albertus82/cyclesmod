package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.FormProperty;
import it.albertus.jface.SwtUtils;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
public class EditMenuListener implements ArmListener, MenuListener {

	private final CyclesModGui gui;

	public EditMenuListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(final ArmEvent e) {
		execute();
	}

	@Override
	public void menuShown(final MenuEvent e) {
		execute();
	}

	@Override
	public void menuHidden(final MenuEvent e) {/* Ignore */}

	private void execute() {
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
