package it.albertus.cycles.gui;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Resources;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class TorqueGraph extends Canvas {

	private static final float TITLE_FONT_HEIGHT_FACTOR = 1.25f;
	private final Trace trace;
	private final double[] values;

	private final XYGraph xyGraph;
	private final Axis abscissae;
	private final Axis ordinates;

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

	public TorqueGraph(final Composite parent, final Bike bike) {
		super(parent, SWT.NULL);

		final LightweightSystem lws = new LightweightSystem(this);

		xyGraph = new XYGraph();
		xyGraph.setTitle(Resources.get("lbl.graph.title"));
		lws.setContents(xyGraph);

		final double[] x = new double[Torque.LENGTH], y = new double[Torque.LENGTH];
		for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
			x[i] = ((double) Torque.getRpm(i)) / 1000;
			y[i] = bike.getTorque().getCurve()[i];
		}

		final CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
		traceDataProvider.setBufferSize(x.length);
		traceDataProvider.setCurrentXDataArray(x);
		traceDataProvider.setCurrentYDataArray(y);

		final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (!fontRegistry.hasValueFor("axisTitle")) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put("axisTitle", new FontData[] { new FontData(sysFont.getFontData()[0].getName(), sysFont.getFontData()[0].getHeight(), SWT.BOLD) });
		}
		final Font axisTitleFont = fontRegistry.get("axisTitle");

		abscissae = xyGraph.primaryXAxis;
		abscissae.setTitle(Resources.get("lbl.graph.axis.x"));
		abscissae.setAutoScale(true);
		abscissae.setTitleFont(axisTitleFont);
		abscissae.setShowMajorGrid(true);

		ordinates = xyGraph.primaryYAxis;
		ordinates.setTitle(Resources.get("lbl.graph.axis.y"));
		ordinates.setAutoScale(true);
		ordinates.setTitleFont(axisTitleFont);
		ordinates.setShowMajorGrid(true);

		final Trace trace = new Trace("Torque", abscissae, ordinates, traceDataProvider);
		trace.setPointStyle(PointStyle.NONE);
		trace.setLineWidth(3);
		final Color traceColor;
		switch (bike.getType()) {
		case CLASS_125:
			traceColor = new Color(Display.getCurrent(), 0xFF, 0, 0);
			break;
		case CLASS_250:
			traceColor = new Color(Display.getCurrent(), 0, 0, 0xFF);
			break;
		case CLASS_500:
			traceColor = new Color(Display.getCurrent(), 0x1F, 0x1F, 0x1F);
			break;
		default:
			traceColor = trace.getTraceColor();
		}
		this.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				traceColor.dispose();
			}
		});

		trace.setTraceColor(traceColor);

		xyGraph.addTrace(trace);
		xyGraph.setShowLegend(false);

		if (!fontRegistry.hasValueFor("graphTitle")) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put("graphTitle", new FontData[] { new FontData(sysFont.getFontData()[0].getName(), (int) (sysFont.getFontData()[0].getHeight() * TITLE_FONT_HEIGHT_FACTOR), SWT.BOLD) });
		}
		xyGraph.setTitleFont(fontRegistry.get("graphTitle"));

		this.trace = trace;
		this.values = y;
	}

	public void updateTexts() {
		xyGraph.setTitle(Resources.get("lbl.graph.title"));
		abscissae.setTitle(Resources.get("lbl.graph.axis.x"));
		ordinates.setTitle(Resources.get("lbl.graph.axis.y"));
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
