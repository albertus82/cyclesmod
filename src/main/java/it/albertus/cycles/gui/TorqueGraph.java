package it.albertus.cycles.gui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.Figure;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

public class TorqueGraph extends Figure implements ITorqueGraph {

	private static final byte[] POINT_SIZE_OPTIONS = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
	private static final byte[] LINE_WIDTH_OPTIONS = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	private static final short[] AREA_ALPHA_OPTIONS = { 25, 50, 75, 100, 125, 150, 175, 200, 225, 0xFF };

	private final IXYGraph xyGraph = new XYGraph();
	private final Axis abscissae = xyGraph.getPrimaryXAxis();
	private final Axis ordinates = xyGraph.getPrimaryYAxis();
	private final CircularBufferDataProvider dataProvider = new CircularBufferDataProvider(false);
	private final Trace trace = new Trace(Messages.get("lbl.graph.title"), abscissae, ordinates, dataProvider);
	private final double[] values = new double[Torque.LENGTH];
	private final double[] xDataArray = new double[Torque.LENGTH];

	public TorqueGraph(final Bike bike) {
		for (int i = 0; i < Torque.LENGTH; i++) {
			xDataArray[i] = (double) Torque.getRpm(i) / 1000;
			values[i] = bike.getTorque().getCurve()[i];
		}
		init(bike.getType());
	}

	public TorqueGraph(final Map<Integer, Short> map, final BikeType bikeType) {
		if (map.size() != Torque.LENGTH) {
			throw new IllegalArgumentException("map size must be " + Torque.LENGTH);
		}

		int i = 0;
		for (final Entry<Integer, Short> entry : map.entrySet()) {
			xDataArray[i] = entry.getKey().doubleValue() / 1000;
			values[i] = entry.getValue();
			i++;
		}
		init(bikeType);
	}

	protected void init(final BikeType bikeType) {
		dataProvider.setBufferSize(xDataArray.length);
		dataProvider.setCurrentXDataArray(xDataArray);
		dataProvider.setCurrentYDataArray(values);

		final Font axisTitleFont = Display.getCurrent().getSystemFont();

		abscissae.setTitle(Messages.get("lbl.graph.axis.x"));
		abscissae.setTitleFont(axisTitleFont);
		abscissae.setShowMajorGrid(true);

		ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
		ordinates.setTitleFont(axisTitleFont);
		ordinates.setShowMajorGrid(true);

		xyGraph.addTrace(trace);
		xyGraph.setShowLegend(false);

		trace.setTraceColor(getColor(bikeType));
	}

	private static Color getColor(final BikeType bikeType) {
		final Display display = Display.getCurrent();
		switch (bikeType) {
		case CLASS_125:
			return display.getSystemColor(SWT.COLOR_RED);
		case CLASS_250:
			return display.getSystemColor(SWT.COLOR_BLUE);
		case CLASS_500:
			return display.getSystemColor(SWT.COLOR_BLACK);
		default:
			throw new IllegalStateException("Unknown bike type: " + bikeType);
		}
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

	@Override
	public double[] getValues() {
		return values;
	}

	public class TorqueGraphContextMenu {

		public class SubMenu<K> {
			private final MenuItem menuItem;
			private final Map<K, MenuItem> menuItems;
			private final List<SubMenu<?>> children = new ArrayList<SubMenu<?>>();

			public SubMenu(final MenuItem menuItem, final Map<K, MenuItem> menuItems) {
				this.menuItem = menuItem;
				this.menuItems = menuItems;
			}

			public MenuItem getMenuItem() {
				return menuItem;
			}

			public Map<K, MenuItem> getMenuItems() {
				return menuItems;
			}

			public List<SubMenu<?>> getChildren() {
				return children;
			}
		}

		protected SubMenu<Integer> addPointSizeSubMenu(final Control control) {
			final Menu parentMenu = control.getMenu();
			final Map<Integer, MenuItem> pointSizeSubMenuItems = new HashMap<Integer, MenuItem>();
			final MenuItem pointSizeMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
			pointSizeMenuItem.setText(Messages.get("lbl.menu.item.point.size"));

			final Menu pointSizeSubMenu = new Menu(pointSizeMenuItem);
			pointSizeMenuItem.setMenu(pointSizeSubMenu);

			for (final int pointSize : POINT_SIZE_OPTIONS) {
				final MenuItem menuItem = new MenuItem(pointSizeSubMenu, SWT.RADIO);
				menuItem.setText("&" + pointSize);
				if (pointSize == trace.getPointSize()) {
					pointSizeSubMenu.setDefaultItem(menuItem);
				}
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						trace.setPointSize(pointSize);
					}
				});
				pointSizeSubMenuItems.put(pointSize, menuItem);
			}

			control.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(final MenuDetectEvent e) {
					for (final Entry<Integer, MenuItem> entry : pointSizeSubMenuItems.entrySet()) {
						entry.getValue().setSelection(entry.getKey().intValue() == trace.getPointSize());
					}
				}
			});

			return new SubMenu<Integer>(pointSizeMenuItem, pointSizeSubMenuItems);
		}

		protected SubMenu<Integer> addLineWidthSubMenu(final Control control) {
			final Menu parentMenu = control.getMenu();
			final Map<Integer, MenuItem> lineWidthSubMenuItems = new HashMap<Integer, MenuItem>();
			final MenuItem lineWidthMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
			lineWidthMenuItem.setText(Messages.get("lbl.menu.item.line.width"));

			final Menu lineWidthSubMenu = new Menu(lineWidthMenuItem);
			lineWidthMenuItem.setMenu(lineWidthSubMenu);

			for (final int lineWidth : LINE_WIDTH_OPTIONS) {
				final MenuItem menuItem = new MenuItem(lineWidthSubMenu, SWT.RADIO);
				menuItem.setText("&" + lineWidth);
				if (lineWidth == trace.getLineWidth()) {
					lineWidthSubMenu.setDefaultItem(menuItem);
				}
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						trace.setLineWidth(lineWidth);
					}
				});
				lineWidthSubMenuItems.put(lineWidth, menuItem);
			}

			control.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(final MenuDetectEvent e) {
					for (final Entry<Integer, MenuItem> entry : lineWidthSubMenuItems.entrySet()) {
						entry.getValue().setSelection(entry.getKey().intValue() == trace.getLineWidth());
					}
				}
			});

			return new SubMenu<Integer>(lineWidthMenuItem, lineWidthSubMenuItems);
		}

		protected SubMenu<TraceType> addTraceTypeSubMenu(final Control control) {
			final Menu parentMenu = control.getMenu();
			final Map<TraceType, MenuItem> traceTypeSubMenuItems = new EnumMap<TraceType, MenuItem>(TraceType.class);
			final MenuItem traceTypeMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
			traceTypeMenuItem.setText(Messages.get("lbl.menu.item.trace.type"));

			final Menu traceTypeSubMenu = new Menu(traceTypeMenuItem);
			traceTypeMenuItem.setMenu(traceTypeSubMenu);

			for (final TraceType traceType : TraceType.values()) {
				final MenuItem menuItem = new MenuItem(traceTypeSubMenu, SWT.RADIO);
				menuItem.setText("&" + traceType.toString());
				if (traceType.equals(trace.getTraceType())) {
					traceTypeSubMenu.setDefaultItem(menuItem);
				}
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						trace.setTraceType(traceType);
					}
				});
				traceTypeSubMenuItems.put(traceType, menuItem);
			}

			final MenuItem areaAlphaMenuItem = new MenuItem(traceTypeSubMenu, SWT.CASCADE);
			areaAlphaMenuItem.setText(Messages.get("lbl.menu.item.area.alpha"));

			final Menu areaAlphaSubMenu = new Menu(areaAlphaMenuItem);
			areaAlphaMenuItem.setMenu(areaAlphaSubMenu);

			final Map<Integer, MenuItem> areaAlphaSubMenuItems = new HashMap<Integer, MenuItem>();
			for (final int areaAlpha : AREA_ALPHA_OPTIONS) {
				final MenuItem menuItem = new MenuItem(areaAlphaSubMenu, SWT.RADIO);
				menuItem.setText("&" + (int) Math.min(areaAlpha / 2.5, 100) + "%");
				if (areaAlpha == trace.getAreaAlpha()) {
					areaAlphaSubMenu.setDefaultItem(menuItem);
				}
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						trace.setAreaAlpha(areaAlpha);
					}
				});
				areaAlphaSubMenuItems.put(areaAlpha, menuItem);
			}

			control.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(final MenuDetectEvent e) {
					for (final Entry<TraceType, MenuItem> entry : traceTypeSubMenuItems.entrySet()) {
						entry.getValue().setSelection(entry.getKey().equals(trace.getTraceType()));
					}
					for (final Entry<Integer, MenuItem> entry : areaAlphaSubMenuItems.entrySet()) {
						entry.getValue().setSelection(entry.getKey().equals(trace.getAreaAlpha()));
					}
				}
			});

			final SubMenu<TraceType> subMenu = new SubMenu<TraceType>(traceTypeMenuItem, traceTypeSubMenuItems);
			subMenu.getChildren().add(new SubMenu<Integer>(areaAlphaMenuItem, areaAlphaSubMenuItems));
			return subMenu;
		}

		protected SubMenu<PointStyle> addPointStyleSubMenu(final Control control) {
			final Menu parentMenu = control.getMenu();
			final Map<PointStyle, MenuItem> pointStyleSubMenuItems = new EnumMap<PointStyle, MenuItem>(PointStyle.class);
			final MenuItem pointStyleMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
			pointStyleMenuItem.setText(Messages.get("lbl.menu.item.point.style"));

			final Menu traceTypeSubMenu = new Menu(pointStyleMenuItem);
			pointStyleMenuItem.setMenu(traceTypeSubMenu);

			for (final PointStyle pointStyle : PointStyle.values()) {
				final MenuItem menuItem = new MenuItem(traceTypeSubMenu, SWT.RADIO);
				menuItem.setText("&" + pointStyle.toString());
				if (pointStyle.equals(trace.getPointStyle())) {
					traceTypeSubMenu.setDefaultItem(menuItem);
				}
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						trace.setPointStyle(pointStyle);
					}
				});
				pointStyleSubMenuItems.put(pointStyle, menuItem);
			}

			control.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(final MenuDetectEvent e) {
					for (final Entry<PointStyle, MenuItem> entry : pointStyleSubMenuItems.entrySet()) {
						entry.getValue().setSelection(entry.getKey().equals(trace.getPointStyle()));
					}
				}
			});

			return new SubMenu<PointStyle>(pointStyleMenuItem, pointStyleSubMenuItems);
		}
	}

}
