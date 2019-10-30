package it.albertus.cyclesmod.gui.powergraph;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.resources.Messages;

public abstract class PowerGraphContextMenu {

	protected static final String TEXT_MESSAGE_KEY = "TEXT_MESSAGE_KEY";

	private static final byte[] POINT_SIZE_OPTIONS = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
	private static final byte[] LINE_WIDTH_OPTIONS = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	private static final short[] AREA_ALPHA_OPTIONS = { 26, 51, 77, 102, 128, 153, 179, 204, 230, 255 };

	private final IPowerGraph powerGraph;
	private final Control parent;
	private final Menu menu;

	public PowerGraphContextMenu(final Control parent, final IPowerGraph powerGraph) {
		this.parent = parent;
		this.powerGraph = powerGraph;

		menu = new Menu(parent);
		parent.setMenu(menu);
	}

	public Menu getMenu() {
		return menu;
	}

	protected SubMenu<TraceType> addTraceTypeSubMenu() {
		final Map<TraceType, MenuItem> traceTypeSubMenuItems = new EnumMap<TraceType, MenuItem>(TraceType.class);
		final MenuItem traceTypeMenuItem = new MenuItem(menu, SWT.CASCADE);
		final String traceTypeMenuItemTextMessageKey = "lbl.menu.item.graph.traceType";
		traceTypeMenuItem.setData(TEXT_MESSAGE_KEY, traceTypeMenuItemTextMessageKey);
		traceTypeMenuItem.setText(Messages.get(traceTypeMenuItemTextMessageKey));

		final Menu traceTypeSubMenu = new Menu(traceTypeMenuItem);
		traceTypeMenuItem.setMenu(traceTypeSubMenu);

		for (final TraceType traceType : TraceType.values()) {
			final MenuItem menuItem = new MenuItem(traceTypeSubMenu, SWT.RADIO);
			menuItem.setText("&" + traceType.toString());
			if (traceType.equals(powerGraph.getPowerTrace().getTraceType())) {
				traceTypeSubMenu.setDefaultItem(menuItem);
			}
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					powerGraph.getPowerTrace().setTraceType(traceType);
				}
			});
			traceTypeSubMenuItems.put(traceType, menuItem);
		}

		final MenuItem areaAlphaMenuItem = new MenuItem(traceTypeSubMenu, SWT.CASCADE);
		final String areaAlphaMenuItemTextMessageKey = "lbl.menu.item.graph.areaAlpha";
		areaAlphaMenuItem.setData(TEXT_MESSAGE_KEY, areaAlphaMenuItemTextMessageKey);
		areaAlphaMenuItem.setText(Messages.get(areaAlphaMenuItemTextMessageKey));

		final Menu areaAlphaSubMenu = new Menu(areaAlphaMenuItem);
		areaAlphaMenuItem.setMenu(areaAlphaSubMenu);

		final Map<Integer, MenuItem> areaAlphaSubMenuItems = new HashMap<Integer, MenuItem>();
		for (final int areaAlpha : AREA_ALPHA_OPTIONS) {
			final MenuItem menuItem = new MenuItem(areaAlphaSubMenu, SWT.RADIO);
			menuItem.setText("&" + Math.round(areaAlpha / 2.55f) + "%");
			if (Math.round(areaAlpha / 255f * AREA_ALPHA_OPTIONS.length) == Math.round(powerGraph.getPowerTrace().getAreaAlpha() / 255f * AREA_ALPHA_OPTIONS.length)) {
				areaAlphaSubMenu.setDefaultItem(menuItem);
			}
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					powerGraph.getPowerTrace().setAreaAlpha(areaAlpha);
				}
			});
			areaAlphaSubMenuItems.put(areaAlpha, menuItem);
		}

		parent.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(final MenuDetectEvent e) {
				for (final Entry<TraceType, MenuItem> entry : traceTypeSubMenuItems.entrySet()) {
					entry.getValue().setSelection(entry.getKey().equals(powerGraph.getPowerTrace().getTraceType()));
				}
				for (final Entry<Integer, MenuItem> entry : areaAlphaSubMenuItems.entrySet()) {
					entry.getValue().setSelection(Math.round(entry.getKey().floatValue() / 255 * AREA_ALPHA_OPTIONS.length) == Math.round(powerGraph.getPowerTrace().getAreaAlpha() / 255f * AREA_ALPHA_OPTIONS.length));
				}
			}
		});

		final SubMenu<TraceType> subMenu = new SubMenu<TraceType>(traceTypeMenuItem, traceTypeSubMenuItems);
		subMenu.getChildren().add(new SubMenu<Integer>(areaAlphaMenuItem, areaAlphaSubMenuItems));
		return subMenu;
	}

	protected SubMenu<Integer> addLineWidthSubMenu() {
		final Map<Integer, MenuItem> lineWidthSubMenuItems = new HashMap<Integer, MenuItem>();
		final MenuItem lineWidthMenuItem = new MenuItem(menu, SWT.CASCADE);
		final String lineWidthMenuItemTextMessageKey = "lbl.menu.item.graph.lineWidth";
		lineWidthMenuItem.setData(TEXT_MESSAGE_KEY, lineWidthMenuItemTextMessageKey);
		lineWidthMenuItem.setText(Messages.get(lineWidthMenuItemTextMessageKey));

		final Menu lineWidthSubMenu = new Menu(lineWidthMenuItem);
		lineWidthMenuItem.setMenu(lineWidthSubMenu);

		for (final int lineWidth : LINE_WIDTH_OPTIONS) {
			final MenuItem menuItem = new MenuItem(lineWidthSubMenu, SWT.RADIO);
			menuItem.setText("&" + lineWidth);
			if (lineWidth == powerGraph.getPowerTrace().getLineWidth()) {
				lineWidthSubMenu.setDefaultItem(menuItem);
			}
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					powerGraph.getPowerTrace().setLineWidth(lineWidth);
					powerGraph.getTorqueTrace().setLineWidth(lineWidth);
				}
			});
			lineWidthSubMenuItems.put(lineWidth, menuItem);
		}

		parent.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(final MenuDetectEvent e) {
				for (final Entry<Integer, MenuItem> entry : lineWidthSubMenuItems.entrySet()) {
					entry.getValue().setSelection(entry.getKey().intValue() == powerGraph.getPowerTrace().getLineWidth());
				}
			}
		});

		return new SubMenu<Integer>(lineWidthMenuItem, lineWidthSubMenuItems);
	}

	protected SubMenu<PointStyle> addPointStyleSubMenu() {
		final Map<PointStyle, MenuItem> pointStyleSubMenuItems = new EnumMap<PointStyle, MenuItem>(PointStyle.class);
		final MenuItem pointStyleMenuItem = new MenuItem(menu, SWT.CASCADE);
		final String pointStyleMenuItemTextMessageKey = "lbl.menu.item.graph.pointStyle";
		pointStyleMenuItem.setData(TEXT_MESSAGE_KEY, pointStyleMenuItemTextMessageKey);
		pointStyleMenuItem.setText(Messages.get(pointStyleMenuItemTextMessageKey));

		final Menu traceTypeSubMenu = new Menu(pointStyleMenuItem);
		pointStyleMenuItem.setMenu(traceTypeSubMenu);

		for (final PointStyle pointStyle : PointStyle.values()) {
			final MenuItem menuItem = new MenuItem(traceTypeSubMenu, SWT.RADIO);
			menuItem.setText("&" + pointStyle.toString());
			if (pointStyle.equals(powerGraph.getPowerTrace().getPointStyle())) {
				traceTypeSubMenu.setDefaultItem(menuItem);
			}
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					powerGraph.getPowerTrace().setPointStyle(pointStyle);
				}
			});
			pointStyleSubMenuItems.put(pointStyle, menuItem);
		}

		parent.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(final MenuDetectEvent e) {
				for (final Entry<PointStyle, MenuItem> entry : pointStyleSubMenuItems.entrySet()) {
					entry.getValue().setSelection(entry.getKey().equals(powerGraph.getPowerTrace().getPointStyle()));
				}
			}
		});

		return new SubMenu<PointStyle>(pointStyleMenuItem, pointStyleSubMenuItems);
	}

	protected SubMenu<Integer> addPointSizeSubMenu() {
		final Trace trace = powerGraph.getPowerTrace();

		final Map<Integer, MenuItem> pointSizeSubMenuItems = new HashMap<Integer, MenuItem>();
		final MenuItem pointSizeMenuItem = new MenuItem(menu, SWT.CASCADE);
		final String pointSizeMenuItemTextMessageKey = "lbl.menu.item.graph.pointSize";
		pointSizeMenuItem.setData(TEXT_MESSAGE_KEY, pointSizeMenuItemTextMessageKey);
		pointSizeMenuItem.setText(Messages.get(pointSizeMenuItemTextMessageKey));

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

		parent.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(final MenuDetectEvent e) {
				for (final Entry<Integer, MenuItem> entry : pointSizeSubMenuItems.entrySet()) {
					entry.getValue().setSelection(entry.getKey().intValue() == trace.getPointSize());
				}
			}
		});

		return new SubMenu<Integer>(pointSizeMenuItem, pointSizeSubMenuItems);
	}

	protected MenuItem addShowTorqueMenuItem() {
		final MenuItem showTorqueMenuItem = new MenuItem(menu, SWT.CHECK);
		final String showTorqueMenuItemTextMessageKey = "lbl.menu.item.graph.showTorque";
		showTorqueMenuItem.setData(TEXT_MESSAGE_KEY, showTorqueMenuItemTextMessageKey);
		showTorqueMenuItem.setText(Messages.get(showTorqueMenuItemTextMessageKey));
		showTorqueMenuItem.setSelection(powerGraph.isTorqueVisible());
		showTorqueMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				powerGraph.toggleTorqueVisibility(showTorqueMenuItem.getSelection());
			}
		});
		return showTorqueMenuItem;
	}

	@SuppressWarnings("rawtypes")
	public class SubMenu<K> {
		private final MenuItem menuItem;
		private final Map<K, MenuItem> menuItems;
		private final List<SubMenu> children = new ArrayList<SubMenu>();

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

		public List<SubMenu> getChildren() {
			return children;
		}
	}

}
