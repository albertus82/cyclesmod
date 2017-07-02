package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;

import it.albertus.cycles.gui.torquegraph.TorqueGraph;
import it.albertus.cycles.gui.torquegraph.dialog.ComplexTorqueGraph;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

public class ChangeValueListener implements MouseListener, MouseMotionListener {

	private final ComplexTorqueGraph torqueGraph;

	private Point mouseEnteredLocation; // needed to manage the double click (maximize) on the title bar.

	public ChangeValueListener(final ComplexTorqueGraph torqueGraph) {
		this.torqueGraph = torqueGraph;
	}

	@Override
	public void mouseDragged(final MouseEvent me) {
		if (ZoomType.NONE.equals(torqueGraph.getXyGraph().getZoomType()) && !me.getLocation().equals(mouseEnteredLocation)) {
			execute(me.getLocation());
		}
	}

	@Override
	public void mouseEntered(final MouseEvent me) {
		mouseEnteredLocation = me.getLocation();
	}

	@Override
	public void mousePressed(final MouseEvent me) {
		if (me.button == 1) { // left click
			execute(me.getLocation());
		}
	}

	private void execute(final Point location) {
		final double rpm = torqueGraph.getAbscissae().getPositionValue(location.x, false) * TorqueGraph.RPM_DIVISOR;
		final int index = Math.max(Math.min(Torque.indexOf(rpm), Torque.LENGTH - 1), 0);
		final double[] values = torqueGraph.getValues();
		final short oldValue = (short) values[index];
		final short newValue = torqueGraph.getTorqueValue(location);
		if (oldValue != newValue) {
			values[index] = newValue;
			torqueGraph.refresh();
			torqueGraph.getXyGraph().getOperationsManager().addCommand(new IUndoableCommand() {
				@Override
				public void undo() {
					values[index] = oldValue;
					torqueGraph.refresh();
				}

				@Override
				public void redo() {
					values[index] = newValue;
					torqueGraph.refresh();
				}

				@Override
				public String toString() {
					return Messages.get("lbl.graph.action.valueChange");
				};
			});
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
