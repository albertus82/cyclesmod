package it.albertus.cyclesmod.gui.listener;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.common.model.VehicleType;
import it.albertus.cyclesmod.common.model.VehiclesCfg;
import it.albertus.cyclesmod.common.model.Power;
import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.model.FormProperty;
import it.albertus.cyclesmod.gui.powergraph.dialog.PowerGraphDialog;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenPowerGraphDialogListener extends MouseAdapter implements SelectionListener {

	@NonNull private final CyclesModGui gui;
	@NonNull private final VehicleType bikeType;

	@Override
	public void mouseDoubleClick(final MouseEvent e) {
		handleEvent();
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		handleEvent();
	}

	private void handleEvent() {
		final PowerGraphDialog powerGraphDialog = new PowerGraphDialog(gui.getShell());
		final Map<Integer, Short> map = new TreeMap<>();
		final Map<String, FormProperty> formProperties = gui.getTabs().getFormProperties();
		for (int i = 0; i < Power.LENGTH; i++) {
			final FormProperty formProperty = formProperties.get(VehiclesCfg.buildPropertyKey(bikeType, Power.PREFIX, i));
			map.put(Power.getRpm(i), Short.valueOf(formProperty.getValue(), gui.getNumeralSystem().getRadix()));
		}

		if (powerGraphDialog.open(map, bikeType, false) == SWT.OK) {
			for (int i = 0; i < Power.LENGTH; i++) {
				final FormProperty formProperty = formProperties.get(VehiclesCfg.buildPropertyKey(bikeType, Power.PREFIX, i));
				final Text text = formProperty.getText();
				final String oldValue = text.getText();
				final String newValue = Long.toString(Math.max(Power.MIN_VALUE, Math.min(Power.MAX_VALUE, Math.round(powerGraphDialog.getPowerGraph().getPowerValue(i)))), gui.getNumeralSystem().getRadix());
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
