package it.albertus.cycles.gui;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.visualization.internal.xygraph.toolbar.GrayableButton;
import org.eclipse.nebula.visualization.internal.xygraph.undo.IOperationsManagerListener;
import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.OperationsManager;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cycles.gui.TorqueGraph.TorqueGraphContextMenu;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;
import it.albertus.jface.JFaceMessages;
import it.albertus.jface.SwtUtils;
import it.albertus.util.logging.LoggerFactory;

public class TorqueGraphDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(TorqueGraphDialog.class);

	private static final boolean DEFAULT_AUTOSCALE = false;

	private int returnCode = SWT.CANCEL;
	private ComplexTorqueGraph torqueGraph;

	private static class ComplexTorqueGraph extends TorqueGraph {

		private static final String MSG_KEY_LBL_GRAPH_TOOLBAR_REDO = "lbl.graph.toolbar.redo";
		private static final String MSG_KEY_LBL_GRAPH_TOOLBAR_UNDO = "lbl.graph.toolbar.undo";

		private static final byte DEFAULT_POINT_SIZE = 6;
		private static final byte DEFAULT_LINE_WIDTH = 2;

		private final ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(getXyGraph());

		private ComplexTorqueGraph(final Map<Integer, Short> map, final BikeType bikeType) {
			super(map, bikeType);

			final Axis abscissae = getAbscissae();
			abscissae.setAutoScale(DEFAULT_AUTOSCALE);

			final Axis ordinates = getOrdinates();
			ordinates.setAutoScale(DEFAULT_AUTOSCALE);

			final Trace trace = getTrace();
			trace.setPointStyle(PointStyle.FILLED_DIAMOND);
			trace.setLineWidth(DEFAULT_LINE_WIDTH);
			trace.setPointSize(DEFAULT_POINT_SIZE);

			fixUndoRedoButtons();

			add(toolbarArmedXYGraph);

			final IXYGraph xyGraph = getXyGraph();
			xyGraph.getPlotArea().addMouseListener(new MouseListener.Stub() {
				@Override
				public void mousePressed(final org.eclipse.draw2d.MouseEvent me) {
					if (me.button == 1) { // left click
						final double rpm = abscissae.getPositionValue(me.getLocation().x, false) * 1000;
						final int index = Math.max(Math.min(Torque.indexOf(rpm), Torque.LENGTH - 1), 0);
						final double[] values = getValues();
						final short oldValue = (short) values[index];
						final short newValue = (short) Math.round(Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, ordinates.getPositionValue(me.getLocation().y, false))));
						if (oldValue != newValue) {
							values[index] = newValue;
							refresh();
							xyGraph.getOperationsManager().addCommand(new IUndoableCommand() {
								@Override
								public void undo() {
									values[index] = oldValue;
									refresh();
								}

								@Override
								public void redo() {
									values[index] = newValue;
									refresh();
								}

								@Override
								public String toString() {
									return Messages.get("lbl.graph.action.valueChange");
								};
							});
						}
					}
				}
			});
			xyGraph.setShowTitle(false);

			abscissae.performAutoScale(true);
			ordinates.performAutoScale(true);
		}

		@SuppressWarnings("unchecked")
		protected void fixUndoRedoButtons() {
			try {
				final Field listenersField = OperationsManager.class.getDeclaredField("listeners");
				listenersField.setAccessible(true);
				for (final IOperationsManagerListener listener : (Collection<IOperationsManagerListener>) listenersField.get(getXyGraph().getOperationsManager())) {
					toolbarArmedXYGraph.getIXYGraph().getOperationsManager().removeListener(listener);
				}

				for (final Object o : toolbarArmedXYGraph.getToolbar().getChildren()) {
					if (o instanceof GrayableButton) {
						final GrayableButton button = (GrayableButton) o;
						if (button.getToolTip() instanceof Label) {
							final String labelText = ((Label) button.getToolTip()).getText();
							if ("undo".equalsIgnoreCase(labelText)) {
								button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_UNDO, "")));
								addUndoListener(button, toolbarArmedXYGraph.getIXYGraph().getOperationsManager());
							}
							else if ("redo".equalsIgnoreCase(labelText)) {
								button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_REDO, "")));
								addRedoListener(button, toolbarArmedXYGraph.getIXYGraph().getOperationsManager());
							}
						}
					}
				}
			}
			catch (final Exception e) {
				logger.log(Level.WARNING, e.toString(), e);
			}
		}

		private static void addUndoListener(final GrayableButton button, final OperationsManager manager) {
			manager.addListener(new IOperationsManagerListener() {
				@Override
				public void operationsHistoryChanged(final OperationsManager manager) {
					if (manager.getUndoCommandsSize() > 0) {
						button.setEnabled(true);
						final String cmdName = manager.getUndoCommands()[manager.getUndoCommandsSize() - 1].toString();
						button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_UNDO, cmdName)));
					}
					else {
						button.setEnabled(false);
						button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_UNDO, "")));
					}
				}
			});
		}

		private static void addRedoListener(final GrayableButton button, final OperationsManager manager) {
			manager.addListener(new IOperationsManagerListener() {
				@Override
				public void operationsHistoryChanged(final OperationsManager manager) {
					if (manager.getRedoCommandsSize() > 0) {
						button.setEnabled(true);
						final String cmdName = manager.getRedoCommands()[manager.getRedoCommandsSize() - 1].toString();
						button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_REDO, cmdName)));
					}
					else {
						button.setEnabled(false);
						button.setToolTip(new Label(Messages.get(MSG_KEY_LBL_GRAPH_TOOLBAR_REDO, "")));
					}
				}
			});
		}

		public ToolbarArmedXYGraph getToolbarArmedXYGraph() {
			return toolbarArmedXYGraph;
		}

		@Override
		protected void layout() {
			toolbarArmedXYGraph.setBounds(getBounds().getCopy());
			super.layout();
		}
	}

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
		shell.setImages(Images.MAIN_ICONS);
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
		lws.setContents(torqueGraph);

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
						saveSnapshot();
					}
				}
			}
		});

		canvas.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(final MouseEvent e) {
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

		new ContextMenu(canvas);
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
			public void widgetSelected(final SelectionEvent event) {
				try {
					returnCode = SWT.OK;
				}
				catch (final SWTException se) {
					logger.log(Level.FINE, se.toString(), se);
				}
				catch (final Exception e) {
					logger.log(Level.SEVERE, e.toString(), e);
				}
				finally {
					shell.close();
				}
			}
		});

		final Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText(JFaceMessages.get("lbl.button.cancel"));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).minSize(SwtUtils.convertHorizontalDLUsToPixels(cancelButton, IDialogConstants.BUTTON_WIDTH), SWT.DEFAULT).applyTo(cancelButton);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent se) {
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

	private void saveSnapshot() {
		final ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { torqueGraph.getXyGraph().getImage().getImageData() };
		final FileDialog dialog = new FileDialog(Display.getDefault().getShells()[0], SWT.SAVE);
		dialog.setFilterNames(new String[] { "Portable Network Graphics (*.png)", Messages.get("lbl.graph.save.allFiles", "(*.*)") });
		dialog.setFilterExtensions(new String[] { "*.PNG;*.png", "*.*" }); // Windows
		final String path = dialog.open();
		if ((path != null) && !path.isEmpty()) {
			loader.save(path, SWT.IMAGE_PNG);
		}
	}

	private class ContextMenu extends TorqueGraphContextMenu {

		private ContextMenu(final Control control) {
			torqueGraph.super();
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
					saveSnapshot();
				}
			});

			new MenuItem(menu, SWT.SEPARATOR);

			final MenuItem autoScaleMenuItem = new MenuItem(menu, SWT.CHECK);
			autoScaleMenuItem.setSelection(DEFAULT_AUTOSCALE);
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

			addTraceTypeSubMenu(control);
			addLineWidthSubMenu(control);
			addPointStyleSubMenu(control);
			addPointSizeSubMenu(control);

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
