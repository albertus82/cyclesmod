package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.ZoomCommand;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

public class XYGraphZoomMouseWheelListener implements MouseWheelListener {

	private static final double ZOOM_RATIO = 0.1;
	private static final int DIVISOR = 3;

	private final IXYGraph xyGraph;

	public XYGraphZoomMouseWheelListener(final IXYGraph xyGraph) {
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

		SaveStateCommand command = null;

		if (figureUnderMouse instanceof PlotArea) {
			final PlotArea plotArea = (PlotArea) figureUnderMouse;
			command = createCommand(e);
			plotArea.zoomInOut(true, true, e.x, e.y, e.count * ZOOM_RATIO / DIVISOR);
		}
		else if (figureUnderMouse instanceof Axis) {
			final Axis axis = ((Axis) figureUnderMouse);
			final double valuePosition = axis.getPositionValue(axis.isHorizontal() ? e.x : e.y, false);
			command = createCommand(e);
			axis.zoomInOut(valuePosition, e.count * ZOOM_RATIO / DIVISOR);
		}

		if (command != null) {
			command.saveState();
			xyGraph.getOperationsManager().addCommand(command);
		}
	}

	private SaveStateCommand createCommand(final MouseEvent e) {
		final ZoomType zoomType = e.count > 0 ? ZoomType.ZOOM_IN : ZoomType.ZOOM_OUT;
		return new ZoomCommand(zoomType.getDescription(), xyGraph.getXAxisList(), xyGraph.getYAxisList());
	}

}
