package it.albertus.cyclesmod.gui.powergraph.simple;

import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.swt.widgets.Display;

import it.albertus.cyclesmod.gui.powergraph.PowerGraph;
import it.albertus.cyclesmod.model.Bike;
import it.albertus.cyclesmod.resources.Messages;

public class SimplePowerGraph extends PowerGraph {

	private static final byte DEFAULT_POINT_SIZE = 4;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	public SimplePowerGraph(final Bike bike) {
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
		xyGraph.setTitle(Messages.get("lbl.graph.title"));
		xyGraph.setTitleFont(Display.getCurrent().getSystemFont());
	}

	public void updateTexts() {
		getXyGraph().setTitle(Messages.get("lbl.graph.title"));
		getAbscissae().setTitle(Messages.get("lbl.graph.axis.x", RPM_DIVISOR));
		if (isTorqueVisible()) {
			getOrdinates().setTitle(Messages.get("lbl.graph.axis.y.power") + " / " + Messages.get("lbl.graph.axis.y.torque"));
		}
		else {
			getOrdinates().setTitle(Messages.get("lbl.graph.axis.y.power"));
		}
	}

}
