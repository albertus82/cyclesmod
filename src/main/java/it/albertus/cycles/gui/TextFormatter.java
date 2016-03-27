package it.albertus.cycles.gui;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Text;

public class TextFormatter {

	private final CyclesModGui gui;
	private final FontRegistry fontRegistry;

	TextFormatter(final CyclesModGui gui) {
		this.gui = gui;
		this.fontRegistry = JFaceResources.getFontRegistry();
	}

	public void clean(final Text text) {
		if (text != null) {
			if (gui.isNumeric(text.getText().trim())) {
				text.setText(Integer.toString(Integer.parseInt(text.getText().trim(), gui.getNumeralSystem().getRadix()), gui.getNumeralSystem().getRadix()));
			}
			else {
				text.setText("0");
			}
		}
	}

	public void updateFontStyle(final Text text) {
		if (text != null && gui.isNumeric(text.getText()) && text.getFont() != null && ArrayUtils.isNotEmpty(text.getFont().getFontData()) && text.getData(FormProperty.TextDataKey.DEFAULT.toString()) instanceof Integer) {
			final Integer defaultValue = (Integer) text.getData(FormProperty.TextDataKey.DEFAULT.toString());
			if (!defaultValue.equals(Integer.valueOf(text.getText(), gui.getNumeralSystem().getRadix()))) {
				if (text.getFont().getFontData()[0].getStyle() != SWT.BOLD) {
					setBoldFontStyle(text);
				}
			}
			else {
				if (text.getFont().getFontData()[0].getStyle() != SWT.NORMAL) {
					setNormalFontStyle(text);
				}
			}
		}
	}

	public void setNormalFontStyle(final Text field) {
		final FontData fontData = field.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("defaultProperty")) {
			fontData.setStyle(SWT.NORMAL);
			fontRegistry.put("defaultProperty", new FontData[] { fontData });
		}
		field.setFont(fontRegistry.get("defaultProperty"));
	}

	public void setBoldFontStyle(final Text field) {
		final FontData fontData = field.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("customProperty")) {
			fontData.setStyle(SWT.BOLD);
			fontRegistry.put("customProperty", new FontData[] { fontData });
		}
		field.setFont(fontRegistry.get("customProperty"));
	}

}
