package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public abstract class ZoomListener implements KeyListener, SelectionListener {

	protected static final double ZOOM_RATIO = 0.1;

	protected final IXYGraph graph;

	public ZoomListener(final IXYGraph graph) {
		this.graph = graph;
	}

	protected void afterZoom(final SaveStateCommand command) {
		command.saveState();
		graph.getOperationsManager().addCommand(command);
	}

	protected static int getX(final PlotArea plotArea) {
		return plotArea.getSize().width / 2 + plotArea.getLocation().x;
	}

	protected static int getY(final PlotArea plotArea) {
		return plotArea.getSize().height / 2 + plotArea.getLocation().y;
	}

	protected abstract void execute();

	@Override
	public final void keyReleased(final KeyEvent e) {/* Ignore */}

	@Override
	public final void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

}
