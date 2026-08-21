package io.github.albertus82.cyclesmod.gui.torquegraph;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;

public interface TorqueGraph {

	IXYGraph getXyGraph();

	IDataProvider getDataProvider();

	Trace getPowerTrace();

	Trace getTorqueTrace();

	double getTorqueValue(int index);

	void setTorqueValue(int index, int value);

	void refresh();

	short getTorqueValue(Point location);

	int getTorqueIndex(Point location);

	boolean togglePowerVisibility();

	boolean isPowerVisible();

}
