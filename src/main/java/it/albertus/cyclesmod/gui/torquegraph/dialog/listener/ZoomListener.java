package it.albertus.cyclesmod.gui.torquegraph.dialog.listener;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public abstract class ZoomListener implements KeyListener, SelectionListener {

	protected static final double ZOOM_RATIO = 0.1;

	protected final IXYGraph xyGraph;

	public ZoomListener(final IXYGraph graph) {
		this.xyGraph = graph;
	}

	protected abstract void execute();

	@Override
	public void widgetSelected(final SelectionEvent e) {
		execute();
	}

	protected Point getPlotAreaCenter() {
		final PlotArea plotArea = xyGraph.getPlotArea();
		return new Point(plotArea.getSize().width / 2 + plotArea.getLocation().x, plotArea.getSize().height / 2 + plotArea.getLocation().y);
	}

	protected void saveUndo(final SaveStateCommand command) {
		command.saveState();
		xyGraph.getOperationsManager().addCommand(command);
	}

	@Override
	public final void keyReleased(final KeyEvent e) {/* Ignore */}

	@Override
	public final void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

}
