package io.github.albertus82.cyclesmod.gui.torquegraph.dialog.listener;

import java.text.NumberFormat;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;

import io.github.albertus82.cyclesmod.common.model.Torque;
import io.github.albertus82.cyclesmod.common.resources.ConfigurableMessages;
import io.github.albertus82.cyclesmod.gui.Mode;
import io.github.albertus82.cyclesmod.gui.resources.GuiMessages;
import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraph;
import lombok.NonNull;

public class UpdateTitleListener extends MouseMotionListener.Stub {

	private static final double NM_TO_LBFT = 0.73756214927727;
	private static final double KW_TO_HP = 1.3404825737265;

	private static final float CYCLES_TORQUE_FACTOR = 0.45f;
	private static final float GPC_TORQUE_FACTOR = 3.4f;

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private final TorqueGraph torqueGraph;
	private final Mode mode;
	private final NumberFormat numberFormat;

	private String lastPosition;

	public UpdateTitleListener(@NonNull final TorqueGraph torqueGraph, @NonNull final Mode mode) {
		this.torqueGraph = torqueGraph;
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
		torqueGraph.getXyGraph().setTitle(lastPosition);
	}

	@Override
	public void mouseMoved(@NonNull final MouseEvent me) {
		handleEvent(me.getLocation());
	}

	private void handleEvent(final Point location) {
		final short torque = torqueGraph.getTorqueValue(location);
		final int rpm = Torque.getRpm(torqueGraph.getTorqueIndex(location));
		final double torqueLbFt = torque * (Mode.CYCLES.equals(mode) ? CYCLES_TORQUE_FACTOR : GPC_TORQUE_FACTOR);
		final double powerHp = (torqueLbFt * rpm) / 5252;
		final String currentPosition = messages.get("gui.label.graph.torqueAtRpm", torque, numberFormat.format(torqueLbFt), numberFormat.format(torqueLbFt / NM_TO_LBFT), numberFormat.format(powerHp), numberFormat.format(powerHp / KW_TO_HP), rpm);
		if (!currentPosition.equals(lastPosition)) {
			lastPosition = currentPosition;
			torqueGraph.getXyGraph().setTitle(lastPosition);
		}
	}

}
