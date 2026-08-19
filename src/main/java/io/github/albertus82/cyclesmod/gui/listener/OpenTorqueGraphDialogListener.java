package io.github.albertus82.cyclesmod.gui.listener;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;

import io.github.albertus82.cyclesmod.common.model.Torque;
import io.github.albertus82.cyclesmod.common.model.VehicleType;
import io.github.albertus82.cyclesmod.common.model.VehiclesCfg;
import io.github.albertus82.cyclesmod.gui.CyclesModGui;
import io.github.albertus82.cyclesmod.gui.model.FormProperty;
import io.github.albertus82.cyclesmod.gui.torquegraph.dialog.TorqueGraphDialog;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenTorqueGraphDialogListener extends MouseAdapter implements SelectionListener {

	@NonNull
	private final CyclesModGui gui;
	@NonNull
	private final VehicleType vehicleType;

	@Override
	public void mouseDoubleClick(final MouseEvent e) {
		handleEvent();
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		handleEvent();
	}

	private void handleEvent() {
		final TorqueGraphDialog powerGraphDialog = new TorqueGraphDialog(gui.getShell(), gui.getMode());
		final Map<Integer, Short> map = new TreeMap<>();
		final Map<String, FormProperty> formProperties = gui.getTabs().getFormProperties().get(gui.getMode());
		for (int i = 0; i < Torque.LENGTH; i++) {
			final FormProperty formProperty = formProperties.get(VehiclesCfg.buildPropertyKey(gui.getMode().getGame(), vehicleType, Torque.PREFIX, i));
			map.put(Torque.getRpm(i), Short.valueOf(formProperty.getValue(), gui.getNumeralSystem().getRadix()));
		}

		if (powerGraphDialog.open(map, vehicleType, false) == SWT.OK) {
			for (int i = 0; i < Torque.LENGTH; i++) {
				final FormProperty formProperty = formProperties.get(VehiclesCfg.buildPropertyKey(gui.getMode().getGame(), vehicleType, Torque.PREFIX, i));
				final Text text = formProperty.getText();
				final String oldValue = text.getText();
				final String newValue = Long.toString(Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, Math.round(powerGraphDialog.getTorqueGraph().getTorqueValue(i)))), gui.getNumeralSystem().getRadix());
				if (!oldValue.equals(newValue)) {
					text.setText(newValue);
					text.notifyListeners(SWT.FocusOut, null);
				}
			}
		}
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

}
