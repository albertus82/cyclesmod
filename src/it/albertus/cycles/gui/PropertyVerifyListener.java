package it.albertus.cycles.gui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class PropertyVerifyListener implements Listener {

	public void handleEvent(Event event) {
		if (!StringUtils.isNumeric(event.text)) {
			event.doit = false;
		}
	}

}
