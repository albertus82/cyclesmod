package com.github.albertus82.cyclesmod.gui.powergraph.dialog.listener;

import org.eclipse.nebula.visualization.internal.xygraph.undo.OperationsManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.jface.SwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedoListener extends SelectionAdapter implements KeyListener {

	private final OperationsManager manager;

	@Override
	public void keyPressed(@NonNull final KeyEvent e) {
		if (SWT.MOD1 == e.stateMask && SwtUtils.KEY_REDO == e.keyCode) {
			manager.redo();
		}
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		manager.redo();
	}

	@Override
	public void keyReleased(final KeyEvent e) {/* Ignore */}

}
