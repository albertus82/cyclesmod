package io.github.albertus82.cyclesmod.gui.powergraph;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;

public interface IPowerGraph {

	IXYGraph getXyGraph();

	Axis getAbscissae();

	Axis getOrdinates();

	IDataProvider getDataProvider();

	Trace getPowerTrace();

	Trace getTorqueTrace();

	double getPowerValue(int index);

	void setPowerValue(int index, double value);

	void refresh();

	short getPowerValue(Point location);

	int getPowerIndex(Point location);

	void toggleTorqueVisibility(boolean visibility);

	boolean isTorqueVisible();

}
