package it.albertus.cycles.gui;

import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;

public interface ITorqueGraph {

	IXYGraph getXyGraph();

	Axis getAbscissae();

	Axis getOrdinates();

	IDataProvider getDataProvider();

	Trace getTrace();

	double[] getValues();

	void refresh();

}
