package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.ZoomCommand;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;

public class ZoomInListener extends ZoomListener {

	public ZoomInListener(final IXYGraph graph) {
		super(graph);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if ('+' == e.keyCode || SWT.KEYPAD_ADD == e.keyCode) {
			execute();
		}
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		execute();
	}

	@Override
	protected void execute() {
		final SaveStateCommand command = new ZoomCommand(ZoomType.ZOOM_IN.getDescription(), graph.getXAxisList(), graph.getYAxisList());
		final PlotArea plotArea = graph.getPlotArea();
		plotArea.zoomInOut(true, true, getX(plotArea), getY(plotArea), ZOOM_RATIO);
		afterZoom(command);
	}

}
