package it.albertus.cycles.gui.torquegraph.dialog;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.internal.xygraph.toolbar.GrayableButton;
import org.eclipse.nebula.visualization.internal.xygraph.undo.IOperationsManagerListener;
import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.OperationsManager;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

import it.albertus.cycles.gui.torquegraph.TorqueGraph;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class ComplexTorqueGraph extends TorqueGraph {

	private static final Logger logger = LoggerFactory.getLogger(ComplexTorqueGraph.class);

	private static final String MSG_KEY_LBL_GRAPH_TOOLBAR_REDO = "lbl.graph.toolbar.redo";
	private static final String MSG_KEY_LBL_GRAPH_TOOLBAR_UNDO = "lbl.graph.toolbar.undo";

	private static final byte DEFAULT_POINT_SIZE = 6;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = false;

	private final ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(getXyGraph());

	public ComplexTorqueGraph(final Map<Integer, Short> map, final BikeType bikeType) {
		super(map, bikeType);

		final Axis abscissae = getAbscissae();
		abscissae.setAutoScale(DEFAULT_AUTOSCALE);

		final Axis ordinates = getOrdinates();
		ordinates.setAutoScale(DEFAULT_AUTOSCALE);

		final Trace trace = getTrace();
		trace.setPointStyle(PointStyle.FILLED_DIAMOND);
		trace.setLineWidth(DEFAULT_LINE_WIDTH);
		trace.setPointSize(DEFAULT_POINT_SIZE);

		fixUndoRedoButtons();

		final IXYGraph xyGraph = getXyGraph();
		xyGraph.getPlotArea().addMouseListener(new MouseListener.Stub() {
			@Override
			public void mousePressed(final MouseEvent me) {
				if (me.button == 1) { // left click
					final double rpm = abscissae.getPositionValue(me.getLocation().x, false) * RPM_DIVISOR;
					final int index = Math.max(Math.min(Torque.indexOf(rpm), Torque.LENGTH - 1), 0);
					final double[] values = getValues();
					final short oldValue = (short) values[index];
					final short newValue = getTorqueValue(me.getLocation());
					if (oldValue != newValue) {
						values[index] = newValue;
						refresh();
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

							@Override
							public String toString() {
								return Messages.get("lbl.graph.action.valueChange");
							};
						});
					}
				}
			}
		});

		// Title
		final FontData titleFontData = xyGraph.getTitleFontData();
		xyGraph.setTitleFont(XYGraphMediaFactory.getInstance().getFont(titleFontData.getName(), Math.round(titleFontData.getHeight() * 0.80f), SWT.NORMAL));
		xyGraph.setTitle(" ");
		xyGraph.getPlotArea().addMouseMotionListener(new UpdateGraphTitleListener());

		abscissae.performAutoScale(true);
		ordinates.performAutoScale(true);
	}

	@SuppressWarnings("unchecked")
	protected void fixUndoRedoButtons() {
		try {
			final Field listenersField = OperationsManager.class.getDeclaredField("listeners");
			listenersField.setAccessible(true);
			for (final IOperationsManagerListener listener : (Collection<IOperationsManagerListener>) listenersField.get(getXyGraph().getOperationsManager())) {
				toolbarArmedXYGraph.getIXYGraph().getOperationsManager().removeListener(listener);
			}

			for (final Object o : toolbarArmedXYGraph.getToolbar().getChildren()) {
				if (o instanceof GrayableButton) {
					final GrayableButton button = (GrayableButton) o;
					if (button.getToolTip() instanceof Label) {
						final String labelText = ((Label) button.getToolTip()).getText();
						if ("undo".equalsIgnoreCase(labelText)) {
							button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_UNDO, "")));
							addUndoListener(button, toolbarArmedXYGraph.getIXYGraph().getOperationsManager());
						}
						else if ("redo".equalsIgnoreCase(labelText)) {
							button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_REDO, "")));
							addRedoListener(button, toolbarArmedXYGraph.getIXYGraph().getOperationsManager());
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
				if (manager.getUndoCommandsSize() > 0) {
					button.setEnabled(true);
					final String cmdName = manager.getUndoCommands()[manager.getUndoCommandsSize() - 1].toString();
					button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_UNDO, cmdName)));
				}
				else {
					button.setEnabled(false);
					button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_UNDO, "")));
				}
			}
		});
	}

	private static void addRedoListener(final GrayableButton button, final OperationsManager manager) {
		manager.addListener(new IOperationsManagerListener() {
			@Override
			public void operationsHistoryChanged(final OperationsManager manager) {
				if (manager.getRedoCommandsSize() > 0) {
					button.setEnabled(true);
					final String cmdName = manager.getRedoCommands()[manager.getRedoCommandsSize() - 1].toString();
					button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_REDO, cmdName)));
				}
				else {
					button.setEnabled(false);
					button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_REDO, "")));
				}
			}
		});
	}

	public ToolbarArmedXYGraph getToolbarArmedXYGraph() {
		return toolbarArmedXYGraph;
	}

	private short getTorqueValue(final Point location) {
		return (short) Math.round(Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, getOrdinates().getPositionValue(location.y, false))));
	}

	private class UpdateGraphTitleListener extends MouseMotionListener.Stub {

		private static final double NM_TO_LBFT = 1.35581794884;

		private String lastPosition;
		private final NumberFormat numberFormat;

		private UpdateGraphTitleListener() {
			numberFormat = NumberFormat.getNumberInstance(Messages.getLanguage().getLocale());
			numberFormat.setMaximumFractionDigits(1);
		}

		@Override
		public void mouseMoved(final MouseEvent me) {
			final int rpm = Torque.getRpm(Math.max(Math.min(Torque.indexOf(getAbscissae().getPositionValue(me.getLocation().x, false) * RPM_DIVISOR), Torque.LENGTH - 1), 0));
			final short torqueValue = getTorqueValue(me.getLocation());

			final String currentPosition = Messages.get("lbl.graph.torqueAtRpm", torqueValue, numberFormat.format(torqueValue / NM_TO_LBFT), rpm);
			if (!currentPosition.equals(lastPosition)) {
				lastPosition = currentPosition;
				getXyGraph().setTitle(lastPosition);
			}
		}

		@Override
		public void mouseExited(final MouseEvent me) {
			lastPosition = " ";
			getXyGraph().setTitle(lastPosition);
		}
	}

}
