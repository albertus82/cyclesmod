package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

public class PropertyFocusListener implements FocusListener {

	protected final CyclesModGui gui;

	private boolean enabled = true;

	public PropertyFocusListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void focusLost(final FocusEvent fe) {
		if (enabled) {
			final Text text = (Text) fe.widget;
			gui.getTextFormatter().clean(text);
			gui.getTextFormatter().updateFontStyle(text);
		}
	}

	@Override
	public void focusGained(final FocusEvent fe) {
		if (enabled) {
			final Text text = (Text) fe.widget;
			text.selectAll();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
