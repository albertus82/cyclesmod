package it.albertus.cyclesmod.gui;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.gui.FormProperty.TextDataKey;

public class TextFormatter {

	private static final String FONT_KEY = "it.albertus.jface.font.property";

	private static final char SAMPLE_CHAR = '9';

	private final CyclesModGui gui;
	private final FontRegistry fontRegistry;

	TextFormatter(final CyclesModGui gui) {
		this.gui = gui;
		this.fontRegistry = JFaceResources.getFontRegistry();
	}

	public void clean(final Text text) {
		if (text != null) {
			String textValue = text.getText().trim();
			if (gui.isNumeric(textValue)) {
				final int actualValue = Integer.parseInt(textValue, gui.getNumeralSystem().getRadix());
				final Integer maxValue = (Integer) text.getData(TextDataKey.MAX.toString());
				if (maxValue != null && actualValue > maxValue.intValue()) {
					textValue = Integer.toString(maxValue, gui.getNumeralSystem().getRadix());
				}
				else {
					textValue = Integer.toString(Integer.parseInt(textValue, gui.getNumeralSystem().getRadix()), gui.getNumeralSystem().getRadix());
				}
			}
			else {
				textValue = "0";
			}
			if (!text.getText().equals(textValue)) {
				text.setText(textValue);
			}
		}
	}

	public void updateFontStyle(final Text text) {
		if (text != null && gui.isNumeric(text.getText()) && text.getFont() != null && text.getFont().getFontData() != null && text.getFont().getFontData().length > 0 && text.getData(FormProperty.TextDataKey.DEFAULT.toString()) instanceof Integer) {
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

	public void setNormalFontStyle(final Control control) {
		if (!fontRegistry.hasValueFor(FONT_KEY)) {
			fontRegistry.put(FONT_KEY, control.getFont().getFontData());
		}
		control.setFont(fontRegistry.get(FONT_KEY));
	}

	public void setBoldFontStyle(final Control control) {
		if (!fontRegistry.hasValueFor(FONT_KEY)) {
			fontRegistry.put(FONT_KEY, control.getFont().getFontData());
		}
		control.setFont(fontRegistry.getBold(FONT_KEY));
	}

}
