package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

public class PropertyVerifyListener implements VerifyListener {

	protected final CyclesModGui gui;

	private boolean enabled = true;

	public PropertyVerifyListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void verifyText(final VerifyEvent ve) {
		if (enabled) {
			final Text text = (Text) ve.widget;
			final String oldText = text.getText();
			final String newText = oldText.substring(0, ve.start) + ve.text + oldText.substring(ve.end);

			if (!oldText.equals(newText)) {
				if (!gui.isNumeric(newText.trim()) && newText.trim().length() > 0) {
					ve.doit = false;
				}
				else {
					if (gui.getNumeralSystem().getRadix() > 10 && gui.getNumeralSystem().getRadix() <= 36) {
						ve.text = ve.text.toUpperCase(); // Hex letters case.
					}
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
