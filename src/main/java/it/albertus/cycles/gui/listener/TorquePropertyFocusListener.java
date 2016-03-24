package it.albertus.cycles.gui.listener;

import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.gui.FormProperty;
import it.albertus.cycles.gui.TorqueGraph;
import it.albertus.cycles.model.Torque;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

public class TorquePropertyFocusListener extends PropertyFocusListener {

	public TorquePropertyFocusListener(final CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void focusLost(FocusEvent fe) {
		super.focusLost(fe);

		// Update torque graph...
		Text field = (Text) fe.widget;
		try {
			final String key = (String) field.getData(FormProperty.KEY_KEY);
			final int index = (Integer) field.getData(FormProperty.KEY_INDEX);
			final TorqueGraph graph = (TorqueGraph) field.getData(FormProperty.KEY_GRAPH);

			short value = Torque.parse(key, field.getText(), gui.getRadix());
			graph.getValues()[index] = value;
			graph.refresh();
		}
		catch (ClassCastException ce) {}
		catch (InvalidPropertyException ipe) {}
	}

}
