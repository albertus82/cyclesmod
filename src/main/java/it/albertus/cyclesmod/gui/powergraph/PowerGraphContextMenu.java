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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.i18n.LocalizedWidgets;
import it.albertus.util.ISupplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

public abstract class PowerGraphContextMenu implements Multilanguage {

	private static final byte[] POINT_SIZE_OPTIONS = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
	private static final byte[] LINE_WIDTH_OPTIONS = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	private static final short[] AREA_ALPHA_OPTIONS = { 26, 51, 77, 102, 128, 153, 179, 204, 230, 255 };

	private static final Messages messages = GuiMessages.INSTANCE;

	private final LocalizedWidgets localizedWidgets = new LocalizedWidgets();

	private final IPowerGraph powerGraph;
	private final Control parent;

	@Getter(AccessLevel.PROTECTED) private final Menu menu;

	protected PowerGraphContextMenu(final Control parent, final IPowerGraph powerGraph) {
		this.parent = parent;
		this.powerGraph = powerGraph;

		menu = new Menu(parent);
		parent.setMenu(menu);
	}

	protected SubMenu<TraceType> addTraceTypeSubMenu() {
		final Map<TraceType, MenuItem> traceTypeSubMenuItems = new EnumMap<>(TraceType.class);
		final MenuItem traceTypeMenuItem = newLocalizedMenuItem(menu, SWT.CASCADE, "gui.lbl.menu.item.graph.traceType");

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

		final MenuItem areaAlphaMenuItem = newLocalizedMenuItem(traceTypeSubMenu, SWT.CASCADE, "gui.lbl.menu.item.graph.areaAlpha");

		final Menu areaAlphaSubMenu = new Menu(areaAlphaMenuItem);
		areaAlphaMenuItem.setMenu(areaAlphaSubMenu);

		final Map<Integer, MenuItem> areaAlphaSubMenuItems = new HashMap<>();
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

		parent.addMenuDetectListener(e -> {
			for (final Entry<TraceType, MenuItem> entry : traceTypeSubMenuItems.entrySet()) {
				entry.getValue().setSelection(entry.getKey().equals(powerGraph.getPowerTrace().getTraceType()));
			}
			for (final Entry<Integer, MenuItem> entry : areaAlphaSubMenuItems.entrySet()) {
				entry.getValue().setSelection(Math.round(entry.getKey().floatValue() / 255 * AREA_ALPHA_OPTIONS.length) == Math.round(powerGraph.getPowerTrace().getAreaAlpha() / 255f * AREA_ALPHA_OPTIONS.length));
			}
		});

		final SubMenu<TraceType> subMenu = new SubMenu<>(traceTypeMenuItem, traceTypeSubMenuItems);
		subMenu.getChildren().add(new SubMenu<Integer>(areaAlphaMenuItem, areaAlphaSubMenuItems));
		return subMenu;
	}

	protected SubMenu<Integer> addLineWidthSubMenu() {
		final Map<Integer, MenuItem> lineWidthSubMenuItems = new HashMap<>();
		final MenuItem lineWidthMenuItem = newLocalizedMenuItem(menu, SWT.CASCADE, "gui.lbl.menu.item.graph.lineWidth");

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

		parent.addMenuDetectListener(e -> {
			for (final Entry<Integer, MenuItem> entry : lineWidthSubMenuItems.entrySet()) {
				entry.getValue().setSelection(entry.getKey().intValue() == powerGraph.getPowerTrace().getLineWidth());
			}
		});

		return new SubMenu<>(lineWidthMenuItem, lineWidthSubMenuItems);
	}

	protected SubMenu<PointStyle> addPointStyleSubMenu() {
		final Map<PointStyle, MenuItem> pointStyleSubMenuItems = new EnumMap<>(PointStyle.class);
		final MenuItem pointStyleMenuItem = newLocalizedMenuItem(menu, SWT.CASCADE, "gui.lbl.menu.item.graph.pointStyle");

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

		parent.addMenuDetectListener(e -> {
			for (final Entry<PointStyle, MenuItem> entry : pointStyleSubMenuItems.entrySet()) {
				entry.getValue().setSelection(entry.getKey().equals(powerGraph.getPowerTrace().getPointStyle()));
			}
		});

		return new SubMenu<>(pointStyleMenuItem, pointStyleSubMenuItems);
	}

	protected SubMenu<Integer> addPointSizeSubMenu() {
		final Trace trace = powerGraph.getPowerTrace();

		final Map<Integer, MenuItem> pointSizeSubMenuItems = new HashMap<>();
		final MenuItem pointSizeMenuItem = newLocalizedMenuItem(menu, SWT.CASCADE, "gui.lbl.menu.item.graph.pointSize");

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

		parent.addMenuDetectListener(e -> {
			for (final Entry<Integer, MenuItem> entry : pointSizeSubMenuItems.entrySet()) {
				entry.getValue().setSelection(entry.getKey().intValue() == trace.getPointSize());
			}
		});

		return new SubMenu<>(pointSizeMenuItem, pointSizeSubMenuItems);
	}

	protected MenuItem addShowTorqueMenuItem() {
		final MenuItem showTorqueMenuItem = newLocalizedMenuItem(menu, SWT.CHECK, "gui.lbl.menu.item.graph.showTorque");
		showTorqueMenuItem.setSelection(powerGraph.isTorqueVisible());
		showTorqueMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				powerGraph.toggleTorqueVisibility(showTorqueMenuItem.getSelection());
			}
		});
		return showTorqueMenuItem;
	}

	@Value
	public class SubMenu<K> {
		MenuItem menuItem;
		Map<K, MenuItem> menuItems;
		List<SubMenu<?>> children = new ArrayList<>();
	}

	@Override
	public void updateLanguage() {
		localizedWidgets.resetAllTexts();
	}

	protected MenuItem newLocalizedMenuItem(@NonNull final Menu parent, final int style, @NonNull final String messageKey) {
		return newLocalizedMenuItem(parent, style, () -> messages.get(messageKey));
	}

	protected MenuItem newLocalizedMenuItem(@NonNull final Menu parent, final int style, @NonNull final ISupplier<String> textSupplier) {
		return localizedWidgets.putAndReturn(new MenuItem(parent, style), textSupplier).getKey();
	}

}
