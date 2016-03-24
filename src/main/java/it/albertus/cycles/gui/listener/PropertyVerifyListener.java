package it.albertus.cycles.gui.listener;

import it.albertus.cycles.engine.CyclesModEngine;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class PropertyVerifyListener implements Listener {

	public void handleEvent(Event event) {
		if (!CyclesModEngine.isNumeric(event.text) && !StringUtils.isNumeric(event.text)) {
			event.doit = false;
		}
		else {
			event.text = event.text.toUpperCase(); // Hex letters case.
		}
	}

}
