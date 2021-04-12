package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;

import lombok.NonNull;

public class LinkSelectionListener implements SelectionListener {

	@Override
	public void widgetSelected(@NonNull final SelectionEvent event) {
		Program.launch(event.text);
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {
		widgetSelected(event);
	}

}
