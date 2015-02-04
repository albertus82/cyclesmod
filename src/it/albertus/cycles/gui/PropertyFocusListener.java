package it.albertus.cycles.gui;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

public class PropertyFocusListener extends FocusAdapter {

	private final String defaultValue;

	public PropertyFocusListener(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void focusLost(FocusEvent event) {
		Text field = (Text) event.widget;
		PropertyFormatter formatter = PropertyFormatter.getInstance();
		formatter.clean(field);
		formatter.updateFontStyle(field, defaultValue);
	}

}