package it.albertus.cyclesmod.gui.powergraph.dialog;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;

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

import it.albertus.cyclesmod.common.model.VehicleType;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.Mode;
import it.albertus.cyclesmod.gui.powergraph.PowerGraph;
import it.albertus.cyclesmod.gui.powergraph.dialog.listener.ChangeValueListener;
import it.albertus.cyclesmod.gui.powergraph.dialog.listener.UpdateTitleListener;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class ComplexPowerGraph extends PowerGraph {

	private static final byte DEFAULT_POINT_SIZE = 6;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = false;

	private static final float TITLE_HEIGHT_FACTOR = 0.80f;

	private static final Messages messages = GuiMessages.INSTANCE;

	private final Shell shell;
	private final Mode mode;
	private final VehicleType vehicleType;

	@Getter
	private final ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(getXyGraph());

	public ComplexPowerGraph(@NonNull final Map<Integer, Short> map, @NonNull final Mode mode, @NonNull final VehicleType vehicleType, @NonNull final Shell shell) {
		super(map, vehicleType, () -> mode);
		this.shell = shell;
		this.mode = mode;
		this.vehicleType = vehicleType;

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

		customizeToolbarButtons(toolbarArmedXYGraph);

		final IXYGraph xyGraph = getXyGraph();
		final PlotArea plotArea = xyGraph.getPlotArea();
		final ChangeValueListener changeValueListener = new ChangeValueListener(this);
		plotArea.addMouseListener(changeValueListener);
		plotArea.addMouseMotionListener(changeValueListener);

		// Title
		final FontData titleFontData = xyGraph.getTitleFontData();
		xyGraph.setTitleFont(XYGraphMediaFactory.getInstance().getFont(titleFontData.getName(), Math.round(titleFontData.getHeight() * TITLE_HEIGHT_FACTOR), SWT.NORMAL));
		xyGraph.setTitle(" ");
		plotArea.addMouseMotionListener(new UpdateTitleListener(this, mode));

		abscissae.performAutoScale(true);
		ordinates.performAutoScale(true);
	}

	@SuppressWarnings("unchecked")
	private static void customizeToolbarButtons(@NonNull final ToolbarArmedXYGraph toolbarArmedXYGraph) {
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
							button.setToolTip(new Label(messages.get("gui.label.graph.toolbar.undo", "")));
							addUndoListener(button, manager);
						}
						else if ("redo".equalsIgnoreCase(labelText)) {
							button.setToolTip(new Label(messages.get("gui.label.graph.toolbar.redo", "")));
							addRedoListener(button, manager);
						}
					}
				}
			}
		}
		catch (final Exception e) {
			log.log(Level.WARNING, "Cannot customize toolbar buttons:", e);
		}
	}

	private static void addUndoListener(@NonNull final GrayableButton button, @NonNull final OperationsManager manager) {
		manager.addListener(m -> {
			final int undoCommandsSize = m.getUndoCommandsSize();
			if (undoCommandsSize > 0) {
				button.setEnabled(true);
				final String cmdName = m.getUndoCommands()[undoCommandsSize - 1].toString();
				button.setToolTip(new Label(messages.get("gui.label.graph.toolbar.undo", cmdName)));
			}
			else {
				button.setEnabled(false);
				button.setToolTip(new Label(messages.get("gui.label.graph.toolbar.undo", "")));
			}
		});
	}

	private static void addRedoListener(@NonNull final GrayableButton button, @NonNull final OperationsManager manager) {
		manager.addListener(m -> {
			final int redoCommandsSize = m.getRedoCommandsSize();
			if (redoCommandsSize > 0) {
				button.setEnabled(true);
				final String cmdName = m.getRedoCommands()[redoCommandsSize - 1].toString();
				button.setToolTip(new Label(messages.get("gui.label.graph.toolbar.redo", cmdName)));
			}
			else {
				button.setEnabled(false);
				button.setToolTip(new Label(messages.get("gui.label.graph.toolbar.redo", "")));
			}
		});
	}

	@Override
	public void toggleTorqueVisibility(final boolean visibility) {
		super.toggleTorqueVisibility(visibility);
		if (shell != null && !shell.isDisposed()) {
			if (visibility) {
				shell.setText(messages.get("gui.label.graph.dialog.title.power.torque", vehicleType.getDescription(mode.getGame())));
			}
			else {
				shell.setText(messages.get("gui.label.graph.dialog.title.power", vehicleType.getDescription(mode.getGame())));
			}
		}
	}

}
