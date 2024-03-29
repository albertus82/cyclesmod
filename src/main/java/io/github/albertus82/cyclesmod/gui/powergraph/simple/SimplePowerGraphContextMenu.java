package io.github.albertus82.cyclesmod.gui.powergraph.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import io.github.albertus82.cyclesmod.gui.powergraph.IPowerGraph;
import io.github.albertus82.cyclesmod.gui.powergraph.PowerGraphContextMenu;

public class SimplePowerGraphContextMenu extends PowerGraphContextMenu {

	public SimplePowerGraphContextMenu(final Control parent, final IPowerGraph powerGraph) {
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

		addShowTorqueMenuItem();

		parent.addMenuDetectListener(e -> menu.setVisible(true));
	}

}
