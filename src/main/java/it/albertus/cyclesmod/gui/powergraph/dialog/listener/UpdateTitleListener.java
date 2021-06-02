package it.albertus.cyclesmod.gui.powergraph.dialog.listener;

import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;

import it.albertus.cyclesmod.common.model.Power;
import it.albertus.cyclesmod.common.resources.ConfigurableMessages;
import it.albertus.cyclesmod.gui.Mode;
import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import it.albertus.cyclesmod.gui.powergraph.PowerGraph;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import lombok.NonNull;

public class UpdateTitleListener extends MouseMotionListener.Stub {

	private static final double NM_TO_LBFT = 0.73756214927727;
	private static final double KW_TO_HP = 1.3404825737265;

	private static final float GPC_FACTOR = 6.8f; // Ferrari F1/87/88C (Ferrari 3.5L V12 - 680 bhp (507 kW; 689 PS))

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private final IPowerGraph powerGraph;
	private final Mode mode;
	private final NumberFormat numberFormat;

	private String lastPosition;

	public UpdateTitleListener(@NonNull final IPowerGraph powerGraph, @NonNull final Mode mode) {
		this.powerGraph = powerGraph;
		this.mode = mode;
		numberFormat = NumberFormat.getNumberInstance(messages.getLanguage().getLocale());
		numberFormat.setMaximumFractionDigits(1);
	}

	@Override
	public void mouseDragged(@NonNull final MouseEvent me) {
		handleEvent(me.getLocation());
	}

	@Override
	public void mouseExited(final MouseEvent me) {
		lastPosition = " ";
		powerGraph.getXyGraph().setTitle(lastPosition);
	}

	@Override
	public void mouseMoved(@NonNull final MouseEvent me) {
		handleEvent(me.getLocation());
	}

	private void handleEvent(final Point location) {
		final short value = powerGraph.getPowerValue(location);
		final short hp = Mode.GPC.equals(mode) ? (short) (value * GPC_FACTOR) : value;
		final double kw = hp / KW_TO_HP;
		final int rpm = Power.getRpm(powerGraph.getPowerIndex(location));
		final double nm = PowerGraph.hpToNm(hp, rpm);
		final double lbft = nm * NM_TO_LBFT;
		final String currentPosition;
		if (Mode.GPC.equals(mode)) {
			currentPosition = messages.get("gui.label.graph.powerAtRpm." + mode.getGame().toString().toLowerCase(Locale.ROOT), value, hp, numberFormat.format(kw), numberFormat.format(nm), numberFormat.format(lbft), rpm);
		}
		else {
			currentPosition = messages.get("gui.label.graph.powerAtRpm." + mode.getGame().toString().toLowerCase(Locale.ROOT), hp, numberFormat.format(kw), numberFormat.format(nm), numberFormat.format(lbft), rpm);
		}
		if (!currentPosition.equals(lastPosition)) {
			lastPosition = currentPosition;
			powerGraph.getXyGraph().setTitle(lastPosition);
		}
	}

}
