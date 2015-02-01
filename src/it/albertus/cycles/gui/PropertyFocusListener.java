package it.albertus.cycles.gui;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
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
		updateFontStyle(field, defaultValue);
	}

	public static void updateFontStyle(Text field, String defaultValue) {
		if (field != null && field.getFont() != null && ArrayUtils.isNotEmpty(field.getFont().getFontData()) && defaultValue != null) {
			final FontData fontData = field.getFont().getFontData()[0];
			if (!defaultValue.equalsIgnoreCase(field.getText())) {
				if (fontData.getStyle() != SWT.BOLD) {
					fontData.setStyle(SWT.BOLD);
					final Font newFont = new Font(Display.getCurrent(), fontData);
					field.setFont(newFont);
					field.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							newFont.dispose();
						}
					});
				}
			}
			else {
				if (fontData.getStyle() != SWT.NORMAL) {
					fontData.setStyle(SWT.NORMAL);
					final Font newFont = new Font(Display.getCurrent(), fontData);
					field.setFont(newFont);
					field.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							newFont.dispose();
						}
					});
				}
			}
		}
	}

}