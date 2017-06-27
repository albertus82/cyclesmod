package it.albertus.cycles.gui;

import java.util.Map;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.resources.Messages;

public class TorqueGraphCanvas extends Canvas {

	private static final byte[] POINT_SIZE_OPTIONS = { 0, 2, 4, 6, 8, 10, 12, 14, 16 };
	private static final byte[] LINE_WIDTH_OPTIONS = { 1, 2, 3, 4, 5 };

	private static final byte DEFAULT_POINT_SIZE = 4;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	private final ContextMenu contextMenu;
	private final SimpleTorqueGraph torqueGraph;

	private class SimpleTorqueGraph extends TorqueGraph {

		private static final float TITLE_FONT_HEIGHT_FACTOR = 1.25f;

		private static final byte DEFAULT_POINT_SIZE = 4;
		private static final byte DEFAULT_LINE_WIDTH = 2;

		SimpleTorqueGraph(final Bike bike) {
			this(getValueMap(bike), getColor(bike.getType()));
		}

		private SimpleTorqueGraph(final Map<Double, Double> valueMap, final Color traceColor) {
			super(valueMap);
			final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
			if (!fontRegistry.hasValueFor(FONT_KEY_AXIS_TITLE)) {
				final Font sysFont = Display.getCurrent().getSystemFont();
				fontRegistry.put(FONT_KEY_AXIS_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), sysFont.getFontData()[0].getHeight(), SWT.BOLD) });
			}
			final Font axisTitleFont = fontRegistry.get(FONT_KEY_AXIS_TITLE);

			final Axis abscissae = getAbscissae();
			abscissae.setAutoScale(DEFAULT_AUTOSCALE);
			abscissae.setTitleFont(axisTitleFont);
			abscissae.setZoomType(ZoomType.DYNAMIC_ZOOM);
			abscissae.addMouseListener(new MouseListener.Stub() {
				@Override
				public void mouseDoubleClicked(final MouseEvent me) {
					abscissae.performAutoScale(true);
				}
			});

			final Axis ordinates = getOrdinates();
			ordinates.setAutoScale(DEFAULT_AUTOSCALE);
			ordinates.setTitleFont(axisTitleFont);
			ordinates.setZoomType(ZoomType.DYNAMIC_ZOOM);
			ordinates.addMouseListener(new MouseListener.Stub() {
				@Override
				public void mouseDoubleClicked(final MouseEvent me) {
					ordinates.performAutoScale(true);
				}
			});

			final Trace trace = getTrace();
			trace.setPointStyle(PointStyle.FILLED_DIAMOND);
			trace.setTraceColor(traceColor);

			final IXYGraph xyGraph = getXyGraph();
			xyGraph.setTitle(Messages.get("lbl.graph.title"));
			if (!fontRegistry.hasValueFor(FONT_KEY_GRAPH_TITLE)) {
				final Font sysFont = Display.getCurrent().getSystemFont();
				fontRegistry.put(FONT_KEY_GRAPH_TITLE, new FontData[] { new FontData(sysFont.getFontData()[0].getName(), (int) (sysFont.getFontData()[0].getHeight() * TITLE_FONT_HEIGHT_FACTOR), SWT.BOLD) });
			}
			xyGraph.setTitleFont(fontRegistry.get(FONT_KEY_GRAPH_TITLE));

			trace.setLineWidth(DEFAULT_LINE_WIDTH);
			trace.setPointSize(DEFAULT_POINT_SIZE);

			add(xyGraph);
		}

		@Override
		protected void layout() {
			getXyGraph().setBounds(getBounds().getCopy());
			super.layout();
		}

	}

	public TorqueGraphCanvas(final Composite parent, final Bike bike) {
		super(parent, SWT.NULL);

		final LightweightSystem lws = new LightweightSystem(this);
		torqueGraph = new SimpleTorqueGraph(bike);
		lws.setContents(torqueGraph);

		this.contextMenu = new ContextMenu(this);
	}

	public void updateTexts() {
		torqueGraph.getXyGraph().setTitle(Messages.get("lbl.graph.title"));
		torqueGraph.getAbscissae().setTitle(Messages.get("lbl.graph.axis.x"));
		torqueGraph.getOrdinates().setTitle(Messages.get("lbl.graph.axis.y"));
		contextMenu.updateTexts();
	}

	public ITorqueGraph getTorqueGraph() {
		return torqueGraph;
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
