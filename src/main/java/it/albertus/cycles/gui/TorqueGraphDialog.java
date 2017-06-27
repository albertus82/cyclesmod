package it.albertus.cycles.gui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.visualization.internal.xygraph.undo.IUndoableCommand;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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

import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;
import it.albertus.jface.JFaceMessages;
import it.albertus.jface.SwtUtils;
import it.albertus.util.logging.LoggerFactory;

public class TorqueGraphDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(TorqueGraphDialog.class);

	private static final byte[] POINT_SIZE_OPTIONS = { 0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
	private static final byte[] LINE_WIDTH_OPTIONS = { 1, 2, 3, 4, 5, 6 };

	private static final byte DEFAULT_POINT_SIZE = 6;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = false;

	private int returnCode = SWT.CANCEL;
	private Canvas canvas;
	private FullFeaturedTorqueGraph torqueGraph;
	private ContextMenu contextMenu;

	class FullFeaturedTorqueGraph extends Figure implements ITorqueGraph {

		public static final String FONT_KEY_AXIS_TITLE = "axisTitle";

		private final IXYGraph xyGraph = new XYGraph();
		private final ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(xyGraph);
		private final Axis abscissae = xyGraph.getPrimaryXAxis();
		private final Axis ordinates = xyGraph.getPrimaryYAxis();
		private final CircularBufferDataProvider dataProvider = new CircularBufferDataProvider(false);
		private final Trace trace = new Trace(Messages.get("lbl.graph.title"), abscissae, ordinates, dataProvider);
		private final double[] values = new double[Torque.LENGTH];

		private FullFeaturedTorqueGraph(final Map<Double, Double> valueMap, final Color traceColor) {
			if (valueMap.size() != Torque.LENGTH) {
				throw new IllegalArgumentException("values size must be " + Torque.LENGTH);
			}

			final double[] x = new double[Torque.LENGTH];
			byte i = 0;
			for (final Entry<Double, Double> entry : valueMap.entrySet()) {
				x[i] = entry.getKey();
				values[i] = entry.getValue();
				i++;
			}

			dataProvider.setBufferSize(x.length);
			dataProvider.setCurrentXDataArray(x);
			dataProvider.setCurrentYDataArray(values);

			final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
			if (!fontRegistry.hasValueFor(FONT_KEY_AXIS_TITLE)) {
				final Font sysFont = Display.getCurrent().getSystemFont();
				fontRegistry.put(FONT_KEY_AXIS_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), sysFont.getFontData()[0].getHeight(), SWT.BOLD) });
			}
			final Font axisTitleFont = fontRegistry.get(FONT_KEY_AXIS_TITLE);

			abscissae.setTitle(Messages.get("lbl.graph.axis.x"));
			abscissae.setAutoScale(DEFAULT_AUTOSCALE);
			abscissae.setTitleFont(axisTitleFont);
			abscissae.setShowMajorGrid(true);

			ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
			ordinates.setAutoScale(DEFAULT_AUTOSCALE);
			ordinates.setTitleFont(axisTitleFont);
			ordinates.setShowMajorGrid(true);

			trace.setPointStyle(PointStyle.FILLED_DIAMOND);
			trace.setTraceColor(traceColor);
			trace.setLineWidth(DEFAULT_LINE_WIDTH);
			trace.setPointSize(DEFAULT_POINT_SIZE);

			xyGraph.addTrace(trace);
			xyGraph.setShowLegend(false);

			add(toolbarArmedXYGraph);

			xyGraph.getPlotArea().addMouseListener(new MouseListener.Stub() {
				@Override
				public void mousePressed(final MouseEvent me) {
					if (me.button == 1) { // left click
						final double rpm = abscissae.getPositionValue(me.getLocation().x, false) * 1000;
						final int index = Math.max(Math.min(Torque.indexOf(rpm), Torque.LENGTH - 1), 0);
						final short oldValue = (short) values[index];
						final short newValue = (short) Math.round(Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, ordinates.getPositionValue(me.getLocation().y, false))));
						if (oldValue != newValue) {
							values[index] = newValue;
							dataProvider.triggerUpdate();
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
							});
						}
					}
				}
			});

			abscissae.performAutoScale(true);
			ordinates.performAutoScale(true);
		}

		@Override
		protected void layout() {
			toolbarArmedXYGraph.setBounds(getBounds().getCopy());
			super.layout();
		}

		@Override
		public void refresh() {
			dataProvider.triggerUpdate();
		}

		@Override
		public IXYGraph getXyGraph() {
			return xyGraph;
		}

		@Override
		public Axis getAbscissae() {
			return abscissae;
		}

		@Override
		public Axis getOrdinates() {
			return ordinates;
		}

		@Override
		public CircularBufferDataProvider getDataProvider() {
			return dataProvider;
		}

		@Override
		public Trace getTrace() {
			return trace;
		}

		public ToolbarArmedXYGraph getToolbarArmedXYGraph() {
			return toolbarArmedXYGraph;
		}

		@Override
		public double[] getValues() {
			return values;
		}
	}

	public TorqueGraphDialog(final Shell parent) {
		this(parent, SWT.SHEET | SWT.RESIZE | SWT.MAX);
	}

	public TorqueGraphDialog(final Shell parent, final int style) {
		super(parent, style);
		setText(Messages.get("lbl.graph.title"));
	}

	public int open(final Map<Double, Double> values, final Color traceColor) {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setImages(Images.MAIN_ICONS);
		createContents(shell, values, traceColor);
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

	private void createContents(final Shell shell, final Map<Double, Double> values, final Color traceColor) {
		shell.setLayout(getLayout());
		createGraph(shell, values, traceColor);
		createButtonBox(shell);
	}

	private Layout getLayout() {
		return GridLayoutFactory.swtDefaults().create();
	}

	private void createGraph(final Shell shell, final Map<Double, Double> values, final Color traceColor) {
		canvas = new Canvas(shell, SWT.NULL);
		final LightweightSystem lws = new LightweightSystem(canvas);
		torqueGraph = new FullFeaturedTorqueGraph(values, traceColor);
		lws.setContents(torqueGraph);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);

		contextMenu = new ContextMenu(canvas);
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

	public Canvas getCanvas() {
		return canvas;
	}

	public ContextMenu getContextMenu() {
		return contextMenu;
	}

	class ContextMenu {

		private final Menu menu;
		private final MenuItem autoScaleMenuItem;
		private final MenuItem performAutoScaleMenuItem;
		private final Menu lineWidthSubMenu;
		private final MenuItem lineWidthMenuItem;
		private final Menu pointSizeSubMenu;
		private final MenuItem pointSizeMenuItem;

		public ContextMenu(final Control control) {
			menu = new Menu(control);
			control.setMenu(menu);

			autoScaleMenuItem = new MenuItem(menu, SWT.CHECK);
			autoScaleMenuItem.setSelection(DEFAULT_AUTOSCALE);
			autoScaleMenuItem.setText(Messages.get("lbl.menu.item.autoscaling"));
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

			performAutoScaleMenuItem = new MenuItem(menu, SWT.PUSH);
			performAutoScaleMenuItem.setText(Messages.get("lbl.menu.item.autoscale.now"));
			performAutoScaleMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					torqueGraph.getAbscissae().performAutoScale(true);
					torqueGraph.getOrdinates().performAutoScale(true);
				}
			});

			lineWidthMenuItem = new MenuItem(menu, SWT.CASCADE);
			lineWidthMenuItem.setText(Messages.get("lbl.menu.item.line.width"));

			lineWidthSubMenu = new Menu(lineWidthMenuItem);
			lineWidthMenuItem.setMenu(lineWidthSubMenu);

			for (final byte lineWidth : LINE_WIDTH_OPTIONS) {
				final MenuItem menuItem = new MenuItem(lineWidthSubMenu, SWT.RADIO);
				menuItem.setText("&" + lineWidth);
				if (lineWidth == DEFAULT_LINE_WIDTH) {
					menuItem.setSelection(true);
					lineWidthSubMenu.setDefaultItem(menuItem);
				}
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						torqueGraph.getTrace().setLineWidth(lineWidth);
					}
				});
			}

			pointSizeMenuItem = new MenuItem(menu, SWT.CASCADE);
			pointSizeMenuItem.setText(Messages.get("lbl.menu.item.point.size"));

			pointSizeSubMenu = new Menu(pointSizeMenuItem);
			pointSizeMenuItem.setMenu(pointSizeSubMenu);

			for (final byte pointSize : POINT_SIZE_OPTIONS) {
				final MenuItem menuItem = new MenuItem(pointSizeSubMenu, SWT.RADIO);
				menuItem.setText("&" + pointSize);
				if (pointSize == DEFAULT_POINT_SIZE) {
					menuItem.setSelection(true);
					pointSizeSubMenu.setDefaultItem(menuItem);
				}
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						torqueGraph.getTrace().setPointSize(pointSize);
					}
				});
			}

			control.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(final MenuDetectEvent e) {
					menu.setVisible(true);
				}
			});
		}

		public Menu getMenu() {
			return menu;
		}

		public MenuItem getAutoScaleMenuItem() {
			return autoScaleMenuItem;
		}

		public MenuItem getPerformAutoScaleMenuItem() {
			return performAutoScaleMenuItem;
		}

		public Menu getLineWidthSubMenu() {
			return lineWidthSubMenu;
		}

		public MenuItem getLineWidthMenuItem() {
			return lineWidthMenuItem;
		}

		public Menu getPointSizeSubMenu() {
			return pointSizeSubMenu;
		}

		public MenuItem getPointSizeMenuItem() {
			return pointSizeMenuItem;
		}

	}

}
