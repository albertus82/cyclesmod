package it.albertus.cycles.gui.torquegraph.dialog;

import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;

import it.albertus.cycles.gui.torquegraph.ITorqueGraph;
import it.albertus.cycles.resources.Messages;

public class PlotCurveCommand implements IUndoableCommand {

	private final ITorqueGraph torqueGraph;
	private final short[] oldValues;
	private final short[] newValues;

	public PlotCurveCommand(final ITorqueGraph torqueGraph, final short[] oldValues, final short[] newValues) {
		this.torqueGraph = torqueGraph;
		this.oldValues = oldValues;
		this.newValues = newValues;
	}

	@Override
	public void undo() {
		for (int i = 0; i < oldValues.length; i++) {
			torqueGraph.getValues()[i] = oldValues[i];
		}
		torqueGraph.refresh();
	}

	@Override
	public void redo() {
		for (int i = 0; i < newValues.length; i++) {
			torqueGraph.getValues()[i] = newValues[i];
		}
		torqueGraph.refresh();
	}

	@Override
	public String toString() {
		return Messages.get("lbl.graph.action.plotCurve");
	}

}
