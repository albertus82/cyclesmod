package it.albertus.cyclesmod.gui.powergraph.dialog;

import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;

import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import it.albertus.cyclesmod.resources.Messages;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeValueCommand implements IUndoableCommand {

	@NonNull private final IPowerGraph powerGraph;
	private final int index;
	private final short oldValue;
	private final short newValue;

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
