package it.albertus.cyclesmod.gui.powergraph.dialog;

import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;

import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import it.albertus.cyclesmod.resources.Messages;

public class ChangeValueCommand implements IUndoableCommand {

	private final IPowerGraph powerGraph;
	private final int index;
	private final short oldValue;
	private final short newValue;

	public ChangeValueCommand(final IPowerGraph powerGraph, final int index, final short oldValue, final short newValue) {
		this.powerGraph = powerGraph;
		this.index = index;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public void undo() {
		powerGraph.setPowerValue(index, oldValue);
		powerGraph.refresh();
	}

	@Override
	public void redo() {
		powerGraph.setPowerValue(index, newValue);
		powerGraph.refresh();
	}

	@Override
	public String toString() {
		return Messages.get("lbl.graph.action.valueChange");
	}

}
