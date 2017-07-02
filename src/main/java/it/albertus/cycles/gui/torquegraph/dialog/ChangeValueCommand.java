package it.albertus.cycles.gui.torquegraph.dialog;

import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;

import it.albertus.cycles.gui.torquegraph.ITorqueGraph;
import it.albertus.cycles.resources.Messages;

public class ChangeValueCommand implements IUndoableCommand {

	private final ITorqueGraph torqueGraph;
	private final int index;
	private final short oldValue;
	private final short newValue;

	public ChangeValueCommand(final ITorqueGraph torqueGraph, final int index, final short oldValue, final short newValue) {
		this.torqueGraph = torqueGraph;
		this.index = index;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public void undo() {
		torqueGraph.getValues()[index] = oldValue;
		torqueGraph.refresh();
	}

	@Override
	public void redo() {
		torqueGraph.getValues()[index] = newValue;
		torqueGraph.refresh();
	}

	@Override
	public String toString() {
		return Messages.get("lbl.graph.action.valueChange");
	}

}
