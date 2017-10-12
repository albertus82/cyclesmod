package it.albertus.cyclesmod.gui.torquegraph;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import it.albertus.cyclesmod.model.Bike;
import it.albertus.cyclesmod.model.Torque;
import it.albertus.cyclesmod.model.Bike.BikeType;
import it.albertus.cyclesmod.resources.Messages;

public class TorqueGraph implements ITorqueGraph {

	public static final int RPM_DIVISOR = 1000;

	private final IXYGraph xyGraph = new XYGraph();
	private final Axis abscissae = xyGraph.getPrimaryXAxis();
	private final Axis ordinates = xyGraph.getPrimaryYAxis();
	private final CircularBufferDataProvider dataProvider = new CircularBufferDataProvider(false);
	private final Trace trace = new Trace(Messages.get("lbl.graph.trace"), abscissae, ordinates, dataProvider);
	private final double[] values = new double[Torque.LENGTH];
	private final double[] xDataArray = new double[Torque.LENGTH];

	public TorqueGraph(final Bike bike) {
		for (int i = 0; i < Torque.LENGTH; i++) {
			xDataArray[i] = (double) Torque.getRpm(i) / RPM_DIVISOR;
			values[i] = bike.getTorque().getCurve()[i];
		}
		init(bike.getType());
	}

	public TorqueGraph(final Map<Integer, Short> map, final BikeType bikeType) {
		if (map.size() != Torque.LENGTH) {
			throw new IllegalArgumentException("map size must be " + Torque.LENGTH);
		}

		int i = 0;
		for (final Entry<Integer, Short> entry : map.entrySet()) {
			xDataArray[i] = entry.getKey().doubleValue() / RPM_DIVISOR;
			values[i] = entry.getValue();
			i++;
		}
		init(bikeType);
	}

	protected void init(final BikeType bikeType) {
		dataProvider.setBufferSize(xDataArray.length);
		dataProvider.setCurrentXDataArray(xDataArray);
		dataProvider.setCurrentYDataArray(values);

		final Font axisTitleFont = Display.getCurrent().getSystemFont();

		abscissae.setTitle(Messages.get("lbl.graph.axis.x", RPM_DIVISOR));
		abscissae.setTitleFont(axisTitleFont);
		abscissae.setShowMajorGrid(true);

		ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
		ordinates.setTitleFont(axisTitleFont);
		ordinates.setShowMajorGrid(true);

		xyGraph.addTrace(trace);
		xyGraph.setShowLegend(false);

		trace.setTraceColor(getColor(bikeType));
	}

	private static Color getColor(final BikeType bikeType) {
		final Display display = Display.getCurrent();
		switch (bikeType) {
		case CLASS_125:
			return display.getSystemColor(SWT.COLOR_RED);
		case CLASS_250:
			return display.getSystemColor(SWT.COLOR_BLUE);
		case CLASS_500:
			return display.getSystemColor(SWT.COLOR_BLACK);
		default:
			throw new IllegalStateException("Unknown bike type: " + bikeType);
		}
	}

	@Override
	public void refresh() {
		dataProvider.triggerUpdate();
	}

	@Override
	public IXYGraph getXyGraph() {
		return xyGraph;
	}

	@Override
	public Axis getAbscissae() {
		return abscissae;
	}

	@Override
	public Axis getOrdinates() {
		return ordinates;
	}

	@Override
	public CircularBufferDataProvider getDataProvider() {
		return dataProvider;
	}

	@Override
	public Trace getTrace() {
		return trace;
	}

	@Override
	public double[] getValues() {
		return values;
	}

	@Override
	public short getTorqueValue(final Point location) {
		return (short) Math.round(Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, getOrdinates().getPositionValue(location.y, false))));
	}

	@Override
	public int getTorqueIndex(final Point location) {
		return Math.max(Math.min(Torque.indexOf(getAbscissae().getPositionValue(location.x, false) * RPM_DIVISOR), Torque.LENGTH - 1), 0);
	}

}
