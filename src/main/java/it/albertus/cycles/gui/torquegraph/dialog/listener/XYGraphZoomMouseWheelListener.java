package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

public class XYGraphZoomMouseWheelListener implements MouseWheelListener {

	private final IFigure figure;

	public XYGraphZoomMouseWheelListener(final IFigure figure) {
		this.figure = figure;
	}

	@Override
	public void mouseScrolled(final MouseEvent e) {
		final IFigure figureUnderMouse = figure.findFigureAt(e.x, e.y, new TreeSearch() {
			@Override
			public boolean prune(final IFigure figure) {
				return false;
			}

			@Override
			public boolean accept(final IFigure figure) {
				return figure instanceof Axis || figure instanceof PlotArea;
			}
		});
		if (figureUnderMouse instanceof Axis) {
			final Axis axis = ((Axis) figureUnderMouse);
			final double valuePosition = axis.getPositionValue(axis.isHorizontal() ? e.x : e.y, false);
			axis.zoomInOut(valuePosition, e.count * 0.1 / 3);
		}
		else if (figureUnderMouse instanceof PlotArea) {
			final PlotArea plotArea = (PlotArea) figureUnderMouse;
			plotArea.zoomInOut(true, true, e.x, e.y, e.count * 0.1 / 3);
		}
	}

}
