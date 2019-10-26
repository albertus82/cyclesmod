package it.albertus.cyclesmod.gui.powergraph.dialog.listener;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;

import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import it.albertus.cyclesmod.gui.powergraph.dialog.ChangeValueCommand;

public class ChangeValueListener implements MouseListener, MouseMotionListener {

	private static final int BUTTON_LEFT = 1;

	private final IPowerGraph powerGraph;

	private int mouseButton; // needed to detect drag only for left button.
	private Point mouseEnteredLocation; // needed to manage double click (maximize) on the title bar.

	public ChangeValueListener(final IPowerGraph powerGraph) {
		this.powerGraph = powerGraph;
	}

	@Override
	public void mouseDragged(final MouseEvent me) {
		if (BUTTON_LEFT == mouseButton && ZoomType.NONE.equals(powerGraph.getXyGraph().getZoomType()) && !me.getLocation().equals(mouseEnteredLocation)) {
			execute(me.getLocation());
		}
	}

	@Override
	public void mouseEntered(final MouseEvent me) {
		mouseEnteredLocation = me.getLocation();
	}

	@Override
	public void mousePressed(final MouseEvent me) {
		mouseButton = me.button;
		if (BUTTON_LEFT == mouseButton) { // left click
			execute(me.getLocation());
		}
	}

	private void execute(final Point location) {
		final int index = powerGraph.getPowerIndex(location);
		final short oldValue = (short) powerGraph.getValue(index);
		final short newValue = powerGraph.getPowerValue(location);
		if (oldValue != newValue) {
			powerGraph.setValue(index, newValue);
			powerGraph.refresh();
			powerGraph.getXyGraph().getOperationsManager().addCommand(new ChangeValueCommand(powerGraph, index, oldValue, newValue));
		}
	}

	@Override
	public void mouseExited(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseHover(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseMoved(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseReleased(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseDoubleClicked(final MouseEvent me) {/* Ignore */}

}
