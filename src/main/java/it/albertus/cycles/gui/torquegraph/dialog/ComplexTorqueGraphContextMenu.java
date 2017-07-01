package it.albertus.cycles.gui.torquegraph.dialog;

import org.eclipse.nebula.visualization.internal.xygraph.undo.OperationsManager;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cycles.gui.torquegraph.TorqueGraphContextMenu;
import it.albertus.cycles.resources.Messages;
import it.albertus.jface.SwtUtils;

public class ComplexTorqueGraphContextMenu extends TorqueGraphContextMenu {

	public ComplexTorqueGraphContextMenu(final Control control, final ComplexTorqueGraph torqueGraph) {
		super(control, torqueGraph);

		final Menu menu = getMenu();

		final XYGraphMediaFactory mediaFactory = XYGraphMediaFactory.getInstance();

		final MenuItem undoMenuItem = new MenuItem(menu, SWT.PUSH);
		undoMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				torqueGraph.getXyGraph().getOperationsManager().undo();
			}
		});

		final MenuItem redoMenuItem = new MenuItem(menu, SWT.PUSH);
		redoMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				torqueGraph.getXyGraph().getOperationsManager().redo();
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);

		final MenuItem saveImageMenuItem = new MenuItem(menu, SWT.PUSH);
		saveImageMenuItem.setImage(mediaFactory.getImage("images/camera.png"));
		saveImageMenuItem.setText(Messages.get("lbl.menu.item.graph.saveImageAs") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		saveImageMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				torqueGraph.saveSnapshot(control.getShell());
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);

		final MenuItem autoScaleMenuItem = new MenuItem(menu, SWT.CHECK);
		autoScaleMenuItem.setText(Messages.get("lbl.menu.item.graph.autoscaling"));
		autoScaleMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (autoScaleMenuItem.getSelection()) {
					torqueGraph.getAbscissae().setAutoScale(true);
					torqueGraph.getOrdinates().setAutoScale(true);
				}
				else {
					torqueGraph.getAbscissae().setAutoScale(false);
					torqueGraph.getOrdinates().setAutoScale(false);
				}
			}
		});

		final MenuItem performAutoScaleMenuItem = new MenuItem(menu, SWT.PUSH);
		performAutoScaleMenuItem.setText(Messages.get("lbl.menu.item.graph.autoscaleNow"));
		performAutoScaleMenuItem.setImage(mediaFactory.getImage("images/AutoScale.png"));
		performAutoScaleMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				torqueGraph.getXyGraph().performAutoScale();
			}
		});

		addTraceTypeSubMenu();
		addLineWidthSubMenu();
		addPointStyleSubMenu();
		addPointSizeSubMenu();

		final Image imageUndo = mediaFactory.getImage("images/Undo.png");
		final Image imageUndoGray = mediaFactory.getImage("images/Undo_Gray.png");
		final Image imageRedo = mediaFactory.getImage("images/Redo.png");
		final Image imageRedoGray = mediaFactory.getImage("images/Redo_Gray.png");

		control.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(final MenuDetectEvent e) {
				autoScaleMenuItem.setSelection(torqueGraph.getAbscissae().isAutoScale() && torqueGraph.getOrdinates().isAutoScale());

				// Undo/Redo
				final OperationsManager manager = torqueGraph.getXyGraph().getOperationsManager();
				if (manager.getUndoCommandsSize() > 0) {
					undoMenuItem.setEnabled(true);
					undoMenuItem.setImage(imageUndo);
					final String cmdName = manager.getUndoCommands()[manager.getUndoCommandsSize() - 1].toString();
					undoMenuItem.setText(Messages.get("lbl.menu.item.graph.undo", cmdName) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_UNDO));
				}
				else {
					undoMenuItem.setEnabled(false);
					undoMenuItem.setImage(imageUndoGray);
					undoMenuItem.setText(Messages.get("lbl.menu.item.graph.undo", "") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_UNDO));
				}
				if (manager.getRedoCommandsSize() > 0) {
					redoMenuItem.setEnabled(true);
					redoMenuItem.setImage(imageRedo);
					final String cmdName = manager.getRedoCommands()[manager.getRedoCommandsSize() - 1].toString();
					redoMenuItem.setText(Messages.get("lbl.menu.item.graph.redo", cmdName) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_REDO));
				}
				else {
					redoMenuItem.setEnabled(false);
					redoMenuItem.setImage(imageRedoGray);
					redoMenuItem.setText(Messages.get("lbl.menu.item.graph.redo", "") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_REDO));
				}

				menu.setVisible(true);
			}
		});
	}

}
