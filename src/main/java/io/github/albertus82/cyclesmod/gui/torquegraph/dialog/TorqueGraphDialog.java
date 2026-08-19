package io.github.albertus82.cyclesmod.gui.torquegraph.dialog;

import java.util.Map;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import io.github.albertus82.cyclesmod.common.model.VehicleType;
import io.github.albertus82.cyclesmod.common.resources.Messages;
import io.github.albertus82.cyclesmod.gui.Images;
import io.github.albertus82.cyclesmod.gui.Mode;
import io.github.albertus82.cyclesmod.gui.resources.GuiMessages;
import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraphProvider;
import io.github.albertus82.cyclesmod.gui.torquegraph.dialog.listener.RedoListener;
import io.github.albertus82.cyclesmod.gui.torquegraph.dialog.listener.SaveSnapshotListener;
import io.github.albertus82.cyclesmod.gui.torquegraph.dialog.listener.UndoListener;
import io.github.albertus82.cyclesmod.gui.torquegraph.dialog.listener.ZoomInListener;
import io.github.albertus82.cyclesmod.gui.torquegraph.dialog.listener.ZoomMouseWheelListener;
import io.github.albertus82.cyclesmod.gui.torquegraph.dialog.listener.ZoomOutListener;
import io.github.albertus82.jface.SwtUtils;
import lombok.Getter;
import lombok.NonNull;

public class TorqueGraphDialog extends Dialog implements TorqueGraphProvider {

	private static final int SHELL_SIZE_FACTOR = 3;

	private static final Messages messages = GuiMessages.INSTANCE;

	private final Mode mode;

	private int returnCode = SWT.CANCEL;

	@Getter
	private ComplexTorqueGraph graph;

	public TorqueGraphDialog(@NonNull final Shell parent, @NonNull final Mode mode) {
		super(parent, SWT.SHEET | SWT.RESIZE | SWT.MAX);
		this.mode = mode;
	}

	public int open(@NonNull final Map<Integer, Short> map, @NonNull final VehicleType vehicleType, final boolean powerVisible) {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(messages.get("gui.label.graph.dialog.title.torque.power", vehicleType.getDescription(mode.getGame())));
		shell.setImages(Images.getAppIconArray());
		GridLayoutFactory.swtDefaults().applyTo(shell);
		createContents(shell, map, vehicleType, powerVisible);
		final Point minimumSize = getMinimumSize(shell);
		shell.setSize(getSize(shell));
		shell.setMinimumSize(minimumSize);
		shell.open();
		final Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return returnCode;
	}

	private Point getSize(final Shell shell) {
		final Point normalShellSize = shell.getSize();
		final Point packedShellSize = getMinimumSize(shell);
		return new Point(Math.min(packedShellSize.x * SHELL_SIZE_FACTOR, normalShellSize.x), Math.min(packedShellSize.y * SHELL_SIZE_FACTOR, normalShellSize.y));
	}

	private Point getMinimumSize(final Shell shell) {
		return shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}

	private void createContents(final Shell shell, final Map<Integer, Short> map, final VehicleType vehicleType, final boolean powerVisible) {
		createGraph(shell, map, vehicleType, powerVisible);
		createButtonBox(shell);
	}

	private void createGraph(final Shell shell, final Map<Integer, Short> map, final VehicleType vehicleType, final boolean powerVisible) {
		final Canvas canvas = new Canvas(shell, SWT.NONE);

		final LightweightSystem lws = new LightweightSystem(canvas);
		graph = new ComplexTorqueGraph(map, mode, vehicleType, shell);
		final ComplexTorqueGraphContextMenu menu = new ComplexTorqueGraphContextMenu(canvas, graph);
		if (powerVisible != graph.isPowerVisible()) {
			graph.togglePowerVisibility();
			menu.getShowTorqueMenuItem().setSelection(graph.isPowerVisible());
		}
		lws.setContents(graph.getToolbarArmedXYGraph());

		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);

		final IXYGraph xyGraph = graph.getXyGraph();
		canvas.addKeyListener(new ZoomInListener(xyGraph));
		canvas.addKeyListener(new ZoomOutListener(xyGraph));
		canvas.addKeyListener(new UndoListener(xyGraph.getOperationsManager()));
		canvas.addKeyListener(new RedoListener(xyGraph.getOperationsManager()));
		canvas.addKeyListener(new SaveSnapshotListener(shell, xyGraph));

		canvas.addMouseWheelListener(new ZoomMouseWheelListener(xyGraph));
	}

	private Composite createButtonBox(final Shell shell) {
		final Composite buttonComposite = new Composite(shell, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(buttonComposite);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(buttonComposite);

		final Button okButton = new Button(buttonComposite, SWT.PUSH);
		okButton.setText(messages.get("gui.label.button.ok"));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).minSize(SwtUtils.convertHorizontalDLUsToPixels(okButton, IDialogConstants.BUTTON_WIDTH), SWT.DEFAULT).applyTo(okButton);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				returnCode = SWT.OK;
				shell.close();
			}
		});

		final Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText(messages.get("gui.label.button.cancel"));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).minSize(SwtUtils.convertHorizontalDLUsToPixels(cancelButton, IDialogConstants.BUTTON_WIDTH), SWT.DEFAULT).applyTo(cancelButton);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				returnCode = SWT.CANCEL;
				shell.close();
			}
		});

		shell.setDefaultButton(okButton);
		return buttonComposite;
	}

	public int getReturnCode() {
		return returnCode;
	}

}
