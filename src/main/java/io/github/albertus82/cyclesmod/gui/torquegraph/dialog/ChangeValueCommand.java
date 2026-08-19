package io.github.albertus82.cyclesmod.gui.torquegraph.dialog;

import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;

import io.github.albertus82.cyclesmod.common.resources.Messages;
import io.github.albertus82.cyclesmod.gui.resources.GuiMessages;
import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraph;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeValueCommand implements IUndoableCommand {

	private static final Messages messages = GuiMessages.INSTANCE;

	@NonNull
	private final TorqueGraph powerGraph;
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
		return messages.get("gui.label.graph.action.valueChange");
	}

}
