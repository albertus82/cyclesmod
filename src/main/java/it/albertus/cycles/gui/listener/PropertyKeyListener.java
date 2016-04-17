package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;

public class PropertyKeyListener extends KeyAdapter {

	protected final CyclesModGui gui;

	private boolean enabled = true;

	public PropertyKeyListener(CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (enabled) {
			gui.getTabs().getTextFormatter().updateFontStyle((Text) e.widget);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
