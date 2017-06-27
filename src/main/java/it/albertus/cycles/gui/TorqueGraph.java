package it.albertus.cycles.gui;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

class TorqueGraph extends Figure implements ITorqueGraph {

	public static final boolean DEFAULT_AUTOSCALE = false;

	public static final String FONT_KEY_GRAPH_TITLE = "graphTitle";
	public static final String FONT_KEY_AXIS_TITLE = "axisTitle";

	private final IXYGraph xyGraph = new XYGraph();
	private final ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(xyGraph);
	private final Axis abscissae = xyGraph.getPrimaryXAxis();
	private final Axis ordinates = xyGraph.getPrimaryYAxis();
	private final CircularBufferDataProvider dataProvider = new CircularBufferDataProvider(false);
	private final Trace trace = new Trace("Torque", abscissae, ordinates, dataProvider);
	private final double[] values = new double[Torque.LENGTH];

	public TorqueGraph(final Map<Double, Double> valueMap, final Color traceColor, final int lineWidth, final int pointSize) {
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
		ordinates.addMouseListener(new MouseListener.Stub() {
			@Override
			public void mouseDoubleClicked(final MouseEvent me) {
				ordinates.performAutoScale(true);
			}
		});

		trace.setPointStyle(PointStyle.FILLED_DIAMOND);
		trace.setTraceColor(traceColor);
		trace.setLineWidth(lineWidth);
		trace.setPointSize(pointSize);

		xyGraph.addTrace(trace);
		xyGraph.setShowLegend(false);

		add(toolbarArmedXYGraph);

		xyGraph.getPlotArea().addMouseListener(new MouseListener.Stub() {
			@Override
			public void mousePressed(final MouseEvent me) {
				if (me.button == 1) { // left click
					final double rpm = abscissae.getPositionValue(me.getLocation().x, false) * 1000;
					final int index = Math.max(Math.min(Torque.indexOf(rpm), Torque.LENGTH - 1), 0);
					final short oldValue = (short) values[index];
					final short newValue = (short) Math.round(Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, ordinates.getPositionValue(me.getLocation().y, false))));
					if (oldValue != newValue) {
						values[index] = newValue;
						dataProvider.triggerUpdate();
						xyGraph.getOperationsManager().addCommand(new IUndoableCommand() {
							@Override
							public void undo() {
								values[index] = oldValue;
								refresh();
							}

							@Override
							public void redo() {
								values[index] = newValue;
								refresh();
							}
						});
					}
				}
			}
		});

		abscissae.performAutoScale(true);
		ordinates.performAutoScale(true);
	}

	@Override
	protected void layout() {
		toolbarArmedXYGraph.setBounds(getBounds().getCopy());
		super.layout();
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
