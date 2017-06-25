package it.albertus.cycles.gui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import it.albertus.cycles.engine.NumeralSystemProvider;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

public class TorqueGraphCanvas extends Canvas implements TorqueGraph {

	public static final boolean DEFAULT_AUTOSCALE = true;

	public static final String FONT_KEY_GRAPH_TITLE = "graphTitle";
	public static final String FONT_KEY_AXIS_TITLE = "axisTitle";

	public static final float TITLE_FONT_HEIGHT_FACTOR = 1.25f;

	private final XYGraph xyGraph = new XYGraph();
	private final Axis abscissae = xyGraph.getPrimaryXAxis();
	private final Axis ordinates = xyGraph.getPrimaryYAxis();
	private final CircularBufferDataProvider dataProvider = new CircularBufferDataProvider(false);
	private final Trace trace = new Trace("Torque", abscissae, ordinates, dataProvider);
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

	public TorqueGraphCanvas(final Composite parent, final Bike bike) {
		this(parent, getValueMap(bike), getColor(bike.getType()));
	}

	public TorqueGraphCanvas(final Composite parent, final Map<Double, Double> valueMap, final Color traceColor) {
		super(parent, SWT.NULL);
		if (valueMap.size() != Torque.LENGTH) {
			throw new IllegalArgumentException("values size must be " + Torque.LENGTH);
		}

		final LightweightSystem lws = new LightweightSystem(this);
		lws.setContents(xyGraph);

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

		final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (!fontRegistry.hasValueFor(FONT_KEY_AXIS_TITLE)) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put(FONT_KEY_AXIS_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), sysFont.getFontData()[0].getHeight(), SWT.BOLD) });
		}
		final Font axisTitleFont = fontRegistry.get(FONT_KEY_AXIS_TITLE);

		abscissae.setTitle(Messages.get("lbl.graph.axis.x"));
		abscissae.setAutoScale(DEFAULT_AUTOSCALE);
		abscissae.setTitleFont(axisTitleFont);
		abscissae.setShowMajorGrid(true);
		abscissae.setZoomType(ZoomType.DYNAMIC_ZOOM);
		abscissae.addMouseListener(new MouseListener.Stub() {
			@Override
			public void mouseDoubleClicked(final MouseEvent me) {
				abscissae.performAutoScale(true);
			}
		});

		ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
		ordinates.setAutoScale(DEFAULT_AUTOSCALE);
		ordinates.setTitleFont(axisTitleFont);
		ordinates.setShowMajorGrid(true);
		ordinates.setZoomType(ZoomType.DYNAMIC_ZOOM);
		ordinates.addMouseListener(new MouseListener.Stub() {
			@Override
			public void mouseDoubleClicked(final MouseEvent me) {
				ordinates.performAutoScale(true);
			}
		});

		trace.setPointStyle(PointStyle.FILLED_DIAMOND);
		trace.setTraceColor(traceColor);

		xyGraph.addTrace(trace);
		xyGraph.setShowLegend(false);

		if (!fontRegistry.hasValueFor(FONT_KEY_GRAPH_TITLE)) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put(FONT_KEY_GRAPH_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), (int) (sysFont.getFontData()[0].getHeight() * TITLE_FONT_HEIGHT_FACTOR), SWT.BOLD) });
		}
		xyGraph.setTitleFont(fontRegistry.get(FONT_KEY_GRAPH_TITLE));
	}

	@Override
	public void refresh() {
		dataProvider.triggerUpdate();
	}

	@Override
	public XYGraph getXyGraph() {
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
