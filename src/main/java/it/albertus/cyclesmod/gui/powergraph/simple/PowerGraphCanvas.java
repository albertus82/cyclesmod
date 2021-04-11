package it.albertus.cyclesmod.gui.powergraph.simple;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import it.albertus.cyclesmod.common.model.Bike;
import it.albertus.cyclesmod.gui.powergraph.PowerGraphProvider;

public class PowerGraphCanvas extends Canvas implements PowerGraphProvider {

	private final SimplePowerGraphContextMenu contextMenu;
	private final SimplePowerGraph powerGraph;

	public PowerGraphCanvas(final Composite parent, final Bike bike) {
		super(parent, SWT.NONE);

		final LightweightSystem lws = new LightweightSystem(this);
		powerGraph = new SimplePowerGraph(bike);
		lws.setContents(powerGraph.getXyGraph());

		setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		contextMenu = new SimplePowerGraphContextMenu(this, powerGraph);
	}

	public void updateTexts() {
		powerGraph.updateTexts();
		contextMenu.updateTexts();
	}

	@Override
	public SimplePowerGraph getPowerGraph() {
		return powerGraph;
	}

}
