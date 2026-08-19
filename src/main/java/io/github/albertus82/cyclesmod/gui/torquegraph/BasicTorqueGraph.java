package io.github.albertus82.cyclesmod.gui.torquegraph;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

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

import io.github.albertus82.cyclesmod.common.model.Torque;
import io.github.albertus82.cyclesmod.common.model.Vehicle;
import io.github.albertus82.cyclesmod.common.model.VehicleType;
import io.github.albertus82.cyclesmod.common.resources.Messages;
import io.github.albertus82.cyclesmod.gui.Mode;
import io.github.albertus82.cyclesmod.gui.resources.GuiMessages;
import lombok.Getter;
import lombok.NonNull;

public class BasicTorqueGraph implements TorqueGraph {

	public static final int RPM_DIVISOR = 1000;

	private static final Messages messages = GuiMessages.INSTANCE;

	private static final IDataProvider nullDataProvider = new NullDataProvider();

	@Getter
	private final IXYGraph xyGraph = new XYGraph();
	@Getter
	private final Axis abscissae = xyGraph.getPrimaryXAxis();
	@Getter
	private final Axis ordinates = xyGraph.getPrimaryYAxis();
	private final CircularBufferDataProvider powerDataProvider = new CircularBufferDataProvider(false);
	private final CircularBufferDataProvider torqueDataProvider = new CircularBufferDataProvider(false);
	@Getter
	private final Trace powerTrace = new Trace(messages.get("gui.label.graph.trace.power"), abscissae, ordinates, powerDataProvider);
	@Getter
	private final Trace torqueTrace = new Trace(messages.get("gui.label.graph.trace.torque"), abscissae, ordinates, nullDataProvider);
	private final double[] powerValues = new double[Torque.LENGTH];
	private final double[] torqueValues = new double[Torque.LENGTH];
	private final double[] xDataArray = new double[Torque.LENGTH];
	@Getter
	private final Supplier<Mode> modeSupplier;

	private boolean torqueVisible;

	public BasicTorqueGraph(@NonNull final Vehicle vehicle, @NonNull final Supplier<Mode> modeSupplier) {
		this.modeSupplier = modeSupplier;
		for (int i = 0; i < Torque.LENGTH; i++) {
			xDataArray[i] = (double) Torque.getRpm(i) / RPM_DIVISOR;
			powerValues[i] = vehicle.getPower().getCurve()[i];
			torqueValues[i] = hpToNm(powerValues[i], Torque.getRpm(i));
		}
		init(vehicle.getType());
	}

	public BasicTorqueGraph(@NonNull final Map<Integer, Short> powerMap, @NonNull final VehicleType vehicleType, @NonNull final Supplier<Mode> modeSupplier) {
		this.modeSupplier = modeSupplier;
		if (powerMap.size() != Torque.LENGTH) {
			throw new IllegalArgumentException("map size must be " + Torque.LENGTH);
		}

		int i = 0;
		for (final Entry<Integer, Short> entry : powerMap.entrySet()) {
			xDataArray[i] = entry.getKey().doubleValue() / RPM_DIVISOR;
			powerValues[i] = entry.getValue();
			torqueValues[i] = hpToNm(powerValues[i], Torque.getRpm(i));
			i++;
		}
		init(vehicleType);
	}

	protected void init(@NonNull final VehicleType vehicleType) {
		powerDataProvider.setBufferSize(xDataArray.length);
		powerDataProvider.setCurrentXDataArray(xDataArray);
		powerDataProvider.setCurrentYDataArray(powerValues);
		torqueDataProvider.setBufferSize(xDataArray.length);
		torqueDataProvider.setCurrentXDataArray(xDataArray);
		torqueDataProvider.setCurrentYDataArray(torqueValues);

		final Font axisTitleFont = Display.getCurrent().getSystemFont();

		abscissae.setTitle(messages.get("gui.label.graph.axis.x", RPM_DIVISOR));
		abscissae.setTitleFont(axisTitleFont);
		abscissae.setShowMajorGrid(true);

		ordinates.setTitle(messages.get("gui.label.graph.axis.y.power." + getModeSupplier().get().getGame().toString().toLowerCase(Locale.ROOT)));
		ordinates.setTitleFont(axisTitleFont);
		ordinates.setShowMajorGrid(true);

		xyGraph.addTrace(powerTrace);
		toggleTorqueVisibility(false);
		toggleTorqueVisibility(true);
		xyGraph.setShowLegend(false);

		powerTrace.setTraceColor(getColor(vehicleType));
		torqueTrace.setTraceColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

	private static Color getColor(@NonNull final VehicleType vehicleType) {
		final Display display = Display.getCurrent();
		switch (vehicleType) {
		case FERRARI_125:
			return display.getSystemColor(SWT.COLOR_RED);
		case MCLAREN_250:
			return display.getSystemColor(SWT.COLOR_BLUE);
		case WILLIAMS_500:
			return display.getSystemColor(SWT.COLOR_BLACK);
		default:
			throw new IllegalArgumentException("Unknown vehicle type: " + vehicleType);
		}
	}

	public static double hpToNm(final double torque, final int rpm) {
	//	final double kw = torque * 0.7457;
		//return 9.5488 * kw / rpm * 1000;
		return( torque/ 16) * (rpm/1000d);
	}

	@Override
	public void refresh() {
		powerDataProvider.triggerUpdate();
		torqueDataProvider.triggerUpdate();
	}

	@Override
	public CircularBufferDataProvider getDataProvider() {
		return powerDataProvider;
	}

	@Override
	public double getPowerValue(final int index) {
		return powerValues[index];
	}

	@Override
	public void setPowerValue(final int index, final double value) {
		powerValues[index] = value;
		torqueValues[index] = hpToNm(value, Torque.getRpm(index));
	}

	@Override
	public short getPowerValue(@NonNull final Point location) {
		return (short) Math.round(Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, getOrdinates().getPositionValue(location.y, false))));
	}

	@Override
	public int getPowerIndex(@NonNull final Point location) {
		return Math.max(Math.min(Torque.indexOf(getAbscissae().getPositionValue(location.x, false) * RPM_DIVISOR), Torque.LENGTH - 1), 0);
	}

	@Override
	public void toggleTorqueVisibility(final boolean visibility) {
		this.torqueVisible = visibility;
		if (visibility) {
			torqueTrace.setDataProvider(torqueDataProvider);
			xyGraph.addTrace(torqueTrace);
			ordinates.setTitle(messages.get("gui.label.graph.axis.y.power.torque." + getModeSupplier().get().getGame().toString().toLowerCase(Locale.ROOT)));
		}
		else {
			xyGraph.removeTrace(torqueTrace);
			torqueTrace.setDataProvider(new NullDataProvider());
			ordinates.setTitle(messages.get("gui.label.graph.axis.y.power." + getModeSupplier().get().getGame().toString().toLowerCase(Locale.ROOT)));
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
