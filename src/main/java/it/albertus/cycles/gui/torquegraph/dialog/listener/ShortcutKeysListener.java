package it.albertus.cycles.gui.torquegraph.dialog.listener;

import org.eclipse.nebula.visualization.internal.xygraph.undo.SaveStateCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.ZoomCommand;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cycles.gui.torquegraph.dialog.ComplexTorqueGraph;
import it.albertus.jface.SwtUtils;

public class ShortcutKeysListener implements KeyListener {

	private static final double ZOOM_RATIO = 0.1;

	private final Shell shell;
	private final ComplexTorqueGraph torqueGraph;

	public ShortcutKeysListener(final Shell shell, final ComplexTorqueGraph torqueGraph) {
		this.shell = shell;
		this.torqueGraph = torqueGraph;
	}

	@Override
	public void keyPressed(final KeyEvent ke) {
		final IXYGraph xyGraph = torqueGraph.getXyGraph();
		if (SWT.MOD1 == ke.stateMask) { // CTRL/Cmd
			if (SwtUtils.KEY_UNDO == ke.keyCode) {
				xyGraph.getOperationsManager().undo();
			}
			else if (SwtUtils.KEY_REDO == ke.keyCode) {
				xyGraph.getOperationsManager().redo();
			}
			else if (SwtUtils.KEY_SAVE == ke.keyCode) {
				torqueGraph.saveSnapshot(shell);
			}
		}
		else {
			final PlotArea plotArea = xyGraph.getPlotArea();
			if ('+' == ke.keyCode || SWT.KEYPAD_ADD == ke.keyCode) {
				final SaveStateCommand command = new ZoomCommand(ZoomType.ZOOM_IN.getDescription(), xyGraph.getXAxisList(), xyGraph.getYAxisList());
				plotArea.zoomInOut(true, true, getX(plotArea), getY(plotArea), ZOOM_RATIO);
				afterZoom(command);
			}
			else if ('-' == ke.keyCode || SWT.KEYPAD_SUBTRACT == ke.keyCode) {
				final SaveStateCommand command = new ZoomCommand(ZoomType.ZOOM_OUT.getDescription(), xyGraph.getXAxisList(), xyGraph.getYAxisList());
				plotArea.zoomInOut(true, true, getX(plotArea), getY(plotArea), ZOOM_RATIO * -1);
				afterZoom(command);
			}
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {/* Ignore */}

	private void afterZoom(final SaveStateCommand command) {
		command.saveState();
		torqueGraph.getXyGraph().getOperationsManager().addCommand(command);
	}

	private static int getX(final PlotArea plotArea) {
		return plotArea.getSize().width / 2 + plotArea.getLocation().x;
	}

	private static int getY(final PlotArea plotArea) {
		return plotArea.getSize().height / 2 + plotArea.getLocation().y;
	}

}
