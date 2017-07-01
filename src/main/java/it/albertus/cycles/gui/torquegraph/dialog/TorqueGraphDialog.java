package it.albertus.cycles.gui.torquegraph.dialog;

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

import it.albertus.cycles.gui.Images;
import it.albertus.cycles.gui.torquegraph.TorqueGraphProvider;
import it.albertus.cycles.gui.torquegraph.dialog.listener.RedoListener;
import it.albertus.cycles.gui.torquegraph.dialog.listener.SaveSnapshotListener;
import it.albertus.cycles.gui.torquegraph.dialog.listener.UndoListener;
import it.albertus.cycles.gui.torquegraph.dialog.listener.ZoomInListener;
import it.albertus.cycles.gui.torquegraph.dialog.listener.ZoomMouseWheelListener;
import it.albertus.cycles.gui.torquegraph.dialog.listener.ZoomOutListener;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.resources.Messages;
import it.albertus.jface.JFaceMessages;
import it.albertus.jface.SwtUtils;

public class TorqueGraphDialog extends Dialog implements TorqueGraphProvider {

	private int returnCode = SWT.CANCEL;
	private ComplexTorqueGraph torqueGraph;

	public TorqueGraphDialog(final Shell parent) {
		this(parent, SWT.SHEET | SWT.RESIZE | SWT.MAX);
	}

	private TorqueGraphDialog(final Shell parent, final int style) {
		super(parent, style);
		setText(Messages.get("lbl.graph.title"));
	}

	public int open(final Map<Integer, Short> map, final BikeType bikeType) {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText() + " - " + bikeType.getDisplacement() + " cc");
		shell.setImages(Images.getMainIcons());
		GridLayoutFactory.swtDefaults().applyTo(shell);
		createContents(shell, map, bikeType);
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
		return new Point(Math.min(packedShellSize.x * 3, normalShellSize.x), Math.min(packedShellSize.y * 3, normalShellSize.y));
	}

	private Point getMinimumSize(final Shell shell) {
		return shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}

	private void createContents(final Shell shell, final Map<Integer, Short> map, final BikeType bikeType) {
		createGraph(shell, map, bikeType);
		createButtonBox(shell);
	}

	private void createGraph(final Shell shell, final Map<Integer, Short> map, final BikeType bikeType) {
		final Canvas canvas = new Canvas(shell, SWT.NONE);

		final LightweightSystem lws = new LightweightSystem(canvas);
		torqueGraph = new ComplexTorqueGraph(map, bikeType);
		lws.setContents(torqueGraph.getToolbarArmedXYGraph());

		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);

		final IXYGraph xyGraph = torqueGraph.getXyGraph();
		canvas.addKeyListener(new ZoomInListener(xyGraph));
		canvas.addKeyListener(new ZoomOutListener(xyGraph));
		canvas.addKeyListener(new UndoListener(xyGraph.getOperationsManager()));
		canvas.addKeyListener(new RedoListener(xyGraph.getOperationsManager()));
		canvas.addKeyListener(new SaveSnapshotListener(shell, xyGraph));

		canvas.addMouseWheelListener(new ZoomMouseWheelListener(xyGraph));

		new ComplexTorqueGraphContextMenu(canvas, torqueGraph);
	}

	private Composite createButtonBox(final Shell shell) {
		final Composite buttonComposite = new Composite(shell, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(buttonComposite);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(buttonComposite);

		final Button okButton = new Button(buttonComposite, SWT.PUSH);
		okButton.setText(JFaceMessages.get("lbl.button.ok"));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).minSize(SwtUtils.convertHorizontalDLUsToPixels(okButton, IDialogConstants.BUTTON_WIDTH), SWT.DEFAULT).applyTo(okButton);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				returnCode = SWT.OK;
				shell.close();
			}
		});

		final Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText(JFaceMessages.get("lbl.button.cancel"));
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

	@Override
	public ComplexTorqueGraph getTorqueGraph() {
		return torqueGraph;
	}

}
