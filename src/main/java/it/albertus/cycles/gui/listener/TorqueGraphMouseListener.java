package it.albertus.cycles.gui.listener;

import java.util.Map;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;

import it.albertus.cycles.gui.FormProperty;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.Torque;

public class TorqueGraphMouseListener implements MouseListener {

	private final Bike bike;
	private final Map<String, FormProperty> formProperties;

	public TorqueGraphMouseListener(final Bike bike, final Map<String, FormProperty> formProperties) {
		this.bike = bike;
		this.formProperties = formProperties;
	}

	@Override
	public void mousePressed(final MouseEvent me) {
		final XYGraph xyGraph = (XYGraph) ((PlotArea) me.getSource()).getParent();
		final double rpm = xyGraph.getPrimaryXAxis().getPositionValue(me.getLocation().x, false) * 1000;
		final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(bike.getType(), Torque.class, Torque.indexOf(rpm)));
		if (formProperty != null) {
			formProperty.getText().setFocus();
		}
	}

	@Override
	public void mouseDoubleClicked(final MouseEvent me) {/* Ignore */}

	@Override
	public void mouseReleased(final MouseEvent me) {/* Ignore */}

}
