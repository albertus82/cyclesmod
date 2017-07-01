package it.albertus.cycles.gui.torquegraph.simple;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import it.albertus.cycles.gui.torquegraph.TorqueGraphProvider;
import it.albertus.cycles.model.Bike;

public class TorqueGraphCanvas extends Canvas implements TorqueGraphProvider {

	private final SimpleTorqueGraphContextMenu contextMenu;
	private final SimpleTorqueGraph torqueGraph;

	public TorqueGraphCanvas(final Composite parent, final Bike bike) {
		super(parent, SWT.NONE);

		final LightweightSystem lws = new LightweightSystem(this);
		torqueGraph = new SimpleTorqueGraph(bike);
		lws.setContents(torqueGraph.getXyGraph());

		setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		contextMenu = new SimpleTorqueGraphContextMenu(this, torqueGraph);
	}

	public void updateTexts() {
		torqueGraph.updateTexts();
		contextMenu.updateTexts();
	}

	@Override
	public SimpleTorqueGraph getTorqueGraph() {
		return torqueGraph;
	}

}
