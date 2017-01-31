package it.albertus.cycles.gui;

import java.util.Map;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

public class TorqueGraph extends Canvas {

	private static final String FONT_KEY_GRAPH_TITLE = "graphTitle";
	private static final String FONT_KEY_AXIS_TITLE = "axisTitle";

	private static final float TITLE_FONT_HEIGHT_FACTOR = 1.25f;

	private final Trace trace;
	private final double[] values;

	private final XYGraph xyGraph;
	private final Axis abscissae;
	private final Axis ordinates;

	TorqueGraph(final Composite parent, final Bike bike, final Map<String, FormProperty> formProperties) {
		super(parent, SWT.NULL);

		final LightweightSystem lws = new LightweightSystem(this);

		xyGraph = new XYGraph();
		xyGraph.setTitle(Messages.get("lbl.graph.title"));
		lws.setContents(xyGraph);

		final double[] x = new double[Torque.LENGTH];
		final double[] y = new double[Torque.LENGTH];
		for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
			x[i] = ((double) Torque.getRpm(i)) / 1000;
			y[i] = bike.getTorque().getCurve()[i];
		}

		final CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
		traceDataProvider.setBufferSize(x.length);
		traceDataProvider.setCurrentXDataArray(x);
		traceDataProvider.setCurrentYDataArray(y);

		final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (!fontRegistry.hasValueFor(FONT_KEY_AXIS_TITLE)) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put(FONT_KEY_AXIS_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), sysFont.getFontData()[0].getHeight(), SWT.BOLD) });
		}
		final Font axisTitleFont = fontRegistry.get(FONT_KEY_AXIS_TITLE);

		abscissae = xyGraph.getPrimaryXAxis();
		abscissae.setTitle(Messages.get("lbl.graph.axis.x"));
		abscissae.setAutoScale(true);
		abscissae.setTitleFont(axisTitleFont);
		abscissae.setShowMajorGrid(true);

		ordinates = xyGraph.getPrimaryYAxis();
		ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
		ordinates.setAutoScale(true);
		ordinates.setTitleFont(axisTitleFont);
		ordinates.setShowMajorGrid(true);

		final Trace trc = new Trace("Torque", abscissae, ordinates, traceDataProvider);
		trc.setPointStyle(PointStyle.NONE);
		trc.setLineWidth(3);
		final Color traceColor;
		switch (bike.getType()) {
		case CLASS_125:
			traceColor = getDisplay().getSystemColor(SWT.COLOR_RED);
			break;
		case CLASS_250:
			traceColor = getDisplay().getSystemColor(SWT.COLOR_BLUE);
			break;
		case CLASS_500:
			traceColor = getDisplay().getSystemColor(SWT.COLOR_BLACK);
			break;
		default:
			traceColor = trc.getTraceColor();
		}

		trc.setTraceColor(traceColor);

		xyGraph.addTrace(trc);
		xyGraph.setShowLegend(false);

		if (!fontRegistry.hasValueFor(FONT_KEY_GRAPH_TITLE)) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put(FONT_KEY_GRAPH_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), (int) (sysFont.getFontData()[0].getHeight() * TITLE_FONT_HEIGHT_FACTOR), SWT.BOLD) });
		}
		xyGraph.setTitleFont(fontRegistry.get(FONT_KEY_GRAPH_TITLE));

		xyGraph.getPlotArea().addMouseListener(new MouseListener() {
			@Override
			public void mousePressed(final MouseEvent me) {
				final double rpm = xyGraph.getPrimaryXAxis().getPositionValue(me.getLocation().x, false) * 1000;
				formProperties.get(BikesCfg.buildPropertyKey(bike.getType(), Torque.class, Torque.indexOf(rpm))).getText().setFocus();
			}

			@Override
			public void mouseDoubleClicked(final MouseEvent me) {/* Ignore */}

			@Override
			public void mouseReleased(final MouseEvent me) {/* Ignore */}
		});

		this.trace = trc;
		this.values = y;
	}

	public Trace getTrace() {
		return trace;
	}

	public double[] getValues() {
		return values;
	}

	public boolean refresh() {
		boolean success = false;
		final IDataProvider dataProvider = this.trace.getDataProvider();
		if (dataProvider instanceof CircularBufferDataProvider) {
			((CircularBufferDataProvider) dataProvider).triggerUpdate();
			success = true;
		}
		return success;
	}

	public void updateTexts() {
		xyGraph.setTitle(Messages.get("lbl.graph.title"));
		abscissae.setTitle(Messages.get("lbl.graph.axis.x"));
		ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
	}

	public XYGraph getXyGraph() {
		return xyGraph;
	}

	public Axis getAbscissae() {
		return abscissae;
	}

	public Axis getOrdinates() {
		return ordinates;
	}

}
