package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

public class PropertyFocusListener implements FocusListener {

	protected final CyclesModGui gui;

	public PropertyFocusListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void focusLost(FocusEvent fe) {
		final Text text = (Text) fe.widget;
		gui.getTextFormatter().clean(text);
		gui.getTextFormatter().updateFontStyle(text);
	}

	@Override
	public void focusGained(FocusEvent fe) {
		final Text text = (Text) fe.widget;
		text.selectAll();
	}

}
