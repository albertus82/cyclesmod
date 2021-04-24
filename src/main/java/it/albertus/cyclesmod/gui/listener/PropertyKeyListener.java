package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.gui.Tabs;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class PropertyKeyListener extends KeyAdapter {

	@NonNull private final Tabs tabs;

	@Getter @Setter private boolean enabled = true;

	public PropertyKeyListener(final Tabs tabs) {
		this.tabs = tabs;
	}

	@Override
	public void keyReleased(final KeyEvent ke) {
		if (enabled) {
			tabs.getTextFormatter().updateFontStyle((Text) ke.widget);
		}
	}

}
