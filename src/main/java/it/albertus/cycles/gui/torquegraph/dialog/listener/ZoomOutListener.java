package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.ZoomCommand;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;

public class ZoomOutListener extends ZoomListener {

	public ZoomOutListener(final IXYGraph graph) {
		super(graph);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if ('-' == e.keyCode || SWT.KEYPAD_SUBTRACT == e.keyCode) {
			execute();
		}
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		execute();
	}

	@Override
	protected void execute() {
		final SaveStateCommand command = new ZoomCommand(ZoomType.ZOOM_OUT.getDescription(), graph.getXAxisList(), graph.getYAxisList());
		final PlotArea plotArea = graph.getPlotArea();
		plotArea.zoomInOut(true, true, getX(plotArea), getY(plotArea), ZOOM_RATIO * -1);
		afterZoom(command);
	}

}
