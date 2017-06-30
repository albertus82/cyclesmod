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

import it.albertus.cycles.gui.TorqueGraph.TorqueGraphContextMenu;
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

			add(xyGraph);
		}

		@Override
		protected void layout() {
			getXyGraph().setBounds(getBounds().getCopy());
			super.layout();
		}
	}

	public TorqueGraphCanvas(final Composite parent, final Bike bike) {
		super(parent, SWT.NONE);

		final LightweightSystem lws = new LightweightSystem(this);
		torqueGraph = new SimpleTorqueGraph(bike);
		lws.setContents(torqueGraph);

		setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		contextMenu = new ContextMenu(this);
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

	private class ContextMenu extends TorqueGraphContextMenu {

		private final MenuItem editMenuItem;
		private final SubMenu<TraceType> traceTypeSubMenu;
		private final SubMenu<Integer> lineWidthSubMenu;
		private final SubMenu<PointStyle> pointStyleSubMenu;
		private final SubMenu<Integer> pointSizeSubMenu;

		private ContextMenu(final Control control) {
			torqueGraph.super();
			final Menu menu = new Menu(control);
			control.setMenu(menu);

			editMenuItem = new MenuItem(menu, SWT.PUSH);
			editMenuItem.setText(Messages.get("lbl.menu.item.graph.edit"));
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

		public void updateTexts() {
			editMenuItem.setText(Messages.get("lbl.menu.item.graph.edit"));
			traceTypeSubMenu.getMenuItem().setText(Messages.get("lbl.menu.item.graph.traceType"));
			traceTypeSubMenu.getChildren().get(0).getMenuItem().setText(Messages.get("lbl.menu.item.graph.areaAlpha"));
			lineWidthSubMenu.getMenuItem().setText(Messages.get("lbl.menu.item.graph.lineWidth"));
			pointStyleSubMenu.getMenuItem().setText(Messages.get("lbl.menu.item.graph.pointStyle"));
			pointSizeSubMenu.getMenuItem().setText(Messages.get("lbl.menu.item.graph.pointSize"));
		}
	}

}
