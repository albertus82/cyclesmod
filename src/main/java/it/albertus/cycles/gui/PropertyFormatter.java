package it.albertus.cycles.gui;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Text;

public class PropertyFormatter {

	private static final PropertyFormatter INSTANCE = new PropertyFormatter();

	private PropertyFormatter() {
		fontRegistry = JFaceResources.getFontRegistry();
	}

	public static PropertyFormatter getInstance() {
		return INSTANCE;
	}

	private final FontRegistry fontRegistry;

	public void clean(Text field) {
		if (field != null && StringUtils.isNumeric(field.getText()) && StringUtils.isNotEmpty(field.getText())) {
			field.setText(Integer.valueOf(field.getText()).toString());
		}
	}

	public void updateFontStyle(Text field, String defaultValue) {
		if (field != null && field.getFont() != null && ArrayUtils.isNotEmpty(field.getFont().getFontData()) && defaultValue != null) {
			if (!defaultValue.equalsIgnoreCase(field.getText())) {
				if (field.getFont().getFontData()[0].getStyle() != SWT.BOLD) {
					setBoldFontStyle(field);
				}
			}
			else {
				if (field.getFont().getFontData()[0].getStyle() != SWT.NORMAL) {
					setNormalFontStyle(field);
				}
			}
		}
	}

	public void setNormalFontStyle(Text field) {
		final FontData fontData = field.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("defaultProperty")) {
			fontData.setStyle(SWT.NORMAL);
			fontRegistry.put("defaultProperty", new FontData[] { fontData });
		}
		field.setFont(fontRegistry.get("defaultProperty"));
	}

	public void setBoldFontStyle(Text field) {
		final FontData fontData = field.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("customProperty")) {
			fontData.setStyle(SWT.BOLD);
			fontRegistry.put("customProperty", new FontData[] { fontData });
		}
		field.setFont(fontRegistry.get("customProperty"));
	}

}
