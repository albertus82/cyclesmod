package it.albertus.cycles.gui;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.FontData;
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
	public void focusLost(FocusEvent e) {
		Text field = (Text) e.widget;
		if (field != null) {
			field.setText(StringUtils.trimToEmpty(field.getText()));
			updateFontStyle(field, defaultValue);
		}
	}

	public static void updateFontStyle(Text field, String defaultValue) {
		FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (field != null && field.getFont() != null && ArrayUtils.isNotEmpty(field.getFont().getFontData()) && defaultValue != null) {
			final FontData fontData = field.getFont().getFontData()[0];
			if (!defaultValue.equalsIgnoreCase(field.getText())) {
				if (fontData.getStyle() != SWT.BOLD) {
					if (!fontRegistry.hasValueFor("customProperty")) {
						fontData.setStyle(SWT.BOLD);
						fontRegistry.put("customProperty", new FontData[] { fontData });
					}
					field.setFont(fontRegistry.get("customProperty"));
				}
			}
			else {
				if (fontData.getStyle() != SWT.NORMAL) {
					if (!fontRegistry.hasValueFor("defaultProperty")) {
						fontData.setStyle(SWT.NORMAL);
						fontRegistry.put("defaultProperty", new FontData[] { fontData });
					}
					field.setFont(fontRegistry.get("defaultProperty"));
				}
			}
		}
	}

}