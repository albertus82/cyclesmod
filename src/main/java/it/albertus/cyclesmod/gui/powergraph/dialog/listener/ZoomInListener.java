package it.albertus.cyclesmod.gui.powergraph.dialog.listener;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.ZoomCommand;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import lombok.NonNull;

public class ZoomInListener extends ZoomListener {

	private static final Messages messages = GuiMessages.INSTANCE;

	public ZoomInListener(final IXYGraph xyGraph) {
		super(xyGraph);
	}

	@Override
	public void keyPressed(@NonNull final KeyEvent e) {
		if ('+' == e.keyCode || SWT.KEYPAD_ADD == e.keyCode) {
			execute();
		}
	}

	@Override
	protected void execute() {
		final SaveStateCommand command = new ZoomCommand(messages.get("gui.lbl.graph.action.zoomIn"), xyGraph.getXAxisList(), xyGraph.getYAxisList());
		final Point plotAreaCenter = getPlotAreaCenter();
		xyGraph.getPlotArea().zoomInOut(true, true, plotAreaCenter.x, plotAreaCenter.y, ZOOM_RATIO);
		saveUndo(command);
	}

}
