package it.albertus.cyclesmod.gui.powergraph;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.xygraph.dataprovider.AbstractDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.ISample;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import it.albertus.cyclesmod.model.Bike;
import it.albertus.cyclesmod.model.Bike.BikeType;
import it.albertus.cyclesmod.model.Power;
import it.albertus.cyclesmod.resources.Messages;

public class PowerGraph implements IPowerGraph {

	public static final int RPM_DIVISOR = 1000;

	private static final IDataProvider nullDataProvider = new NullDataProvider();

	private final IXYGraph xyGraph = new XYGraph();
	private final Axis abscissae = xyGraph.getPrimaryXAxis();
	private final Axis ordinates = xyGraph.getPrimaryYAxis();
	private final CircularBufferDataProvider powerDataProvider = new CircularBufferDataProvider(false);
	private final CircularBufferDataProvider torqueDataProvider = new CircularBufferDataProvider(false);
	private final Trace powerTrace = new Trace(Messages.get("lbl.graph.trace.power"), abscissae, ordinates, powerDataProvider);
	private final Trace torqueTrace = new Trace(Messages.get("lbl.graph.trace.torque"), abscissae, ordinates, nullDataProvider);
	private final double[] powerValues = new double[Power.LENGTH];
	private final double[] torqueValues = new double[Power.LENGTH];
	private final double[] xDataArray = new double[Power.LENGTH];
	private boolean torqueVisible;

	public PowerGraph(final Bike bike) {
		for (int i = 0; i < Power.LENGTH; i++) {
			xDataArray[i] = (double) Power.getRpm(i) / RPM_DIVISOR;
			powerValues[i] = bike.getPower().getCurve()[i];
			torqueValues[i] = hpToNm(powerValues[i], Power.getRpm(i));
		}
		init(bike.getType());
	}

	public PowerGraph(final Map<Integer, Short> map, final BikeType bikeType) {
		if (map.size() != Power.LENGTH) {
			throw new IllegalArgumentException("map size must be " + Power.LENGTH);
		}

		int i = 0;
		for (final Entry<Integer, Short> entry : map.entrySet()) {
			xDataArray[i] = entry.getKey().doubleValue() / RPM_DIVISOR;
			powerValues[i] = entry.getValue();
			torqueValues[i] = hpToNm(powerValues[i], Power.getRpm(i));
			i++;
		}
		init(bikeType);
	}

	protected void init(final BikeType bikeType) {
		powerDataProvider.setBufferSize(xDataArray.length);
		powerDataProvider.setCurrentXDataArray(xDataArray);
		powerDataProvider.setCurrentYDataArray(powerValues);
		torqueDataProvider.setBufferSize(xDataArray.length);
		torqueDataProvider.setCurrentXDataArray(xDataArray);
		torqueDataProvider.setCurrentYDataArray(torqueValues);

		final Font axisTitleFont = Display.getCurrent().getSystemFont();

		abscissae.setTitle(Messages.get("lbl.graph.axis.x", RPM_DIVISOR));
		abscissae.setTitleFont(axisTitleFont);
		abscissae.setShowMajorGrid(true);

		ordinates.setTitle(Messages.get("lbl.graph.axis.y.power"));
		ordinates.setTitleFont(axisTitleFont);
		ordinates.setShowMajorGrid(true);

		xyGraph.addTrace(powerTrace);
		toggleTorqueVisibility(false);
		toggleTorqueVisibility(true);
		xyGraph.setShowLegend(false);

		powerTrace.setTraceColor(getColor(bikeType));
		torqueTrace.setTraceColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

	private static Color getColor(final BikeType bikeType) {
		final Display display = Display.getCurrent();
		switch (bikeType) {
		case CLASS_125:
			return display.getSystemColor(SWT.COLOR_RED);
		case CLASS_250:
			return display.getSystemColor(SWT.COLOR_BLUE);
		case CLASS_500:
			return display.getSystemColor(SWT.COLOR_BLACK);
		default:
			throw new IllegalStateException("Unknown bike type: " + bikeType);
		}
	}

	public static double hpToNm(final double hp, final int rpm) {
		final double kw = hp * 0.7457;
		return 9.5488 * kw / rpm * 1000;
	}

	@Override
	public void refresh() {
		powerDataProvider.triggerUpdate();
		torqueDataProvider.triggerUpdate();
	}

	@Override
	public IXYGraph getXyGraph() {
		return xyGraph;
	}

	@Override
	public Axis getAbscissae() {
		return abscissae;
	}

	@Override
	public Axis getOrdinates() {
		return ordinates;
	}

	@Override
	public CircularBufferDataProvider getDataProvider() {
		return powerDataProvider;
	}

	@Override
	public Trace getPowerTrace() {
		return powerTrace;
	}

	@Override
	public Trace getTorqueTrace() {
		return torqueTrace;
	}

	@Override
	public double getPowerValue(final int index) {
		return powerValues[index];
	}

	@Override
	public void setPowerValue(final int index, final double value) {
		powerValues[index] = value;
		torqueValues[index] = hpToNm(value, Power.getRpm(index));
	}

	@Override
	public short getPowerValue(final Point location) {
		return (short) Math.round(Math.max(Power.MIN_VALUE, Math.min(Power.MAX_VALUE, getOrdinates().getPositionValue(location.y, false))));
	}

	@Override
	public int getPowerIndex(final Point location) {
		return Math.max(Math.min(Power.indexOf(getAbscissae().getPositionValue(location.x, false) * RPM_DIVISOR), Power.LENGTH - 1), 0);
	}

	@Override
	public void toggleTorqueVisibility(final boolean visibility) {
		this.torqueVisible = visibility;
		if (visibility) {
			torqueTrace.setDataProvider(torqueDataProvider);
			xyGraph.addTrace(torqueTrace);
			ordinates.setTitle(Messages.get("lbl.graph.axis.y.power") + " / " + Messages.get("lbl.graph.axis.y.torque"));
		}
		else {
			xyGraph.removeTrace(torqueTrace);
			torqueTrace.setDataProvider(new NullDataProvider());
			ordinates.setTitle(Messages.get("lbl.graph.axis.y.power"));
		}
	}

	@Override
	public boolean isTorqueVisible() {
		return torqueVisible;
	}

	private static class NullDataProvider extends AbstractDataProvider {

		public NullDataProvider() {
			super(false);
		}

		@Override
		public int getSize() {
			return 0;
		}

		@Override
		public ISample getSample(int index) {
			return null;
		}
	}

}
