package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.ZoomCommand;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import it.albertus.cycles.resources.Messages;

public class ZoomOutListener extends ZoomListener {

	public ZoomOutListener(final IXYGraph xyGraph) {
		super(xyGraph);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if ('-' == e.keyCode || SWT.KEYPAD_SUBTRACT == e.keyCode) {
			execute();
		}
	}

	@Override
	protected void execute() {
		final SaveStateCommand command = new ZoomCommand(Messages.get("lbl.graph.action.zoomOut"), xyGraph.getXAxisList(), xyGraph.getYAxisList());
		final Point plotAreaCenter = getPlotAreaCenter();
		xyGraph.getPlotArea().zoomInOut(true, true, plotAreaCenter.x, plotAreaCenter.y, ZOOM_RATIO * -1);
		saveUndo(command);
	}

}
