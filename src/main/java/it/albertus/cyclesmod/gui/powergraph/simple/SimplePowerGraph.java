package it.albertus.cyclesmod.gui.powergraph.simple;

import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.swt.widgets.Display;

import it.albertus.cyclesmod.common.model.Vehicle;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.powergraph.PowerGraph;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.jface.Multilanguage;

public class SimplePowerGraph extends PowerGraph implements Multilanguage {

	private static final byte DEFAULT_POINT_SIZE = 4;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	private static final Messages messages = GuiMessages.INSTANCE;

	public SimplePowerGraph(final Vehicle bike) {
		super(bike);

		final Axis abscissae = getAbscissae();
		abscissae.setAutoScale(DEFAULT_AUTOSCALE);

		final Axis ordinates = getOrdinates();
		ordinates.setAutoScale(DEFAULT_AUTOSCALE);

		final Trace powerTrace = getPowerTrace();
		powerTrace.setPointStyle(PointStyle.FILLED_DIAMOND);
		powerTrace.setLineWidth(DEFAULT_LINE_WIDTH);
		powerTrace.setPointSize(DEFAULT_POINT_SIZE);

		final Trace torqueTrace = getTorqueTrace();
		torqueTrace.setLineWidth(DEFAULT_LINE_WIDTH);

		final IXYGraph xyGraph = getXyGraph();
		xyGraph.setTitle(messages.get("gui.label.graph.title.power.torque"));
		xyGraph.setTitleFont(Display.getCurrent().getSystemFont());
	}

	@Override
	public void toggleTorqueVisibility(final boolean visibility) {
		super.toggleTorqueVisibility(visibility);
		if (visibility) {
			getXyGraph().setTitle(messages.get("gui.label.graph.title.power.torque"));
		}
		else {
			getXyGraph().setTitle(messages.get("gui.label.graph.title.power"));
		}
	}

	@Override
	public void updateLanguage() {
		getAbscissae().setTitle(messages.get("gui.label.graph.axis.x", RPM_DIVISOR));
		if (isTorqueVisible()) {
			getXyGraph().setTitle(messages.get("gui.label.graph.title.power.torque"));
			getOrdinates().setTitle(messages.get("gui.label.graph.axis.y.power") + " / " + messages.get("gui.label.graph.axis.y.torque"));
		}
		else {
			getXyGraph().setTitle(messages.get("gui.label.graph.title.power"));
			getOrdinates().setTitle(messages.get("gui.label.graph.axis.y.power"));
		}
	}

}
