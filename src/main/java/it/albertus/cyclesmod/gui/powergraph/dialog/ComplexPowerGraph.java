package it.albertus.cyclesmod.gui.powergraph.dialog;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.draw2d.Label;
import org.eclipse.nebula.visualization.internal.xygraph.toolbar.GrayableButton;
import org.eclipse.nebula.visualization.internal.xygraph.undo.IOperationsManagerListener;
import org.eclipse.nebula.visualization.internal.xygraph.undo.OperationsManager;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cyclesmod.gui.powergraph.PowerGraph;
import it.albertus.cyclesmod.gui.powergraph.dialog.listener.ChangeValueListener;
import it.albertus.cyclesmod.gui.powergraph.dialog.listener.UpdateTitleListener;
import it.albertus.cyclesmod.model.Bike.BikeType;
import it.albertus.cyclesmod.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class ComplexPowerGraph extends PowerGraph {

	private static final Logger logger = LoggerFactory.getLogger(ComplexPowerGraph.class);

	private static final String LBL_GRAPH_TOOLBAR_UNDO = "lbl.graph.toolbar.undo";
	private static final String LBL_GRAPH_TOOLBAR_REDO = "lbl.graph.toolbar.redo";

	private static final byte DEFAULT_POINT_SIZE = 6;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = false;

	private static final float TITLE_HEIGHT_FACTOR = 0.80f;

	private final Shell shell;
	private final BikeType bikeType;

	private final ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(getXyGraph());

	public ComplexPowerGraph(final Map<Integer, Short> map, final BikeType bikeType, final Shell shell) {
		super(map, bikeType);
		this.shell = shell;
		this.bikeType = bikeType;

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

		fixToolbarButtons(toolbarArmedXYGraph);

		final IXYGraph xyGraph = getXyGraph();
		final PlotArea plotArea = xyGraph.getPlotArea();
		final ChangeValueListener changeValueListener = new ChangeValueListener(this);
		plotArea.addMouseListener(changeValueListener);
		plotArea.addMouseMotionListener(changeValueListener);

		// Title
		final FontData titleFontData = xyGraph.getTitleFontData();
		xyGraph.setTitleFont(XYGraphMediaFactory.getInstance().getFont(titleFontData.getName(), Math.round(titleFontData.getHeight() * TITLE_HEIGHT_FACTOR), SWT.NORMAL));
		xyGraph.setTitle(" ");
		plotArea.addMouseMotionListener(new UpdateTitleListener(this));

		abscissae.performAutoScale(true);
		ordinates.performAutoScale(true);
	}

	@SuppressWarnings("unchecked")
	private static void fixToolbarButtons(final ToolbarArmedXYGraph toolbarArmedXYGraph) {
		try {
			final Field listenersField = OperationsManager.class.getDeclaredField("listeners");
			listenersField.setAccessible(true);
			final OperationsManager manager = toolbarArmedXYGraph.getXYGraph().getOperationsManager();
			for (final IOperationsManagerListener listener : (Iterable<IOperationsManagerListener>) listenersField.get(manager)) {
				manager.removeListener(listener);
			}

			for (final Object child : toolbarArmedXYGraph.getToolbar().getChildren()) {
				if (child instanceof GrayableButton) {
					final GrayableButton button = (GrayableButton) child;
					if (button.getToolTip() instanceof Label) {
						final String labelText = ((Label) button.getToolTip()).getText();
						if ("undo".equalsIgnoreCase(labelText)) {
							button.setToolTip(new Label(Messages.get(LBL_GRAPH_TOOLBAR_UNDO, "")));
							addUndoListener(button, manager);
						}
						else if ("redo".equalsIgnoreCase(labelText)) {
							button.setToolTip(new Label(Messages.get(LBL_GRAPH_TOOLBAR_REDO, "")));
							addRedoListener(button, manager);
						}
					}
				}
			}
		}
		catch (final Exception e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
	}

	private static void addUndoListener(final GrayableButton button, final OperationsManager manager) {
		manager.addListener(new IOperationsManagerListener() {
			@Override
			public void operationsHistoryChanged(final OperationsManager manager) {
				final int undoCommandsSize = manager.getUndoCommandsSize();
				if (undoCommandsSize > 0) {
					button.setEnabled(true);
					final String cmdName = manager.getUndoCommands()[undoCommandsSize - 1].toString();
					button.setToolTip(new Label(Messages.get(LBL_GRAPH_TOOLBAR_UNDO, cmdName)));
				}
				else {
					button.setEnabled(false);
					button.setToolTip(new Label(Messages.get(LBL_GRAPH_TOOLBAR_UNDO, "")));
				}
			}
		});
	}

	private static void addRedoListener(final GrayableButton button, final OperationsManager manager) {
		manager.addListener(new IOperationsManagerListener() {
			@Override
			public void operationsHistoryChanged(final OperationsManager manager) {
				final int redoCommandsSize = manager.getRedoCommandsSize();
				if (redoCommandsSize > 0) {
					button.setEnabled(true);
					final String cmdName = manager.getRedoCommands()[redoCommandsSize - 1].toString();
					button.setToolTip(new Label(Messages.get(LBL_GRAPH_TOOLBAR_REDO, cmdName)));
				}
				else {
					button.setEnabled(false);
					button.setToolTip(new Label(Messages.get(LBL_GRAPH_TOOLBAR_REDO, "")));
				}
			}
		});
	}

	public ToolbarArmedXYGraph getToolbarArmedXYGraph() {
		return toolbarArmedXYGraph;
	}

	@Override
	public void toggleTorqueVisibility(final boolean visibility) {
		super.toggleTorqueVisibility(visibility);
		if (shell != null && !shell.isDisposed()) {
			if (visibility) {
				shell.setText(Messages.get("lbl.graph.dialog.title.power.torque", bikeType.getDisplacement()));
			}
			else {
				shell.setText(Messages.get("lbl.graph.dialog.title.power", bikeType.getDisplacement()));
			}
		}
	}

}