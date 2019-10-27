package it.albertus.cyclesmod.gui.powergraph.dialog.listener;

import java.text.NumberFormat;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;

import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import it.albertus.cyclesmod.model.Power;
import it.albertus.cyclesmod.resources.Messages;

public class UpdateTitleListener implements MouseMotionListener {

	private static final double NM_TO_LBFT = 1.35581794884;
	private static final double HP_TO_KW = 0.7457;

	private final IPowerGraph powerGraph;
	private final NumberFormat numberFormat;

	private String lastPosition;

	public UpdateTitleListener(final IPowerGraph powerGraph) {
		this.powerGraph = powerGraph;
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
		powerGraph.getXyGraph().setTitle(lastPosition);
	}

	@Override
	public void mouseMoved(final MouseEvent me) {
		execute(me.getLocation());
	}

	private void execute(final Point location) {
		final short powerValue = powerGraph.getPowerValue(location);
		final String currentPosition = Messages.get("lbl.graph.powerAtRpm", powerValue, numberFormat.format(powerValue * HP_TO_KW), Power.getRpm(powerGraph.getPowerIndex(location)));
		if (!currentPosition.equals(lastPosition)) {
			lastPosition = currentPosition;
			powerGraph.getXyGraph().setTitle(lastPosition);
		}
	}

	@Override
	public void mouseEntered(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseHover(final MouseEvent me) {/* Ignore */}

}
