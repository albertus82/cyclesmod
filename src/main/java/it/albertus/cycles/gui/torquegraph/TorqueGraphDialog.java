package it.albertus.cycles.gui.torquegraph;

import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.visualization.internal.xygraph.undo.OperationsManager;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cycles.gui.Images;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.resources.Messages;
import it.albertus.jface.JFaceMessages;
import it.albertus.jface.SwtUtils;

public class TorqueGraphDialog extends Dialog {

	private int returnCode = SWT.CANCEL;
	private ComplexTorqueGraph torqueGraph;

	public TorqueGraphDialog(final Shell parent) {
		this(parent, SWT.SHEET | SWT.RESIZE | SWT.MAX);
	}

	private TorqueGraphDialog(final Shell parent, final int style) {
		super(parent, style);
		setText(Messages.get("lbl.graph.title"));
	}

	public int open(final Map<Integer, Short> map, final BikeType bikeType) {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText() + " - " + bikeType.getDisplacement() + " cc");
		shell.setImages(Images.getMainIcons());
		createContents(shell, map, bikeType);
		final Point minimumSize = getMinimumSize(shell);
		shell.setSize(getSize(shell));
		shell.setMinimumSize(minimumSize);
		shell.open();
		final Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return returnCode;
	}

	private Point getSize(final Shell shell) {
		final Point normalShellSize = shell.getSize();
		final Point packedShellSize = getMinimumSize(shell);
		return new Point(Math.min(packedShellSize.x * 3, normalShellSize.x), Math.min(packedShellSize.y * 3, normalShellSize.y));
	}

	private Point getMinimumSize(final Shell shell) {
		return shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}

	private void createContents(final Shell shell, final Map<Integer, Short> map, final BikeType bikeType) {
		shell.setLayout(getLayout());
		createGraph(shell, map, bikeType);
		createButtonBox(shell);
	}

	private Layout getLayout() {
		return GridLayoutFactory.swtDefaults().create();
	}

	private void createGraph(final Shell shell, final Map<Integer, Short> map, final BikeType bikeType) {
		final Canvas canvas = new Canvas(shell, SWT.NONE);

		final LightweightSystem lws = new LightweightSystem(canvas);
		torqueGraph = new ComplexTorqueGraph(map, bikeType);
		lws.setContents(torqueGraph.getToolbarArmedXYGraph());

		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);

		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent ke) {
				if (SWT.MOD1 == ke.stateMask) { // CTRL/Cmd
					if (SwtUtils.KEY_UNDO == ke.keyCode) {
						torqueGraph.getXyGraph().getOperationsManager().undo();
					}
					if (SwtUtils.KEY_REDO == ke.keyCode) {
						torqueGraph.getXyGraph().getOperationsManager().redo();
					}
					if (SwtUtils.KEY_SAVE == ke.keyCode) {
						torqueGraph.saveSnapshot();
					}
				}
			}
		});

		canvas.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(final org.eclipse.swt.events.MouseEvent e) {
				final IFigure figureUnderMouse = torqueGraph.getToolbarArmedXYGraph().findFigureAt(e.x, e.y, new TreeSearch() {
					@Override
					public boolean prune(IFigure figure) {
						return false;
					}

					@Override
					public boolean accept(IFigure figure) {
						return figure instanceof Axis || figure instanceof PlotArea;
					}
				});
				if (figureUnderMouse instanceof Axis) {
					final Axis axis = ((Axis) figureUnderMouse);
					final double valuePosition = axis.getPositionValue(axis.isHorizontal() ? e.x : e.y, false);
					axis.zoomInOut(valuePosition, e.count * 0.1 / 3);
				}
				else if (figureUnderMouse instanceof PlotArea) {
					final PlotArea plotArea = (PlotArea) figureUnderMouse;
					plotArea.zoomInOut(true, true, e.x, e.y, e.count * 0.1 / 3);
				}
			}
		});

		new ContextMenu(canvas, torqueGraph);
	}

	private Composite createButtonBox(final Shell shell) {
		final Composite buttonComposite = new Composite(shell, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(buttonComposite);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(buttonComposite);

		final Button okButton = new Button(buttonComposite, SWT.PUSH);
		okButton.setText(JFaceMessages.get("lbl.button.ok"));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).minSize(SwtUtils.convertHorizontalDLUsToPixels(okButton, IDialogConstants.BUTTON_WIDTH), SWT.DEFAULT).applyTo(okButton);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				returnCode = SWT.OK;
				shell.close();
			}
		});

		final Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText(JFaceMessages.get("lbl.button.cancel"));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).minSize(SwtUtils.convertHorizontalDLUsToPixels(cancelButton, IDialogConstants.BUTTON_WIDTH), SWT.DEFAULT).applyTo(cancelButton);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				returnCode = SWT.CANCEL;
				shell.close();
			}
		});

		shell.setDefaultButton(okButton);
		return buttonComposite;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public ITorqueGraph getTorqueGraph() {
		return torqueGraph;
	}

	private static class ContextMenu extends TorqueGraphContextMenu {

		private ContextMenu(final Control control, final ComplexTorqueGraph torqueGraph) {
			super(control, torqueGraph);

			final Menu menu = new Menu(control);
			control.setMenu(menu);

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
					torqueGraph.saveSnapshot();
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

}
