package it.albertus.cycles.gui;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Text;

public class PropertyFormatter {

	private static final PropertyFormatter instance = new PropertyFormatter();

	private PropertyFormatter() {}

	public static PropertyFormatter getInstance() {
		return instance;
	}

	public void clean(Text field) {
		if (field != null && StringUtils.isNumeric(field.getText()) && StringUtils.isNotEmpty(field.getText())) {
			field.setText(Integer.valueOf(field.getText()).toString());
		}
	}

	public void updateFontStyle(Text field, String defaultValue) {
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
