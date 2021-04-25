package it.albertus.cyclesmod.gui.listener;

import java.util.logging.Level;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.common.engine.InvalidNumberException;
import it.albertus.cyclesmod.common.engine.ValueOutOfRangeException;
import it.albertus.cyclesmod.common.model.Power;
import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.FormProperty;
import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class PowerPropertyFocusListener extends PropertyFocusListener {

	public PowerPropertyFocusListener(@NonNull final CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void focusLost(@NonNull final FocusEvent fe) {
		if (isEnabled()) {
			super.focusLost(fe);

			// Update power graph...
			final Text field = (Text) fe.widget;
			if (gui.getEngine().isNumeric(field.getText().trim())) {
				try {
					final int index = (Integer) field.getData(FormProperty.TextDataKey.INDEX.toString());
					final IPowerGraph graph = (IPowerGraph) field.getData(FormProperty.TextDataKey.GRAPH.toString());

					final short newValue = Power.parse(field.getData(FormProperty.TextDataKey.KEY.toString()).toString(), field.getText().trim(), gui.getNumeralSystem().getRadix());
					final short oldValue = (short) graph.getPowerValue(index);
					if (oldValue != newValue) {
						graph.setPowerValue(index, newValue);
						graph.refresh();
					}
				}
				catch (final InvalidNumberException | ValueOutOfRangeException e) {
					log.log(Level.INFO, e.getMessage(), e);
				}
			}
		}
	}

}
