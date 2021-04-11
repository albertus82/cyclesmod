package it.albertus.cyclesmod.gui.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.common.engine.InvalidPropertyException;
import it.albertus.cyclesmod.common.model.Power;
import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.FormProperty;
import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import it.albertus.util.logging.LoggerFactory;

public class PowerPropertyFocusListener extends PropertyFocusListener {

	private static final Logger logger = LoggerFactory.getLogger(PowerPropertyFocusListener.class);

	public PowerPropertyFocusListener(final CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void focusLost(final FocusEvent fe) {
		if (isEnabled()) {
			super.focusLost(fe);

			// Update power graph...
			final Text field = (Text) fe.widget;
			if (gui.isNumeric(field.getText().trim())) {
				try {
					final String key = (String) field.getData(FormProperty.TextDataKey.KEY.toString());
					final int index = (Integer) field.getData(FormProperty.TextDataKey.INDEX.toString());
					final IPowerGraph graph = (IPowerGraph) field.getData(FormProperty.TextDataKey.GRAPH.toString());

					final short newValue = Power.parse(key, field.getText().trim(), gui.getNumeralSystem().getRadix());
					final short oldValue = (short) graph.getPowerValue(index);
					if (oldValue != newValue) {
						graph.setPowerValue(index, newValue);
						graph.refresh();
					}
				}
				catch (final InvalidPropertyException ipe) {
					logger.log(Level.INFO, ipe.getMessage(), ipe);
				}
			}
		}
	}

}
