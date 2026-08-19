package io.github.albertus82.cyclesmod.gui.listener;

import java.util.logging.Level;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

import io.github.albertus82.cyclesmod.common.engine.InvalidNumberException;
import io.github.albertus82.cyclesmod.common.engine.ValueOutOfRangeException;
import io.github.albertus82.cyclesmod.common.model.Torque;
import io.github.albertus82.cyclesmod.gui.CyclesModGui;
import io.github.albertus82.cyclesmod.gui.model.TorqueTextData;
import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraph;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class TorquePropertyFocusListener extends PropertyFocusListener {

	public TorquePropertyFocusListener(@NonNull final CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void focusLost(@NonNull final FocusEvent event) {
		if (isEnabled() && event.widget instanceof Text) {
			super.focusLost(event);

			// Update power graph...
			final Text text = (Text) event.widget;
			if (gui.isNumeric(text.getText().trim()) && text.getData() instanceof TorqueTextData) {
				final TorqueTextData textData = (TorqueTextData) text.getData();
				try {
					final int index = textData.getIndex();
					final TorqueGraph graph = textData.getPowerGraph();

					final short newValue = Torque.parse(textData.getKeyMap().get(gui.getMode()), text.getText().trim(), gui.getNumeralSystem().getRadix());
					final short oldValue = (short) graph.getTorqueValue(index);
					if (oldValue != newValue) {
						graph.setTorqueValue(index, newValue);
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
