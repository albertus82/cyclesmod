package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Text;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LabelMouseListener extends MouseAdapter {

	@NonNull private final Text text;

	@Override
	public void mouseDown(final MouseEvent e) {
		text.setFocus();
	}

}
