package it.albertus.cycles.gui.torquegraph.dialog.listener;

import java.text.NumberFormat;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;

import it.albertus.cycles.gui.torquegraph.dialog.ComplexTorqueGraph;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

public class UpdateTitleListener implements MouseMotionListener {

	private static final double NM_TO_LBFT = 1.35581794884;

	private final ComplexTorqueGraph torqueGraph;
	private final NumberFormat numberFormat;

	private String lastPosition;

	public UpdateTitleListener(final ComplexTorqueGraph torqueGraph) {
		this.torqueGraph = torqueGraph;
		numberFormat = NumberFormat.getNumberInstance(Messages.getLanguage().getLocale());
		numberFormat.setMaximumFractionDigits(1);
	}

	@Override
	public void mouseDragged(final MouseEvent me) {
		execute(me.getLocation());
	}

	@Override
	public void mouseExited(final MouseEvent me) {
		lastPosition = " ";
		torqueGraph.getXyGraph().setTitle(lastPosition);
	}

	@Override
	public void mouseMoved(final MouseEvent me) {
		execute(me.getLocation());
	}

	private void execute(final Point location) {
		final short torqueValue = torqueGraph.getTorqueValue(location);
		final String currentPosition = Messages.get("lbl.graph.torqueAtRpm", torqueValue, numberFormat.format(torqueValue / NM_TO_LBFT), Torque.getRpm(torqueGraph.getTorqueIndex(location)));
		if (!currentPosition.equals(lastPosition)) {
			lastPosition = currentPosition;
			torqueGraph.getXyGraph().setTitle(lastPosition);
		}
	}

	@Override
	public void mouseEntered(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseHover(final MouseEvent me) {/* Ignore */}

}
