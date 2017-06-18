package it.albertus.cycles.gui;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cycles.gui.listener.MouseAdapter;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

public class TorqueGraph extends Canvas {

	private static final byte[] POINT_SIZE_OPTIONS = { 0, 2, 4, 6, 8, 10, 12, 14, 16 };
	private static final byte DEFAULT_POINT_SIZE = 4;

	private static final byte[] LINE_WIDTH_OPTIONS = { 1, 2, 3, 4, 5 };
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	private static final String FONT_KEY_GRAPH_TITLE = "graphTitle";
	private static final String FONT_KEY_AXIS_TITLE = "axisTitle";

	private static final float TITLE_FONT_HEIGHT_FACTOR = 1.25f;

	private final Bike bike;

	private final Trace trace;
	private final double[] values;

	private final XYGraph xyGraph;
	private final Axis abscissae;
	private final Axis ordinates;

	private final ContextMenu contextMenu;

	TorqueGraph(final Composite parent, final Bike bike) {
		super(parent, SWT.NULL);

		this.bike = bike;

		final LightweightSystem lws = new LightweightSystem(this);

		xyGraph = new XYGraph();
		xyGraph.setTitle(Messages.get("lbl.graph.title"));
		lws.setContents(xyGraph);

		final double[] x = new double[Torque.LENGTH];
		final double[] y = new double[Torque.LENGTH];
		for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
			x[i] = ((double) Torque.getRpm(i)) / 1000;
			y[i] = bike.getTorque().getCurve()[i];
		}

		final CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
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
		abscissae.addMouseListener(new MouseAdapter() {
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
		ordinates.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClicked(final MouseEvent me) {
				ordinates.performAutoScale(true);
			}
		});

		trace = new Trace("Torque", abscissae, ordinates, traceDataProvider);
		trace.setLineWidth(DEFAULT_LINE_WIDTH);
		trace.setPointSize(DEFAULT_POINT_SIZE);
		trace.setPointStyle(PointStyle.FILLED_DIAMOND);
		switch (bike.getType()) {
		case CLASS_125:
			trace.setTraceColor(getDisplay().getSystemColor(SWT.COLOR_RED));
			break;
		case CLASS_250:
			trace.setTraceColor(getDisplay().getSystemColor(SWT.COLOR_BLUE));
			break;
		case CLASS_500:
			trace.setTraceColor(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			break;
		}

		xyGraph.addTrace(trace);
		xyGraph.setShowLegend(false);

		if (!fontRegistry.hasValueFor(FONT_KEY_GRAPH_TITLE)) {
			final Font sysFont = Display.getCurrent().getSystemFont();
			fontRegistry.put(FONT_KEY_GRAPH_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), (int) (sysFont.getFontData()[0].getHeight() * TITLE_FONT_HEIGHT_FACTOR), SWT.BOLD) });
		}
		xyGraph.setTitleFont(fontRegistry.get(FONT_KEY_GRAPH_TITLE));

		contextMenu = new ContextMenu(this);

		this.values = y;
	}

	public Trace getTrace() {
		return trace;
	}

	public double[] getValues() {
		return values;
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

	public void updateTexts() {
		xyGraph.setTitle(Messages.get("lbl.graph.title"));
		abscissae.setTitle(Messages.get("lbl.graph.axis.x"));
		ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
		contextMenu.updateTexts();
	}

	public XYGraph getXyGraph() {
		return xyGraph;
	}

	public Axis getAbscissae() {
		return abscissae;
	}

	public Axis getOrdinates() {
		return ordinates;
	}

	public Bike getBike() {
		return bike;
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
