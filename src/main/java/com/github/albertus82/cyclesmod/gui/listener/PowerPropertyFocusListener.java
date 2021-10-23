package com.github.albertus82.cyclesmod.gui.listener;

import java.util.logging.Level;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

import com.github.albertus82.cyclesmod.common.engine.InvalidNumberException;
import com.github.albertus82.cyclesmod.common.engine.ValueOutOfRangeException;
import com.github.albertus82.cyclesmod.common.model.Power;
import com.github.albertus82.cyclesmod.gui.CyclesModGui;
import com.github.albertus82.cyclesmod.gui.model.PowerTextData;
import com.github.albertus82.cyclesmod.gui.powergraph.IPowerGraph;

import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class PowerPropertyFocusListener extends PropertyFocusListener {

	public PowerPropertyFocusListener(@NonNull final CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void focusLost(@NonNull final FocusEvent event) {
		if (isEnabled() && event.widget instanceof Text) {
			super.focusLost(event);

			// Update power graph...
			final Text text = (Text) event.widget;
			if (gui.isNumeric(text.getText().trim()) && text.getData() instanceof PowerTextData) {
				final PowerTextData textData = (PowerTextData) text.getData();
				try {
					final int index = textData.getIndex();
					final IPowerGraph graph = textData.getPowerGraph();

					final short newValue = Power.parse(textData.getKeyMap().get(gui.getMode()), text.getText().trim(), gui.getNumeralSystem().getRadix());
					final short oldValue = (short) graph.getPowerValue(index);
					if (oldValue != newValue) {
						graph.setPowerValue(index, newValue);
						graph.refresh();
					}
				}
				catch (final InvalidNumberException | ValueOutOfRangeException e) {
					log.log(Level.INFO, "Cannot update power graph on " + event + ':', e);
				}
			}
		}
	}

}
