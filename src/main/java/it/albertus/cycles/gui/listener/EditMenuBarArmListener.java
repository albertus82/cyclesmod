package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.MenuItem;

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
		cutMenuItem.setEnabled(gui.canCut());

		final MenuItem copyMenuItem = gui.getMenuBar().getEditCopyMenuItem();
		copyMenuItem.setEnabled(gui.canCopy());

		final MenuItem pasteMenuItem = gui.getMenuBar().getEditPasteMenuItem();
		pasteMenuItem.setEnabled(gui.canPaste());
	}

}
