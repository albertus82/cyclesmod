package it.albertus.cyclesmod.gui.listener;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.FormProperty;
import it.albertus.cyclesmod.gui.powergraph.dialog.PowerGraphDialog;
import it.albertus.cyclesmod.model.BikesCfg;
import it.albertus.cyclesmod.model.Power;
import it.albertus.cyclesmod.model.Bike.BikeType;

public class OpenPowerGraphDialogListener implements MouseListener, SelectionListener {

	private final CyclesModGui gui;
	private final BikeType bikeType;

	public OpenPowerGraphDialogListener(final CyclesModGui gui, final BikeType bikeType) {
		this.gui = gui;
		this.bikeType = bikeType;
	}

	@Override
	public void mouseDoubleClick(final MouseEvent e) {
		execute();
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		execute();
	}

	@Override
	public void mouseDown(final MouseEvent e) {/* Ignore */}

	@Override
	public void mouseUp(final MouseEvent e) {/* Ignore */}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

	private void execute() {
		final PowerGraphDialog powerGraphDialog = new PowerGraphDialog(gui.getShell());
		final Map<Integer, Short> map = new TreeMap<Integer, Short>();
		final Map<String, FormProperty> formProperties = gui.getTabs().getFormProperties();
		for (int i = 0; i < Power.LENGTH; i++) {
			final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(bikeType, Power.class, i));
			map.put(Power.getRpm(i), Short.valueOf(formProperty.getValue(), gui.getNumeralSystem().getRadix()));
		}

		if (powerGraphDialog.open(map, bikeType) == SWT.OK) {
			for (int i = 0; i < Power.LENGTH; i++) {
				final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(bikeType, Power.class, i));
				final Text text = formProperty.getText();
				final String oldValue = text.getText();
				final String newValue = Long.toString(Math.max(Power.MIN_VALUE, Math.min(Power.MAX_VALUE, Math.round(powerGraphDialog.getPowerGraph().getValue(i)))), gui.getNumeralSystem().getRadix());
				if (!oldValue.equals(newValue)) {
					text.setText(newValue);
					text.notifyListeners(SWT.FocusOut, null);
				}
			}
		}
	}

}
