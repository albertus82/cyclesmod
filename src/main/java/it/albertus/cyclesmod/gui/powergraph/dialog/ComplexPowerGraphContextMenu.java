package it.albertus.cyclesmod.gui.powergraph.dialog;

import org.eclipse.nebula.visualization.internal.xygraph.undo.OperationsManager;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import it.albertus.cyclesmod.gui.powergraph.PowerGraphContextMenu;
import it.albertus.cyclesmod.gui.powergraph.dialog.listener.RedoListener;
import it.albertus.cyclesmod.gui.powergraph.dialog.listener.SaveSnapshotListener;
import it.albertus.cyclesmod.gui.powergraph.dialog.listener.UndoListener;
import it.albertus.cyclesmod.gui.powergraph.dialog.listener.ZoomInListener;
import it.albertus.cyclesmod.gui.powergraph.dialog.listener.ZoomOutListener;
import it.albertus.jface.SwtUtils;

public class ComplexPowerGraphContextMenu extends PowerGraphContextMenu {

	private final MenuItem showTorqueMenuItem;

	public ComplexPowerGraphContextMenu(final Control control, final IPowerGraph powerGraph) {
		super(control, powerGraph);

		final Menu menu = getMenu();

		final XYGraphMediaFactory mediaFactory = XYGraphMediaFactory.getInstance();

		final OperationsManager manager = powerGraph.getXyGraph().getOperationsManager();

		final MenuItem undoMenuItem = new MenuItem(menu, SWT.PUSH);
		undoMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_UNDO);
		undoMenuItem.addSelectionListener(new UndoListener(manager));

		final MenuItem redoMenuItem = new MenuItem(menu, SWT.PUSH);
		redoMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_REDO);
		redoMenuItem.addSelectionListener(new RedoListener(manager));

		new MenuItem(menu, SWT.SEPARATOR);

		final MenuItem saveImageMenuItem = new MenuItem(menu, SWT.PUSH);
		saveImageMenuItem.setImage(mediaFactory.getImage("images/camera.png"));
		saveImageMenuItem.setText(Messages.get("lbl.menu.item.graph.saveImageAs") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		saveImageMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SAVE);
		saveImageMenuItem.addSelectionListener(new SaveSnapshotListener(control.getShell(), powerGraph.getXyGraph()));

		new MenuItem(menu, SWT.SEPARATOR);

		final MenuItem autoScaleMenuItem = new MenuItem(menu, SWT.CHECK);
		autoScaleMenuItem.setText(Messages.get("lbl.menu.item.graph.autoscaling"));
		autoScaleMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (autoScaleMenuItem.getSelection()) {
					powerGraph.getAbscissae().setAutoScale(true);
					powerGraph.getOrdinates().setAutoScale(true);
				}
				else {
					powerGraph.getAbscissae().setAutoScale(false);
					powerGraph.getOrdinates().setAutoScale(false);
				}
			}
		});

		final MenuItem zoomInMenuItem = new MenuItem(menu, SWT.PUSH);
		zoomInMenuItem.setText(Messages.get("lbl.menu.item.graph.zoomIn") + SwtUtils.getShortcutLabel("+"));
		zoomInMenuItem.setAccelerator('+');
		zoomInMenuItem.setImage(ZoomType.ZOOM_IN.getIconImage());
		zoomInMenuItem.addSelectionListener(new ZoomInListener(powerGraph.getXyGraph()));

		final MenuItem zoomOutMenuItem = new MenuItem(menu, SWT.PUSH);
		zoomOutMenuItem.setText(Messages.get("lbl.menu.item.graph.zoomOut") + SwtUtils.getShortcutLabel("-"));
		zoomOutMenuItem.setAccelerator('-');
		zoomOutMenuItem.setImage(ZoomType.ZOOM_OUT.getIconImage());
		zoomOutMenuItem.addSelectionListener(new ZoomOutListener(powerGraph.getXyGraph()));

		final MenuItem performAutoScaleMenuItem = new MenuItem(menu, SWT.PUSH);
		performAutoScaleMenuItem.setText(Messages.get("lbl.menu.item.graph.autoscaleNow"));
		performAutoScaleMenuItem.setImage(mediaFactory.getImage("images/AutoScale.png"));
		performAutoScaleMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				powerGraph.getXyGraph().performAutoScale();
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);

		addTraceTypeSubMenu();
		addLineWidthSubMenu();
		addPointStyleSubMenu();
		addPointSizeSubMenu();

		new MenuItem(menu, SWT.SEPARATOR);

		showTorqueMenuItem = addShowTorqueMenuItem();

		final Image imageUndo = mediaFactory.getImage("images/Undo.png");
		final Image imageUndoGray = mediaFactory.getImage("images/Undo_Gray.png");
		final Image imageRedo = mediaFactory.getImage("images/Redo.png");
		final Image imageRedoGray = mediaFactory.getImage("images/Redo_Gray.png");

		control.addMenuDetectListener(e -> {
			autoScaleMenuItem.setSelection(powerGraph.getAbscissae().isAutoScale() && powerGraph.getOrdinates().isAutoScale());

			// Undo/Redo
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
		});
	}

	public MenuItem getShowTorqueMenuItem() {
		return showTorqueMenuItem;
	}

}
