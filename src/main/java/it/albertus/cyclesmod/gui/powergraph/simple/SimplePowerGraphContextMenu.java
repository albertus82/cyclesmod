package it.albertus.cyclesmod.gui.powergraph.simple;

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

import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import it.albertus.cyclesmod.gui.powergraph.PowerGraphContextMenu;
import it.albertus.cyclesmod.resources.Messages;

public class SimplePowerGraphContextMenu extends PowerGraphContextMenu {

	private final MenuItem editMenuItem;
	private final SubMenu<TraceType> traceTypeSubMenu;
	private final SubMenu<Integer> lineWidthSubMenu;
	private final SubMenu<PointStyle> pointStyleSubMenu;
	private final SubMenu<Integer> pointSizeSubMenu;
	private final MenuItem showTorqueMenuItem;

	public SimplePowerGraphContextMenu(final Control parent, final IPowerGraph powerGraph) {
		super(parent, powerGraph);

		final Menu menu = getMenu();

		editMenuItem = new MenuItem(menu, SWT.PUSH);
		final String editMenuItemTextMessageKey = "lbl.menu.item.graph.edit";
		editMenuItem.setData(TEXT_MESSAGE_KEY, editMenuItemTextMessageKey);
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

		new MenuItem(menu, SWT.SEPARATOR);

		showTorqueMenuItem = addShowTorqueMenuItem();

		parent.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(final MenuDetectEvent e) {
				menu.setVisible(true);
			}
		});
	}

	public void updateTexts() {
		editMenuItem.setText(Messages.get(editMenuItem.getData(TEXT_MESSAGE_KEY).toString()));
		final MenuItem traceTypeMenuItem = traceTypeSubMenu.getMenuItem();
		traceTypeMenuItem.setText(Messages.get(traceTypeMenuItem.getData(TEXT_MESSAGE_KEY).toString()));
		final MenuItem areaAlphaMenuItem = traceTypeSubMenu.getChildren().get(0).getMenuItem();
		areaAlphaMenuItem.setText(Messages.get(areaAlphaMenuItem.getData(TEXT_MESSAGE_KEY).toString()));
		final MenuItem lineWidthMenuItem = lineWidthSubMenu.getMenuItem();
		lineWidthMenuItem.setText(Messages.get(lineWidthMenuItem.getData(TEXT_MESSAGE_KEY).toString()));
		final MenuItem pointStyleMenuItem = pointStyleSubMenu.getMenuItem();
		pointStyleMenuItem.setText(Messages.get(pointStyleMenuItem.getData(TEXT_MESSAGE_KEY).toString()));
		final MenuItem pointSizeMenuItem = pointSizeSubMenu.getMenuItem();
		pointSizeMenuItem.setText(Messages.get(pointSizeMenuItem.getData(TEXT_MESSAGE_KEY).toString()));
		showTorqueMenuItem.setText(Messages.get(showTorqueMenuItem.getData(TEXT_MESSAGE_KEY).toString()));
	}

}
