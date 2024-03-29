package io.github.albertus82.cyclesmod.gui.powergraph.simple;

import java.util.Locale;
import java.util.function.Supplier;

import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.swt.widgets.Display;

import io.github.albertus82.cyclesmod.common.model.Vehicle;
import io.github.albertus82.cyclesmod.common.resources.Messages;
import io.github.albertus82.cyclesmod.gui.Mode;
import io.github.albertus82.cyclesmod.gui.powergraph.PowerGraph;
import io.github.albertus82.cyclesmod.gui.resources.GuiMessages;
import io.github.albertus82.jface.Multilanguage;
import lombok.NonNull;

public class SimplePowerGraph extends PowerGraph implements Multilanguage {

	private static final byte DEFAULT_POINT_SIZE = 4;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	private static final Messages messages = GuiMessages.INSTANCE;

	public SimplePowerGraph(@NonNull final Vehicle vehicle, @NonNull final Supplier<Mode> modeSupplier) {
		super(vehicle, modeSupplier);

		final Axis abscissae = getAbscissae();
		abscissae.setAutoScale(DEFAULT_AUTOSCALE);

		final Axis ordinates = getOrdinates();
		ordinates.setAutoScale(DEFAULT_AUTOSCALE);

		final Trace powerTrace = getPowerTrace();
		powerTrace.setPointStyle(PointStyle.FILLED_DIAMOND);
		powerTrace.setLineWidth(DEFAULT_LINE_WIDTH);
		powerTrace.setPointSize(DEFAULT_POINT_SIZE);

		final Trace torqueTrace = getTorqueTrace();
		torqueTrace.setLineWidth(DEFAULT_LINE_WIDTH);

		final IXYGraph xyGraph = getXyGraph();
		xyGraph.setTitle(messages.get("gui.label.graph.title.power.torque"));
		xyGraph.setTitleFont(Display.getCurrent().getSystemFont());
	}

	@Override
	public void toggleTorqueVisibility(final boolean visibility) {
		super.toggleTorqueVisibility(visibility);
		if (visibility) {
			getXyGraph().setTitle(messages.get("gui.label.graph.title.power.torque"));
		}
		else {
			getXyGraph().setTitle(messages.get("gui.label.graph.title.power"));
		}
	}

	@Override
	public void updateLanguage() {
		getAbscissae().setTitle(messages.get("gui.label.graph.axis.x", RPM_DIVISOR));
		if (isTorqueVisible()) {
			getXyGraph().setTitle(messages.get("gui.label.graph.title.power.torque"));
		}
		else {
			getXyGraph().setTitle(messages.get("gui.label.graph.title.power"));
		}
		setOrdinatesTitle();
	}

	public void updateModeSpecificWidgets() {
		setOrdinatesTitle();
	}

	private void setOrdinatesTitle() {
		if (isTorqueVisible()) {
			getOrdinates().setTitle(messages.get("gui.label.graph.axis.y.power.torque." + getModeSupplier().get().getGame().toString().toLowerCase(Locale.ROOT)));
		}
		else {
			getOrdinates().setTitle(messages.get("gui.label.graph.axis.y.power." + getModeSupplier().get().getGame().toString().toLowerCase(Locale.ROOT)));
		}
	}

}
