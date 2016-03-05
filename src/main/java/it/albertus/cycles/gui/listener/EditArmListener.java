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
		final MenuItem copyMenuItem = gui.getMenuBar().getEditCopyMenuItem();
		for (final FormProperty fp : gui.getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().getSelectionText() != null && fp.getText().getSelectionText().length() != 0) {
				copyMenuItem.setEnabled(true);
				return;
			}
		}
		copyMenuItem.setEnabled(false);
	}

}
