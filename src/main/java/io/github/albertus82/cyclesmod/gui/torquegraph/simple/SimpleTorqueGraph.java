package io.github.albertus82.cyclesmod.gui.torquegraph.simple;

import java.util.function.Supplier;

import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.swt.widgets.Display;

import io.github.albertus82.cyclesmod.common.model.Vehicle;
import io.github.albertus82.cyclesmod.common.resources.Messages;
import io.github.albertus82.cyclesmod.gui.Mode;
import io.github.albertus82.cyclesmod.gui.resources.GuiMessages;
import io.github.albertus82.cyclesmod.gui.torquegraph.BasicTorqueGraph;
import io.github.albertus82.jface.Multilanguage;
import lombok.NonNull;

public class SimpleTorqueGraph extends BasicTorqueGraph implements Multilanguage {

	private static final byte DEFAULT_POINT_SIZE = 4;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	private static final Messages messages = GuiMessages.INSTANCE;

	public SimpleTorqueGraph(@NonNull final Vehicle vehicle, @NonNull final Supplier<Mode> modeSupplier) {
		super(vehicle, modeSupplier);

		final IXYGraph xyGraph = getXyGraph();
		
		final Axis abscissae = xyGraph.getPrimaryXAxis();
		abscissae.setAutoScale(DEFAULT_AUTOSCALE);

		final Axis ordinates = xyGraph.getPrimaryYAxis();
		ordinates.setAutoScale(DEFAULT_AUTOSCALE);

		//		final Axis powerOrdinates = getPowerOrdinates();
		//		powerOrdinates.setAutoScale(DEFAULT_AUTOSCALE);

		final Trace torqueTrace = getTorqueTrace();
		torqueTrace.setPointStyle(PointStyle.FILLED_DIAMOND);
		torqueTrace.setLineWidth(DEFAULT_LINE_WIDTH);
		torqueTrace.setPointSize(DEFAULT_POINT_SIZE);

		final Trace powerTrace = getPowerTrace();
		powerTrace.setLineWidth(DEFAULT_LINE_WIDTH);

		
		xyGraph.setTitle(messages.get("gui.label.graph.title.torque.power"));
		xyGraph.setTitleFont(Display.getCurrent().getSystemFont());
	}

	public void updateModeSpecificWidgets() {
		setOrdinatesTitle();
	}
	
	@Override
	public boolean togglePowerVisibility() {
		final boolean visible = super.togglePowerVisibility();
		setGraphTitle();
		return visible;
	}

	
	@Override
	public void updateLanguage() {
		getXyGraph().getPrimaryXAxis().setTitle(messages.get("gui.label.graph.axis.x", RPM_DIVISOR));
//		getAbscissae() abscissae.setTitle(messages.get("gui.label.graph.axis.x", RPM_DIVISOR));
		setGraphTitle();
		setOrdinatesTitle();
	}
	
	private void setGraphTitle() {
		getXyGraph().setTitle(messages.get(isPowerVisible() ? "gui.label.graph.title.torque.power" : "gui.label.graph.title.torque"));
	}

}
