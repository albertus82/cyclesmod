package it.albertus.cycles.gui;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;

public class TorqueGraph {

	private Trace trace;
	private double[] values;

	public TorqueGraph(Trace trace, double[] values) {
		this.trace = trace;
		this.values = values;
	}

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public double[] getValues() {
		return values;
	}

	public void setValues(double[] values) {
		this.values = values;
	}

	public boolean refresh() {
		boolean success = false;
		IDataProvider dataProvider = this.trace.getDataProvider();
		if (dataProvider instanceof CircularBufferDataProvider) {
			((CircularBufferDataProvider) dataProvider).triggerUpdate();
			success = true;
		}
		return success;
	}

}