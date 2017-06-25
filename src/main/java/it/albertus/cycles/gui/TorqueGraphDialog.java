package it.albertus.cycles.gui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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

import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;
import it.albertus.jface.JFaceMessages;
import it.albertus.jface.SwtUtils;
import it.albertus.util.logging.LoggerFactory;

public class TorqueGraphDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(TorqueGraphDialog.class);

	private static final byte[] POINT_SIZE_OPTIONS = { 0, 2, 4, 6, 8, 10, 12, 14, 16 };
	private static final byte DEFAULT_POINT_SIZE = 4;

	private static final byte[] LINE_WIDTH_OPTIONS = { 1, 2, 3, 4, 5 };
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	private static final String FONT_KEY_GRAPH_TITLE = "graphTitle";
	private static final String FONT_KEY_AXIS_TITLE = "axisTitle";

	private static final float TITLE_FONT_HEIGHT_FACTOR = 1.25f;

	private volatile int returnCode = SWT.CANCEL;
	private Image[] images;
	private double[] values;

	private XYGraph xyGraph;
	private CircularBufferDataProvider traceDataProvider;
	private Axis abscissae;
	private Axis ordinates;
	private Trace trace;

	private ContextMenu contextMenu;

	public TorqueGraphDialog(final Shell parent) {
		this(parent, SWT.SHEET | SWT.RESIZE | SWT.MAX);
	}

	public TorqueGraphDialog(final Shell parent, final int style) {
		super(parent, style);
	}

	public int open(final Map<Double, Double> values) {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		final Image[] icons = getImages();
		if (icons != null && icons.length > 0) {
			shell.setImages(icons);
		}
		createContents(shell, values);
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

	protected Point getSize(final Shell shell) {
		final Point normalShellSize = shell.getSize();
		int size = (int) (Math.min(normalShellSize.x, normalShellSize.y) / 1.25);
		return new Point(size, size);
	}

	protected Point getMinimumSize(final Shell shell) {
		return shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}

	protected void createContents(final Shell shell, final Map<Double, Double> values) {
		shell.setLayout(getLayout());
		final XYGraph graph = createGraph(shell, values);
		createButtonBox(shell);
	}

	private XYGraph createGraph(final Shell shell, final Map<Double, Double> values) {
		final Canvas canvas = new Canvas(shell, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);

		final LightweightSystem lws = new LightweightSystem(canvas);

		xyGraph = new XYGraph();
		xyGraph.setTitle(Messages.get("lbl.graph.title"));
		lws.setContents(xyGraph);

		final double[] x = new double[Torque.LENGTH];
		final double[] y = new double[Torque.LENGTH];
		int i = 0;
		for (final Entry<Double, Double> entry : values.entrySet()) {
			x[i] = entry.getKey();
			y[i] = entry.getValue();
			i++;
		}

		traceDataProvider = new CircularBufferDataProvider(false);
		traceDataProvider.setBufferSize(x.length);
		traceDataProvider.setCurrentXDataArray(x);
		traceDataProvider.setCurrentYDataArray(y);

		final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (!fontRegistry.hasValueFor(FONT_KEY_AXIS_TITLE)) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put(FONT_KEY_AXIS_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), sysFont.getFontData()[0].getHeight(), SWT.BOLD) });
		}
		final Font axisTitleFont = fontRegistry.get(FONT_KEY_AXIS_TITLE);

		abscissae = xyGraph.getPrimaryXAxis();
		abscissae.setTitle(Messages.get("lbl.graph.axis.x"));
		abscissae.setAutoScale(DEFAULT_AUTOSCALE);
		abscissae.setTitleFont(axisTitleFont);
		abscissae.setShowMajorGrid(true);
		abscissae.setZoomType(ZoomType.DYNAMIC_ZOOM);
		abscissae.addMouseListener(new MouseListener.Stub() {
			@Override
			public void mouseDoubleClicked(final MouseEvent me) {
				abscissae.performAutoScale(true);
			}
		});

		ordinates = xyGraph.getPrimaryYAxis();
		ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
		ordinates.setAutoScale(DEFAULT_AUTOSCALE);
		ordinates.setTitleFont(axisTitleFont);
		ordinates.setShowMajorGrid(true);
		ordinates.setZoomType(ZoomType.DYNAMIC_ZOOM);
		ordinates.addMouseListener(new MouseListener.Stub() {
			@Override
			public void mouseDoubleClicked(final MouseEvent me) {
				ordinates.performAutoScale(true);
			}
		});

		trace = new Trace("Torque", abscissae, ordinates, traceDataProvider);
		trace.setLineWidth(DEFAULT_LINE_WIDTH);
		trace.setPointSize(DEFAULT_POINT_SIZE);
		trace.setPointStyle(PointStyle.FILLED_DIAMOND);

		xyGraph.addTrace(trace);
		xyGraph.setShowLegend(false);

		if (!fontRegistry.hasValueFor(FONT_KEY_GRAPH_TITLE)) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put(FONT_KEY_GRAPH_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), (int) (sysFont.getFontData()[0].getHeight() * TITLE_FONT_HEIGHT_FACTOR), SWT.BOLD) });
		}
		xyGraph.setTitleFont(fontRegistry.get(FONT_KEY_GRAPH_TITLE));

		contextMenu = new ContextMenu(canvas);

		this.values = y;

		xyGraph.getPlotArea().addMouseListener(new MouseListener.Stub() {
			@Override
			public void mousePressed(final MouseEvent me) {
				if (me.button == 1) { // left click
					final XYGraph xyGraph = (XYGraph) ((PlotArea) me.getSource()).getParent();
					final double rpm = xyGraph.getPrimaryXAxis().getPositionValue(me.getLocation().x, false) * 1000;
					final int index = Math.min(Torque.indexOf(rpm), Torque.LENGTH - 1);
					final double val = Math.max(Torque.MIN_VALUE, Math.min(Torque.MAX_VALUE, xyGraph.getPrimaryYAxis().getPositionValue(me.getLocation().y, false)));
					TorqueGraphDialog.this.values[index] = val;
					final IDataProvider dataProvider = trace.getDataProvider();
					if (dataProvider instanceof CircularBufferDataProvider) {
						final CircularBufferDataProvider circularBufferDataProvider = (CircularBufferDataProvider) dataProvider;
						circularBufferDataProvider.setCurrentYDataArray(TorqueGraphDialog.this.values);
						circularBufferDataProvider.triggerUpdate();
					}
				}
			}
		});

		return xyGraph;
	}

	public boolean refresh() {
		boolean success = false;
		final IDataProvider dataProvider = this.trace.getDataProvider();
		if (dataProvider instanceof CircularBufferDataProvider) {
			((CircularBufferDataProvider) dataProvider).triggerUpdate();
			success = true;
		}
		return success;
	}

	private Composite createButtonBox(final Shell shell) {
		final Composite buttonComposite = new Composite(shell, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(buttonComposite);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(buttonComposite);

		final Button confirmButton = new Button(buttonComposite, SWT.PUSH);
		confirmButton.setText(JFaceMessages.get("lbl.button.ok"));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).minSize(SwtUtils.convertHorizontalDLUsToPixels(confirmButton, IDialogConstants.BUTTON_WIDTH), SWT.DEFAULT).applyTo(confirmButton);
		confirmButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					setReturnCode(SWT.OK);
				}
				catch (final SWTException se) {
					logger.log(Level.FINE, se.toString(), se);
				}
				catch (final Exception e) {
					logger.log(Level.SEVERE, JFaceMessages.get("err.map.retrieve"), e);
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

		shell.setDefaultButton(confirmButton);
		return buttonComposite;
	}

	protected Layout getLayout() {
		return GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).create();
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(final int returnCode) {
		this.returnCode = returnCode;
	}

	public Image[] getImages() {
		return images;
	}

	public void setImages(final Image[] images) {
		this.images = images;
	}

	public double[] getValues() {
		return values;
	}

	class ContextMenu {

		private final Menu menu;
		private final MenuItem autoScaleMenuItem;
		private final MenuItem performAutoScaleMenuItem;
		private final Menu lineWidthSubMenu;
		private final MenuItem lineWidthMenuItem;
		private final Menu pointSizeSubMenu;
		private final MenuItem pointSizeMenuItem;

		public ContextMenu(final Control parent) {
			menu = new Menu(parent);
			parent.setMenu(menu);

			autoScaleMenuItem = new MenuItem(menu, SWT.CHECK);
			autoScaleMenuItem.setSelection(DEFAULT_AUTOSCALE);
			autoScaleMenuItem.setText(Messages.get("lbl.menu.item.autoscaling"));
			autoScaleMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (autoScaleMenuItem.getSelection()) {
						abscissae.setAutoScale(true);
						ordinates.setAutoScale(true);
					}
					else {
						abscissae.setAutoScale(false);
						ordinates.setAutoScale(false);
					}
				}
			});

			performAutoScaleMenuItem = new MenuItem(menu, SWT.PUSH);
			performAutoScaleMenuItem.setText(Messages.get("lbl.menu.item.autoscale.now"));
			performAutoScaleMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					abscissae.performAutoScale(true);
					ordinates.performAutoScale(true);
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
						trace.setLineWidth(lineWidth);
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
						trace.setPointSize(pointSize);
					}
				});
			}

			parent.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(final MenuDetectEvent e) {
					menu.setVisible(true);
				}
			});
		}

		public void updateTexts() {
			autoScaleMenuItem.setText(Messages.get("lbl.menu.item.autoscaling"));
			performAutoScaleMenuItem.setText(Messages.get("lbl.menu.item.autoscale.now"));
			lineWidthMenuItem.setText(Messages.get("lbl.menu.item.line.width"));
			pointSizeMenuItem.setText(Messages.get("lbl.menu.item.point.size"));
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
