package it.albertus.cycles.gui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.draw2d.Figure;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import it.albertus.cycles.engine.NumeralSystemProvider;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

public class TorqueGraph extends Figure implements ITorqueGraph {

	public static final String FONT_KEY_GRAPH_TITLE = "graphTitle";
	public static final String FONT_KEY_AXIS_TITLE = "axisTitle";

	private final IXYGraph xyGraph = new XYGraph();
	private final Axis abscissae = xyGraph.getPrimaryXAxis();
	private final Axis ordinates = xyGraph.getPrimaryYAxis();
	private final CircularBufferDataProvider dataProvider = new CircularBufferDataProvider(false);
	private final Trace trace = new Trace(Messages.get("lbl.graph.title"), abscissae, ordinates, dataProvider);
	private final double[] values = new double[Torque.LENGTH];

	public static Color getColor(final BikeType bikeType) {
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

	public static Map<Double, Double> getValueMap(final Bike bike) {
		final Map<Double, Double> map = new TreeMap<Double, Double>();
		for (byte i = 0; i < bike.getTorque().getCurve().length; i++) {
			map.put(((double) Torque.getRpm(i)) / 1000, (double) bike.getTorque().getCurve()[i]);
		}
		return map;
	}

	public static Map<Double, Double> getValueMap(Map<String, FormProperty> formProperties, BikeType bikeType, NumeralSystemProvider nsp) {
		final Map<Double, Double> map = new TreeMap<Double, Double>();
		for (byte i = 0; i < Torque.LENGTH; i++) {
			final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(bikeType, Torque.class, i));
			map.put((double) Torque.getRpm(i) / 1000, Short.valueOf(formProperty.getValue(), nsp.getNumeralSystem().getRadix()).doubleValue());
		}
		return map;
	}

	public TorqueGraph(final Map<Double, Double> valueMap) {
		if (valueMap.size() != Torque.LENGTH) {
			throw new IllegalArgumentException("values size must be " + Torque.LENGTH);
		}

		final double[] x = new double[Torque.LENGTH];
		byte i = 0;
		for (final Entry<Double, Double> entry : valueMap.entrySet()) {
			x[i] = entry.getKey();
			values[i] = entry.getValue();
			i++;
		}

		dataProvider.setBufferSize(x.length);
		dataProvider.setCurrentXDataArray(x);
		dataProvider.setCurrentYDataArray(values);

		abscissae.setTitle(Messages.get("lbl.graph.axis.x"));
		abscissae.setShowMajorGrid(true);

		ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
		ordinates.setShowMajorGrid(true);

		xyGraph.addTrace(trace);
		xyGraph.setShowLegend(false);
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

}
