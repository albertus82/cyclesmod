package io.github.albertus82.cyclesmod.gui.torquegraph.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraph;
import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraphContextMenu;

public class SimpleTorqueGraphContextMenu extends TorqueGraphContextMenu {

	public SimpleTorqueGraphContextMenu(final Control parent, final TorqueGraph powerGraph) {
		super(parent, powerGraph);

		final Menu menu = getMenu();

		final MenuItem editMenuItem = newLocalizedMenuItem(menu, SWT.PUSH, "gui.label.menu.item.graph.edit");
		editMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				parent.notifyListeners(SWT.MouseDoubleClick, null);
			}
		});
		menu.setDefaultItem(editMenuItem);

		new MenuItem(menu, SWT.SEPARATOR);

		addTraceTypeSubMenu();
		addLineWidthSubMenu();
		addPointStyleSubMenu();
		addPointSizeSubMenu();

		new MenuItem(menu, SWT.SEPARATOR);

		addShowPowerMenuItem();

		parent.addMenuDetectListener(e -> menu.setVisible(true));
	}

}
