package it.albertus.cycles.gui.listener;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.gui.FormProperty;
import it.albertus.cycles.gui.TorqueGraph;
import it.albertus.cycles.model.Torque;

public class TorquePropertyFocusListener extends PropertyFocusListener {

	public TorquePropertyFocusListener(final CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void focusLost(final FocusEvent fe) {
		if (isEnabled()) {
			super.focusLost(fe);

			// Update torque graph...
			final Text field = (Text) fe.widget;
			if (gui.isNumeric(field.getText().trim())) {
				try {
					final String key = (String) field.getData(FormProperty.TextDataKey.KEY.toString());
					final int index = (Integer) field.getData(FormProperty.TextDataKey.INDEX.toString());
					final TorqueGraph graph = (TorqueGraph) field.getData(FormProperty.TextDataKey.GRAPH.toString());

					final short value = Torque.parse(key, field.getText().trim(), gui.getNumeralSystem().getRadix());
					graph.getValues()[index] = value;
					graph.refresh();
				}
				catch (final InvalidPropertyException ipe) {/* Ignore */}
			}
		}
	}

}
