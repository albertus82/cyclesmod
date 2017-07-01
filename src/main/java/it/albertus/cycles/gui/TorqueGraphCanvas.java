package it.albertus.cycles.gui;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.resources.Messages;

public class TorqueGraphCanvas extends Canvas {

	private static final byte DEFAULT_POINT_SIZE = 4;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private static final boolean DEFAULT_AUTOSCALE = true;

	private final ContextMenu contextMenu;
	private final SimpleTorqueGraph torqueGraph;

	private static class SimpleTorqueGraph extends TorqueGraph {

		private SimpleTorqueGraph(final Bike bike) {
			super(bike);

			final Axis abscissae = getAbscissae();
			abscissae.setAutoScale(DEFAULT_AUTOSCALE);

			final Axis ordinates = getOrdinates();
			ordinates.setAutoScale(DEFAULT_AUTOSCALE);

			final Trace trace = getTrace();
			trace.setPointStyle(PointStyle.FILLED_DIAMOND);

			final IXYGraph xyGraph = getXyGraph();
			xyGraph.setTitle(Messages.get("lbl.graph.title"));
			xyGraph.setTitleFont(Display.getCurrent().getSystemFont());

			trace.setLineWidth(DEFAULT_LINE_WIDTH);
			trace.setPointSize(DEFAULT_POINT_SIZE);
		}

		private void updateTexts() {
			getXyGraph().setTitle(Messages.get("lbl.graph.title"));
			getAbscissae().setTitle(Messages.get("lbl.graph.axis.x", RPM_DIVISOR));
			getOrdinates().setTitle(Messages.get("lbl.graph.axis.y"));
		}
	}

	public TorqueGraphCanvas(final Composite parent, final Bike bike) {
		super(parent, SWT.NONE);

		final LightweightSystem lws = new LightweightSystem(this);
		torqueGraph = new SimpleTorqueGraph(bike);
		lws.setContents(torqueGraph.getXyGraph());

		setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		contextMenu = new ContextMenu(this);
	}

	public void updateTexts() {
		torqueGraph.updateTexts();
		contextMenu.updateTexts();
	}

	public ITorqueGraph getTorqueGraph() {
		return torqueGraph;
	}

	private class ContextMenu extends TorqueGraphContextMenu {

		private final MenuItem editMenuItem;
		private final SubMenu<TraceType> traceTypeSubMenu;
		private final SubMenu<Integer> lineWidthSubMenu;
		private final SubMenu<PointStyle> pointStyleSubMenu;
		private final SubMenu<Integer> pointSizeSubMenu;

		private ContextMenu(final Control control) {
			super(torqueGraph);

			final Menu menu = new Menu(control);
			control.setMenu(menu);

			editMenuItem = new MenuItem(menu, SWT.PUSH);
			final String editMenuItemTextMessageKey = "lbl.menu.item.graph.edit";
			editMenuItem.setData(DATA_KEY_MSG_KEY, editMenuItemTextMessageKey);
			editMenuItem.setText(Messages.get(editMenuItemTextMessageKey));
			editMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					control.notifyListeners(SWT.MouseDoubleClick, null);
				}
			});
			menu.setDefaultItem(editMenuItem);

			new MenuItem(menu, SWT.SEPARATOR);

			traceTypeSubMenu = addTraceTypeSubMenu(control);
			lineWidthSubMenu = addLineWidthSubMenu(control);
			pointStyleSubMenu = addPointStyleSubMenu(control);
			pointSizeSubMenu = addPointSizeSubMenu(control);

			control.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(final MenuDetectEvent e) {
					menu.setVisible(true);
				}
			});
		}

		private void updateTexts() {
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

}
