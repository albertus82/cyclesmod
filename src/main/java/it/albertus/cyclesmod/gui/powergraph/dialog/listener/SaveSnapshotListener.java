package it.albertus.cyclesmod.gui.powergraph.dialog.listener;

import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.jface.SwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveSnapshotListener implements KeyListener, SelectionListener {

	private static final Messages messages = GuiMessages.INSTANCE;

	@NonNull private final Shell shell;
	@NonNull private final IXYGraph xyGraph;

	@Override
	public void keyPressed(@NonNull final KeyEvent e) {
		if (SWT.MOD1 == e.stateMask && SwtUtils.KEY_SAVE == e.keyCode) {
			execute();
		}
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		execute();
	}

	private void execute() {
		final ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { xyGraph.getImage().getImageData() };
		final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterNames(new String[] { "Portable Network Graphics (*.png)", messages.get("gui.label.graph.save.allFiles", "(*.*)") });
		dialog.setFilterExtensions(new String[] { "*.PNG;*.png", "*.*" }); // Windows
		final String path = dialog.open();
		if ((path != null) && !path.isEmpty()) {
			loader.save(path, SWT.IMAGE_PNG);
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {/* Ignore */}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

}
