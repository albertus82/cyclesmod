package it.albertus.cyclesmod.gui.powergraph.dialog.listener;

import java.text.NumberFormat;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;

import it.albertus.cyclesmod.common.model.Power;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import it.albertus.cyclesmod.gui.powergraph.PowerGraph;

public class UpdateTitleListener implements MouseMotionListener {

	private static final double NM_TO_LBFT = 0.73756214927727;
	private static final double KW_TO_HP = 1.3404825737265;

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
		final short hp = powerGraph.getPowerValue(location);
		final double kw = hp / KW_TO_HP;
		final int rpm = Power.getRpm(powerGraph.getPowerIndex(location));
		final double nm = PowerGraph.hpToNm(hp, rpm);
		final double lbft = nm * NM_TO_LBFT;
		final String currentPosition = Messages.get("lbl.graph.powerAtRpm", hp, numberFormat.format(kw), numberFormat.format(nm), numberFormat.format(lbft), rpm);
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
