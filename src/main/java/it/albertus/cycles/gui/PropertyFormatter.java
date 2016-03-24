package it.albertus.cycles.gui;

import it.albertus.cycles.engine.CyclesModEngine;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Text;

public class PropertyFormatter {

	private static final PropertyFormatter INSTANCE = new PropertyFormatter();

	public static PropertyFormatter getInstance() {
		return INSTANCE;
	}

	private final FontRegistry fontRegistry = JFaceResources.getFontRegistry();

	public void clean(final Text field) {
		if (field != null && CyclesModEngine.isNumeric(field.getText()) && StringUtils.isNotEmpty(field.getText())) {
			field.setText(Integer.toString(Integer.valueOf(field.getText(), CyclesModEngine.getRadix()), CyclesModEngine.getRadix()));
		}
	}

	public void updateFontStyle(final Text field) {
		if (field != null && field.getFont() != null && ArrayUtils.isNotEmpty(field.getFont().getFontData()) && field.getData(FormProperty.KEY_DEFAULT_VALUE) instanceof Integer) {
			final Integer defaultValue = (Integer) field.getData(FormProperty.KEY_DEFAULT_VALUE);
			if (!defaultValue.equals(Integer.valueOf(field.getText(), CyclesModGui.getRadix()))) {
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
