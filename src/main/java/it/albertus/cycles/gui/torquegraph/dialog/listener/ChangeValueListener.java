package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;

import it.albertus.cycles.gui.torquegraph.ITorqueGraph;
import it.albertus.cycles.gui.torquegraph.dialog.ChangeValueCommand;

public class ChangeValueListener implements MouseListener, MouseMotionListener {

	private static final int BUTTON_LEFT = 1;

	private final ITorqueGraph torqueGraph;

	private int mouseButton; // needed to detect drag only for left button.
	private Point mouseEnteredLocation; // needed to manage double click (maximize) on the title bar.

	public ChangeValueListener(final ITorqueGraph torqueGraph) {
		this.torqueGraph = torqueGraph;
	}

	@Override
	public void mouseDragged(final MouseEvent me) {
		if (BUTTON_LEFT == mouseButton && ZoomType.NONE.equals(torqueGraph.getXyGraph().getZoomType()) && !me.getLocation().equals(mouseEnteredLocation)) {
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
		final int index = torqueGraph.getTorqueIndex(location);
		final short oldValue = (short) torqueGraph.getValues()[index];
		final short newValue = torqueGraph.getTorqueValue(location);
		if (oldValue != newValue) {
			torqueGraph.getValues()[index] = newValue;
			torqueGraph.refresh();
			torqueGraph.getXyGraph().getOperationsManager().addCommand(new ChangeValueCommand(torqueGraph, index, oldValue, newValue));
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
