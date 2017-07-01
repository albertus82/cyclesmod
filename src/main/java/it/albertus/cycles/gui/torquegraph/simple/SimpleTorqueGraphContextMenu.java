package it.albertus.cycles.gui.torquegraph.simple;

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

import it.albertus.cycles.gui.torquegraph.TorqueGraphContextMenu;
import it.albertus.cycles.resources.Messages;

public class SimpleTorqueGraphContextMenu extends TorqueGraphContextMenu {

	private final MenuItem editMenuItem;
	private final SubMenu<TraceType> traceTypeSubMenu;
	private final SubMenu<Integer> lineWidthSubMenu;
	private final SubMenu<PointStyle> pointStyleSubMenu;
	private final SubMenu<Integer> pointSizeSubMenu;

	public SimpleTorqueGraphContextMenu(final Control parent, final SimpleTorqueGraph torqueGraph) {
		super(parent, torqueGraph);

		final Menu menu = getMenu();

		editMenuItem = new MenuItem(menu, SWT.PUSH);
		final String editMenuItemTextMessageKey = "lbl.menu.item.graph.edit";
		editMenuItem.setData(DATA_KEY_MSG_KEY, editMenuItemTextMessageKey);
		editMenuItem.setText(Messages.get(editMenuItemTextMessageKey));
		editMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				parent.notifyListeners(SWT.MouseDoubleClick, null);
			}
		});
		menu.setDefaultItem(editMenuItem);

		new MenuItem(menu, SWT.SEPARATOR);

		traceTypeSubMenu = addTraceTypeSubMenu();
		lineWidthSubMenu = addLineWidthSubMenu();
		pointStyleSubMenu = addPointStyleSubMenu();
		pointSizeSubMenu = addPointSizeSubMenu();

		parent.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(final MenuDetectEvent e) {
				menu.setVisible(true);
			}
		});
	}

	public void updateTexts() {
		editMenuItem.setText(Messages.get(editMenuItem.getData(DATA_KEY_MSG_KEY).toString()));
		final MenuItem traceTypeMenuItem = traceTypeSubMenu.getMenuItem();
		traceTypeMenuItem.setText(Messages.get(traceTypeMenuItem.getData(DATA_KEY_MSG_KEY).toString()));
		final MenuItem areaAlphaMenuItem = traceTypeSubMenu.getChildren().get(0).getMenuItem();
		areaAlphaMenuItem.setText(Messages.get(areaAlphaMenuItem.getData(DATA_KEY_MSG_KEY).toString()));
		final MenuItem lineWidthMenuItem = lineWidthSubMenu.getMenuItem();
		lineWidthMenuItem.setText(Messages.get(lineWidthMenuItem.getData(DATA_KEY_MSG_KEY).toString()));
		final MenuItem pointStyleMenuItem = pointStyleSubMenu.getMenuItem();
		pointStyleMenuItem.setText(Messages.get(pointStyleMenuItem.getData(DATA_KEY_MSG_KEY).toString()));
		final MenuItem pointSizeMenuItem = pointSizeSubMenu.getMenuItem();
		pointSizeMenuItem.setText(Messages.get(pointSizeMenuItem.getData(DATA_KEY_MSG_KEY).toString()));
	}

}
