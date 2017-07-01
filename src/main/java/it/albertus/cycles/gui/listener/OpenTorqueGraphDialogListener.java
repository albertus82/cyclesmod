package it.albertus.cycles.gui.listener;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.gui.FormProperty;
import it.albertus.cycles.gui.torquegraph.dialog.TorqueGraphDialog;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.Torque;

public class OpenTorqueGraphDialogListener implements MouseListener, SelectionListener {

	private final CyclesModGui gui;
	private final BikeType bikeType;

	public OpenTorqueGraphDialogListener(final CyclesModGui gui, final BikeType bikeType) {
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
		final TorqueGraphDialog torqueGraphDialog = new TorqueGraphDialog(gui.getShell());
		final Map<Integer, Short> map = new TreeMap<Integer, Short>();
		final Map<String, FormProperty> formProperties = gui.getTabs().getFormProperties();
		for (int i = 0; i < Torque.LENGTH; i++) {
			final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(bikeType, Torque.class, i));
			map.put(Torque.getRpm(i), Short.valueOf(formProperty.getValue(), gui.getNumeralSystem().getRadix()));
		}

		if (torqueGraphDialog.open(map, bikeType) == SWT.OK) {
			for (int i = 0; i < Torque.LENGTH; i++) {
				final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(bikeType, Torque.class, i));
				final Text text = formProperty.getText();
				final String oldValue = text.getText();
				final String newValue = Long.toString(Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, Math.round(torqueGraphDialog.getTorqueGraph().getValues()[i]))), gui.getNumeralSystem().getRadix());
				if (!oldValue.equals(newValue)) {
					text.setText(newValue);
					text.notifyListeners(SWT.FocusOut, null);
				}
			}
		}
	}

}
