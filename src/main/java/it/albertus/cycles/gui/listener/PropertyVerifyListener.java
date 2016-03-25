package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

public class PropertyVerifyListener implements VerifyListener {

	protected final CyclesModGui gui;

	public PropertyVerifyListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void verifyText(final VerifyEvent ve) {
		final Text text = (Text) ve.widget;
		if (!text.getText().equals(ve.text)) {
			if (!gui.isNumeric(ve.text) && !StringUtils.isNumeric(ve.text)) {
				ve.doit = false;
			}
			else {
				if (gui.getNumeralSystem().getRadix() > 10) {
					ve.text = ve.text.toUpperCase(); // Hex letters case.
				}
			}
		}
	}

}
