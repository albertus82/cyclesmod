package com.github.albertus82.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.github.albertus82.cyclesmod.gui.CyclesModGui;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExitListener extends ShellAdapter implements SelectionListener, Listener {

	@NonNull
	private final CyclesModGui gui;

	private boolean canClose() {
		return gui.askForSavingAndExport();
	}

	private void disposeAll() {
		gui.getShell().dispose();
		final Display display = Display.getCurrent();
		if (display != null) {
			display.dispose(); // Fix close not working on Windows 10 when iconified
		}
	}

	/* macOS Menu */
	@Override
	public void handleEvent(final Event event) {
		if (canClose()) {
			disposeAll();
		}
		else if (event != null) {
			event.doit = false;
		}
	}

	/* Menu */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (canClose()) {
			disposeAll();
		}
		else if (event != null) {
			event.doit = false;
		}
	}

	/* Shell close command */
	@Override
	public void shellClosed(final ShellEvent event) {
		if (canClose()) {
			disposeAll();
		}
		else if (event != null) {
			event.doit = false;
		}
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

}
