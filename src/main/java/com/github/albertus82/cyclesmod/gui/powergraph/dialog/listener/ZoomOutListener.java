package com.github.albertus82.cyclesmod.gui.powergraph.dialog.listener;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.ZoomCommand;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import com.github.albertus82.cyclesmod.common.resources.Messages;
import com.github.albertus82.cyclesmod.gui.resources.GuiMessages;

import lombok.NonNull;

public class ZoomOutListener extends ZoomListener {

	private static final Messages messages = GuiMessages.INSTANCE;

	public ZoomOutListener(final IXYGraph xyGraph) {
		super(xyGraph);
	}

	@Override
	public void keyPressed(@NonNull final KeyEvent e) {
		if ('-' == e.keyCode || SWT.KEYPAD_SUBTRACT == e.keyCode) {
			handleEvent();
		}
	}

	@Override
	protected void handleEvent() {
		final SaveStateCommand command = new ZoomCommand(messages.get("gui.label.graph.action.zoomOut"), xyGraph.getXAxisList(), xyGraph.getYAxisList());
		final Point plotAreaCenter = getPlotAreaCenter();
		xyGraph.getPlotArea().zoomInOut(true, true, plotAreaCenter.x, plotAreaCenter.y, ZOOM_RATIO * -1);
		saveUndo(command);
	}

}
