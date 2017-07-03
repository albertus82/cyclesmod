package it.albertus.cycles.gui.torquegraph.dialog.listener;

import java.util.Arrays;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;

import it.albertus.cycles.gui.torquegraph.dialog.ChangeValueCommand;
import it.albertus.cycles.gui.torquegraph.dialog.ComplexTorqueGraph;
import it.albertus.cycles.gui.torquegraph.dialog.PlotCurveCommand;
import it.albertus.cycles.model.Torque;

public class EditCurveListener implements MouseListener, MouseMotionListener {

	private static final int BUTTON_LEFT = 1;

	private final ComplexTorqueGraph torqueGraph;

	private int mouseButton; // needed to detect drag only for left button.
	private Point mouseEnteredLocation; // needed to manage double click (maximize) on the title bar.
	private short[] initialValues; // needed to treat drag operations as atomic.
	private boolean dragged; // needed to distinguish drag and click operations.

	public EditCurveListener(final ComplexTorqueGraph torqueGraph) {
		this.torqueGraph = torqueGraph;
	}

	@Override
	public void mouseEntered(final MouseEvent me) {
		mouseEnteredLocation = me.getLocation();
		mouseReleased(me);
	}

	@Override
	public void mouseDragged(final MouseEvent me) {
		if (BUTTON_LEFT == mouseButton && ZoomType.NONE.equals(torqueGraph.getXyGraph().getZoomType()) && !me.getLocation().equals(mouseEnteredLocation)) {
			execute(me.getLocation(), true);
		}
	}

	@Override
	public void mousePressed(final MouseEvent me) {
		mouseButton = me.button;
		if (BUTTON_LEFT == mouseButton) { // left click
			initialValues = new short[Torque.LENGTH];
			for (int index = 0; index < initialValues.length; index++) {
				initialValues[index] = (short) torqueGraph.getValues()[index];
			}
			execute(me.getLocation(), false);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent me) {
		if (initialValues != null) {
			if (dragged) {
				final short[] newValues = new short[Torque.LENGTH];
				for (int index = 0; index < newValues.length; index++) {
					newValues[index] = (short) torqueGraph.getValues()[index];
				}
				if (!Arrays.equals(initialValues, newValues)) {
					torqueGraph.getXyGraph().getOperationsManager().addCommand(new PlotCurveCommand(torqueGraph, initialValues, newValues));
				}
				dragged = false;
			}
			else {
				final int index = torqueGraph.getTorqueIndex(me.getLocation());
				final short oldValue = initialValues[index];
				final short newValue = (short) torqueGraph.getValues()[index];
				if (oldValue != newValue) {
					torqueGraph.getXyGraph().getOperationsManager().addCommand(new ChangeValueCommand(torqueGraph, index, oldValue, newValue));
				}
			}
		}
	}

	private void execute(final Point location, final boolean dragged) {
		final int index = torqueGraph.getTorqueIndex(location);
		final short oldValue = (short) torqueGraph.getValues()[index];
		final short newValue = torqueGraph.getTorqueValue(location);
		if (oldValue != newValue) {
			torqueGraph.getValues()[index] = newValue;
			torqueGraph.refresh();
			if (dragged) {
				this.dragged = true;
			}
		}
	}

	@Override
	public void mouseExited(final MouseEvent me) {mouseReleased(me);}

	@Override
	public void mouseHover(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseMoved(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseDoubleClicked(final MouseEvent me) {/* Ignore */}

}
