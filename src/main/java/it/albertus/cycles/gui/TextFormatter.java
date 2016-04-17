package it.albertus.cycles.gui;

import it.albertus.cycles.gui.FormProperty.TextDataKey;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Text;

public class TextFormatter {

	private static final char SAMPLE_CHAR = '9';

	private final CyclesModGui gui;
	private final FontRegistry fontRegistry;

	TextFormatter(final CyclesModGui gui) {
		this.gui = gui;
		this.fontRegistry = JFaceResources.getFontRegistry();
	}

	public void clean(final Text text) {
		if (text != null) {
			final String textValue = text.getText().trim();
			if (gui.isNumeric(textValue)) {
				final int actualValue = Integer.parseInt(textValue, gui.getNumeralSystem().getRadix());
				final Integer maxValue = (Integer) text.getData(TextDataKey.MAX.toString());
				if (maxValue != null && actualValue > maxValue.intValue()) {
					text.setText(Integer.toString(maxValue, gui.getNumeralSystem().getRadix()));
				}
				else {
					text.setText(Integer.toString(Integer.parseInt(textValue, gui.getNumeralSystem().getRadix()), gui.getNumeralSystem().getRadix()));
				}
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

	/** Consente la determinazione automatica della larghezza del campo. */
	public void setSampleNumber(final Text text) {
		if (text != null && text.getData(TextDataKey.SIZE.toString()) instanceof Integer) {
			final int size = (Integer) text.getData(TextDataKey.SIZE.toString());
			final char[] sample = new char[size];
			for (int i = 0; i < size; i++) {
				sample[i] = SAMPLE_CHAR;
			}
			if (text.getTextLimit() < size) {
				text.setTextLimit(size);
			}
			text.setText(String.valueOf(sample));
			setBoldFontStyle(text);
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
