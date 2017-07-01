package it.albertus.cycles.gui.torquegraph;

import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.swt.widgets.Display;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.resources.Messages;

public class SimpleTorqueGraph extends TorqueGraph {

	private static final byte DEFAULT_POINT_SIZE = 4;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	public SimpleTorqueGraph(final Bike bike) {
		super(bike);

		final Axis abscissae = getAbscissae();
		abscissae.setAutoScale(DEFAULT_AUTOSCALE);

		final Axis ordinates = getOrdinates();
		ordinates.setAutoScale(DEFAULT_AUTOSCALE);

		final Trace trace = getTrace();
		trace.setPointStyle(PointStyle.FILLED_DIAMOND);

		final IXYGraph xyGraph = getXyGraph();
		xyGraph.setTitle(Messages.get("lbl.graph.title"));
		xyGraph.setTitleFont(Display.getCurrent().getSystemFont());

		trace.setLineWidth(DEFAULT_LINE_WIDTH);
		trace.setPointSize(DEFAULT_POINT_SIZE);
	}

	public void updateTexts() {
		getXyGraph().setTitle(Messages.get("lbl.graph.title"));
		getAbscissae().setTitle(Messages.get("lbl.graph.axis.x", RPM_DIVISOR));
		getOrdinates().setTitle(Messages.get("lbl.graph.axis.y"));
	}

}
