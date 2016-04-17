package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class PropertyVerifyListener implements VerifyListener {

	protected final CyclesModGui gui;

	private boolean enabled = true;

	public PropertyVerifyListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void verifyText(final VerifyEvent ve) {
		if (enabled) {
			ve.text = ve.text.trim();
			if (!ve.text.isEmpty() && !gui.isNumeric(ve.text)) {
				ve.doit = false;
			}
			else {
				if (gui.getNumeralSystem().getRadix() > 10 && gui.getNumeralSystem().getRadix() <= 36) {
					ve.text = ve.text.toUpperCase(); // Hex letters case.
				}
			}
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
