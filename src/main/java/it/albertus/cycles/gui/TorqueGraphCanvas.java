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
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
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

	private final double[] values = new double[Torque.LENGTH];

	private XYGraph xyGraph;
	private CircularBufferDataProvider dataProvider;
	private Axis abscissae;
	private Axis ordinates;
	private Trace trace;

	public static Color getColor(final Bike bike) {
		switch (bike.getType()) {
		case CLASS_125:
			return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		case CLASS_250:
			return Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
		case CLASS_500:
			return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		default:
			throw new IllegalStateException("Unknown bike type: " + bike.getType());
		}
	}

	public static Map<Double, Double> getValueMap(final Bike bike) {
		final Map<Double, Double> map = new TreeMap<Double, Double>();
		for (byte i = 0; i < bike.getTorque().getCurve().length; i++) {
			map.put(((double) Torque.getRpm(i)) / 1000, (double) bike.getTorque().getCurve()[i]);
		}
		return map;
	}

	public static Map<Double, Double> getValueMap(Map<String, FormProperty> formProperties, BikeType bikeType) {
		final Map<Double, Double> map = new TreeMap<Double, Double>();
		for (byte i = 0; i < Torque.LENGTH; i++) {
			final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(bikeType, Torque.class, i));
			map.put((double) Torque.getRpm(i) / 1000, Double.valueOf(formProperty.getValue()));
		}
		return map;
	}

	public TorqueGraphCanvas(final Composite parent, final Bike bike) {
		this(parent, getValueMap(bike), getColor(bike));
	}

	public TorqueGraphCanvas(final Composite parent, final Map<Double, Double> values, final Color traceColor) {
		super(parent, SWT.NULL);
		if (values.size() != Torque.LENGTH) {
			throw new IllegalArgumentException("values size must be " + Torque.LENGTH);
		}

		final LightweightSystem lws = new LightweightSystem(this);

		xyGraph = new XYGraph();
		xyGraph.setTitle(Messages.get("lbl.graph.title"));
		lws.setContents(xyGraph);

		final double[] x = new double[Torque.LENGTH];
		byte i = 0;
		for (final Entry<Double, Double> entry : values.entrySet()) {
			x[i] = entry.getKey();
			this.values[i] = entry.getValue();
			i++;
		}

		dataProvider = new CircularBufferDataProvider(false);
		dataProvider.setBufferSize(x.length);
		dataProvider.setCurrentXDataArray(x);
		dataProvider.setCurrentYDataArray(this.values);

		final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (!fontRegistry.hasValueFor(FONT_KEY_AXIS_TITLE)) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put(FONT_KEY_AXIS_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), sysFont.getFontData()[0].getHeight(), SWT.BOLD) });
		}
		final Font axisTitleFont = fontRegistry.get(FONT_KEY_AXIS_TITLE);

		abscissae = xyGraph.getPrimaryXAxis();
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

		ordinates = xyGraph.getPrimaryYAxis();
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

		trace = new Trace("Torque", abscissae, ordinates, dataProvider);
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
	public IDataProvider getDataProvider() {
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
