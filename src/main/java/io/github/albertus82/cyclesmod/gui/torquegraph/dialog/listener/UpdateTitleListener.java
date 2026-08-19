package io.github.albertus82.cyclesmod.gui.torquegraph.dialog.listener;

import java.text.NumberFormat;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;

import io.github.albertus82.cyclesmod.common.model.Torque;
import io.github.albertus82.cyclesmod.common.resources.ConfigurableMessages;
import io.github.albertus82.cyclesmod.gui.Mode;
import io.github.albertus82.cyclesmod.gui.resources.GuiMessages;
import io.github.albertus82.cyclesmod.gui.torquegraph.BasicTorqueGraph;
import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraph;
import lombok.NonNull;

public class UpdateTitleListener extends MouseMotionListener.Stub {

	//	private static final double NM_TO_LBFT = 0.73756214927727;
	//	private static final double KW_TO_HP = 1.3404825737265;
	//
	//	private static final float GPC_FACTOR = 6.8f; // Ferrari F1/87/88C (Ferrari 3.5L V12 - 680 bhp (507 kW; 689 PS))

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
		final double power = BasicTorqueGraph.torqueToPower(torque, rpm);
		// TODO convert to human readable measure units
		final String currentPosition = messages.get("gui.label.graph.torqueAtRpm", torque, numberFormat.format(power), rpm);
		if (!currentPosition.equals(lastPosition)) {
			lastPosition = currentPosition;
			torqueGraph.getXyGraph().setTitle(lastPosition);
		}
	}

}
