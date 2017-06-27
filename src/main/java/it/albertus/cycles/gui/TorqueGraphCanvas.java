package it.albertus.cycles.gui;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
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

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

public class TorqueGraphCanvas extends Canvas {

	private static final byte[] POINT_SIZE_OPTIONS = { 0, 2, 4, 6, 8, 10, 12, 14, 16 };
	private static final byte[] LINE_WIDTH_OPTIONS = { 1, 2, 3, 4, 5 };

	private static final byte DEFAULT_POINT_SIZE = 4;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	private final ContextMenu contextMenu;
	private final SimpleTorqueGraph torqueGraph;

	private static class SimpleTorqueGraph extends TorqueGraph {

		private static final float TITLE_FONT_HEIGHT_FACTOR = 1.25f;

		private static final byte DEFAULT_POINT_SIZE = 4;
		private static final byte DEFAULT_LINE_WIDTH = 2;

		private SimpleTorqueGraph(final Bike bike) {
			super(getValueMap(bike));

			final Axis abscissae = getAbscissae();
			abscissae.setAutoScale(DEFAULT_AUTOSCALE);

			final Axis ordinates = getOrdinates();
			ordinates.setAutoScale(DEFAULT_AUTOSCALE);

			final Trace trace = getTrace();
			trace.setPointStyle(PointStyle.FILLED_DIAMOND);
			trace.setTraceColor(getColor(bike.getType()));

			final IXYGraph xyGraph = getXyGraph();
			xyGraph.setTitle(Messages.get("lbl.graph.title"));
			final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
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

		private static Map<Double, Double> getValueMap(final Bike bike) {
			final Map<Double, Double> map = new TreeMap<Double, Double>();
			for (byte i = 0; i < bike.getTorque().getCurve().length; i++) {
				map.put(((double) Torque.getRpm(i)) / 1000, (double) bike.getTorque().getCurve()[i]);
			}
			return map;
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

	private class ContextMenu {

		private final Menu menu;
		private final Menu lineWidthSubMenu;
		private final MenuItem lineWidthMenuItem;
		private final Menu pointSizeSubMenu;
		private final MenuItem pointSizeMenuItem;

		public ContextMenu(final Control parent) {
			menu = new Menu(parent);
			parent.setMenu(menu);

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
			//			autoScaleMenuItem.setText(Messages.get("lbl.menu.item.autoscaling"));
			lineWidthMenuItem.setText(Messages.get("lbl.menu.item.line.width"));
			pointSizeMenuItem.setText(Messages.get("lbl.menu.item.point.size"));
		}

	}

}
