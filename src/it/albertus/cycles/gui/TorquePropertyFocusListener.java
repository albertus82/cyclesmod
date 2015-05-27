package it.albertus.cycles.gui;

import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.model.Torque;
import it.albertus.util.ExceptionUtils;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TorquePropertyFocusListener extends PropertyFocusListener {

	private static final Logger log = LoggerFactory.getLogger(TorquePropertyFocusListener.class);

	private final String key;
	private final TorqueGraph graph;
	private final short index;

	public TorquePropertyFocusListener(String defaultValue, String key, TorqueGraph graph) {
		super(defaultValue);
		this.key = key;
		this.graph = graph;
		this.index = Short.parseShort(StringUtils.substringAfterLast(key, "."));
	}

	@Override
	public void focusLost(FocusEvent event) {
		super.focusLost(event);

		// Update torque graph...
		Text field = (Text) event.widget;
		try {
			short value = Torque.parse(key, field.getText());
			graph.getValues()[index] = value;
			graph.refresh();
		}
		catch (InvalidPropertyException ipe) {
			log.debug(ExceptionUtils.getLogMessage(ipe));
		}
	}

}
