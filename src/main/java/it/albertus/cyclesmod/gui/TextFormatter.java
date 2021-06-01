package it.albertus.cyclesmod.gui;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.gui.model.GenericTextData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TextFormatter {

	private static final String FONT_KEY = "it.albertus.jface.font.property";

	private static final char SAMPLE_CHAR = '9';

	private final CyclesModGui gui;
	private final FontRegistry fontRegistry = JFaceResources.getFontRegistry();

	public void clean(final Text text) {
		if (text != null) {
			String textValue = text.getText().trim();
			if (gui.isNumeric(textValue) && text.getData() instanceof GenericTextData) {
				final int actualValue = Integer.parseInt(textValue, gui.getNumeralSystem().getRadix());
				final int maxValue = ((GenericTextData) text.getData()).getMaxValue();
				if (actualValue > maxValue) {
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
		if (text != null && gui.isNumeric(text.getText()) && text.getFont() != null && text.getFont().getFontData() != null && text.getFont().getFontData().length > 0 && text.getData() instanceof GenericTextData) {
			final int defaultValue = ((GenericTextData) text.getData()).getDefaultValueMap().get(gui.getMode());
			if (defaultValue != Integer.parseInt(text.getText(), gui.getNumeralSystem().getRadix())) {
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
		if (text != null && text.getData() instanceof GenericTextData) {
			final int size = ((GenericTextData) text.getData()).getSize();
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
