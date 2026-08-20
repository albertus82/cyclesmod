package io.github.albertus82.cyclesmod.gui.torquegraph;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
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

	public static final short RPM_DIVISOR = 1000;
	private static final short MAX_RPM = Torque.BASE_RPM + Torque.POINT_WIDTH_RPM * (Torque.LENGTH - 1);

	private static final Messages messages = GuiMessages.INSTANCE;

	//	private static final IDataProvider nullDataProvider = new NullDataProvider();

	@Getter
	private final IXYGraph xyGraph = new XYGraph();
	private final Axis abscissae = xyGraph.getPrimaryXAxis();
	private final Axis ordinates = xyGraph.getPrimaryYAxis();
	//	@Getter
	//	private final Axis powerOrdinates = new Axis("secondary", true);
	//	{
	//		powerOrdinates.setOrientation(Orientation.VERTICAL);
	//		powerOrdinates.setTickLabelSide(LabelSide.Secondary);
	//		xyGraph.addAxis(powerOrdinates);
	//	}
	private final CircularBufferDataProvider torqueDataProvider = new CircularBufferDataProvider(false);
	private final CircularBufferDataProvider powerDataProvider = new CircularBufferDataProvider(false);
	@Getter
	private final Trace torqueTrace = new Trace(messages.get("gui.label.graph.trace.torque"), abscissae, ordinates, torqueDataProvider);
	@Getter
	private final Trace powerTrace = new Trace(messages.get("gui.label.graph.trace.power"), abscissae, /* powerOrdinates */ordinates, powerDataProvider/* nullDataProvider */);
	private final double[] torqueValues = new double[Torque.LENGTH];
	private final double[] powerValues = new double[Torque.LENGTH];
	private final double[] xDataArray = new double[Torque.LENGTH];
	@Getter
	private final Supplier<Mode> modeSupplier;
	//	@Getter
	//	private boolean powerVisible = true;

	public BasicTorqueGraph(@NonNull final Vehicle vehicle, @NonNull final Supplier<Mode> modeSupplier) {
		this.modeSupplier = modeSupplier;
		for (int i = 0; i < Torque.LENGTH; i++) {
			xDataArray[i] = (double) Torque.getRpm(i) / RPM_DIVISOR;
			torqueValues[i] = vehicle.getTorque().getCurve()[i];
			powerValues[i] = torqueToPower(torqueValues[i], Torque.getRpm(i));
		}
		init(vehicle.getType());
	}

	public BasicTorqueGraph(@NonNull final Map<Integer, Short> torqueMap, @NonNull final VehicleType vehicleType, @NonNull final Supplier<Mode> modeSupplier) {
		this.modeSupplier = modeSupplier;
		if (torqueMap.size() != Torque.LENGTH) {
			throw new IllegalArgumentException("map size must be " + Torque.LENGTH);
		}

		int i = 0;
		for (final Entry<Integer, Short> entry : torqueMap.entrySet()) {
			xDataArray[i] = entry.getKey().doubleValue() / RPM_DIVISOR;
			torqueValues[i] = entry.getValue();
			powerValues[i] = torqueToPower(torqueValues[i], Torque.getRpm(i));
			i++;
		}
		init(vehicleType);
	}

	protected void init(@NonNull final VehicleType vehicleType) {
		torqueDataProvider.setBufferSize(xDataArray.length);
		torqueDataProvider.setCurrentXDataArray(xDataArray);
		torqueDataProvider.setCurrentYDataArray(torqueValues);
		powerDataProvider.setBufferSize(xDataArray.length);
		powerDataProvider.setCurrentXDataArray(xDataArray);
		powerDataProvider.setCurrentYDataArray(powerValues);

		final Font axisTitleFont = Display.getCurrent().getSystemFont();

		abscissae.setTitle(messages.get("gui.label.graph.axis.x", RPM_DIVISOR));
		abscissae.setTitleFont(axisTitleFont);
		abscissae.setShowMajorGrid(true);

		//		ordinates.setTitle(messages.get("gui.label.graph.axis.y.torque"));
		ordinates.setTitleFont(axisTitleFont);
		ordinates.setShowMajorGrid(true);

		//		powerOrdinates.setTitle(messages.get("gui.label.graph.axis.y.power"));
		//		powerOrdinates.setTitleFont(axisTitleFont);
		//		powerOrdinates.setShowMajorGrid(false);
		//		powerOrdinates.setPrimarySide(false);
		//		powerOrdinates.setAutoScale(true);

		xyGraph.addTrace(torqueTrace);
		xyGraph.addTrace(powerTrace);
		//		togglePowerVisibility(false);
		//togglePowerVisibility(true);
		xyGraph.setShowLegend(false);

		torqueTrace.setTraceColor(getColor(vehicleType));
		powerTrace.setTraceColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));

		setOrdinatesTitle();
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

	@Override
	public void refresh() {
		torqueDataProvider.triggerUpdate();
		powerDataProvider.triggerUpdate();
	}

	@Override
	public CircularBufferDataProvider getDataProvider() {
		return torqueDataProvider;
	}

	@Override
	public double getTorqueValue(final int index) {
		return torqueValues[index];
	}

	@Override
	public void setTorqueValue(final int index, final int value) {
		torqueValues[index] = value;
		powerValues[index] = torqueToPower(value, Torque.getRpm(index));
	}

	@Override
	public short getTorqueValue(@NonNull final Point location) {
		return (short) Math.round(Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, ordinates.getPositionValue(location.y, false))));
	}

	@Override
	public int getTorqueIndex(@NonNull final Point location) {
		return Math.max(Math.min(Torque.indexOf(abscissae.getPositionValue(location.x, false) * RPM_DIVISOR), Torque.LENGTH - 1), 0);
	}

	@Override
	public boolean togglePowerVisibility() {
		powerTrace.setVisible(!isPowerVisible());
		setOrdinatesTitle();
		return isPowerVisible();
	}

	@Override
	public boolean isPowerVisible() {
		return powerTrace.isVisible();
	}

	protected void setOrdinatesTitle() {
		ordinates.setTitle(messages.get(isPowerVisible() ? "gui.label.graph.axis.y.torque.power" : "gui.label.graph.axis.y.torque"));
	}

	public static double torqueToPower(final double torque, final int rpm) { // [0.0, 255.0]
		return (torque / MAX_RPM * rpm);
	}

	//	private static class NullDataProvider extends AbstractDataProvider {
	//
	//		public NullDataProvider() {
	//			super(false);
	//		}
	//
	//		@Override
	//		public int getSize() {
	//			return 0;
	//		}
	//
	//		@Override
	//		public ISample getSample(int index) {
	//			return null;
	//		}
	//	}

}
