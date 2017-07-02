package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.ZoomCommand;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

import it.albertus.cycles.resources.Messages;

public class ZoomMouseWheelListener implements MouseWheelListener {

	private static final double ZOOM_RATIO = 0.1;
	private static final int DIVISOR = 3;

	private final IXYGraph xyGraph;

	public ZoomMouseWheelListener(final IXYGraph xyGraph) {
		this.xyGraph = xyGraph;
	}

	@Override
	public void mouseScrolled(final MouseEvent e) {
		final IFigure figureUnderMouse = xyGraph.findFigureAt(e.x, e.y, new TreeSearch() {
			@Override
			public boolean prune(final IFigure figure) {
				return false;
			}

			@Override
			public boolean accept(final IFigure figure) {
				return figure instanceof PlotArea || figure instanceof Axis;
			}
		});

		if (figureUnderMouse != null) {
			final String zoomTypeDescription = Messages.get(e.count > 0 ? "lbl.graph.action.zoomIn" : "lbl.graph.action.zoomOut");
			final SaveStateCommand command = new ZoomCommand(zoomTypeDescription, xyGraph.getXAxisList(), xyGraph.getYAxisList());

			if (figureUnderMouse instanceof PlotArea) {
				final PlotArea plotArea = (PlotArea) figureUnderMouse;
				plotArea.zoomInOut(true, true, e.x, e.y, e.count * ZOOM_RATIO / DIVISOR);
			}
			else {
				final Axis axis = ((Axis) figureUnderMouse);
				final double valuePosition = axis.getPositionValue(axis.isHorizontal() ? e.x : e.y, false);
				axis.zoomInOut(valuePosition, e.count * ZOOM_RATIO / DIVISOR);
			}

			command.saveState();
			xyGraph.getOperationsManager().addCommand(command);
		}
	}

}
