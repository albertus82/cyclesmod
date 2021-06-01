package it.albertus.cyclesmod.gui.powergraph.dialog.listener;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ZoomListener extends SelectionAdapter implements KeyListener {

	protected static final double ZOOM_RATIO = 0.1;

	protected final IXYGraph xyGraph;

	protected abstract void handleEvent();

	@Override
	public void widgetSelected(final SelectionEvent e) {
		handleEvent();
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

}
