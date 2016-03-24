package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.PropertyFormatter;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

public class PropertyFocusListener extends FocusAdapter {

	@Override
	public void focusLost(FocusEvent event) {
		Text field = (Text) event.widget;
		PropertyFormatter formatter = PropertyFormatter.getInstance();
		formatter.clean(field);
		formatter.updateFontStyle(field);
	}

}
