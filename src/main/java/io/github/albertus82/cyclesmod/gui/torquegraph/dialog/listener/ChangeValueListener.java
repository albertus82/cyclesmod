package io.github.albertus82.cyclesmod.gui.torquegraph.dialog.listener;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;

import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraph;
import io.github.albertus82.cyclesmod.gui.torquegraph.dialog.ChangeValueCommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeValueListener extends MouseMotionListener.Stub implements MouseListener {

	private static final int BUTTON_LEFT = 1;

	private final TorqueGraph torqueGraph;

	private int mouseButton; // needed to detect drag only for left button.
	private Point mouseEnteredLocation; // needed to manage double click (maximize) on the title bar.

	@Override
	public void mouseDragged(@NonNull final MouseEvent me) {
		if (BUTTON_LEFT == mouseButton && ZoomType.NONE.equals(torqueGraph.getXyGraph().getZoomType()) && !me.getLocation().equals(mouseEnteredLocation)) {
			execute(me.getLocation());
		}
	}

	@Override
	public void mouseEntered(@NonNull final MouseEvent me) {
		mouseEnteredLocation = me.getLocation();
	}

	@Override
	public void mousePressed(@NonNull final MouseEvent me) {
		mouseButton = me.button;
		if (BUTTON_LEFT == mouseButton) { // left click
			execute(me.getLocation());
		}
	}

	private void execute(final Point location) {
		final int index = torqueGraph.getTorqueIndex(location);
		final short oldValue = (short) torqueGraph.getTorqueValue(index);
		final short newValue = torqueGraph.getTorqueValue(location);
		if (oldValue != newValue) {
			torqueGraph.setTorqueValue(index, newValue);
			torqueGraph.refresh();
			torqueGraph.getXyGraph().getOperationsManager().addCommand(new ChangeValueCommand(torqueGraph, index, oldValue, newValue));
		}
	}

	@Override
	public void mouseReleased(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseDoubleClicked(final MouseEvent me) {/* Ignore */}

}
