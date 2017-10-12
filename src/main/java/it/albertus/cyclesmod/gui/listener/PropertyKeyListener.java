package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.gui.CyclesModGui;

public class PropertyKeyListener extends KeyAdapter {

	protected final CyclesModGui gui;

	private boolean enabled = true;

	public PropertyKeyListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void keyReleased(final KeyEvent ke) {
		if (enabled) {
			gui.getTabs().getTextFormatter().updateFontStyle((Text) ke.widget);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

}
