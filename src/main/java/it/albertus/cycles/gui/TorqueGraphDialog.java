package it.albertus.cycles.gui;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cycles.resources.Messages;
import it.albertus.jface.JFaceMessages;
import it.albertus.jface.SwtUtils;
import it.albertus.util.logging.LoggerFactory;

public class TorqueGraphDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(TorqueGraphDialog.class);

	private static final byte[] POINT_SIZE_OPTIONS = { 0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
	private static final byte[] LINE_WIDTH_OPTIONS = { 1, 2, 3, 4, 5, 6 };

	private static final byte DEFAULT_POINT_SIZE = 6;
	private static final byte DEFAULT_LINE_WIDTH = 2;

	private int returnCode = SWT.CANCEL;
	private Canvas canvas;
	private TorqueGraph torqueGraph;
	private ContextMenu contextMenu;

	public TorqueGraphDialog(final Shell parent) {
		this(parent, SWT.SHEET | SWT.RESIZE | SWT.MAX);
	}

	public TorqueGraphDialog(final Shell parent, final int style) {
		super(parent, style);
		setText(Messages.get("lbl.graph.title"));
	}

	public int open(final Map<Double, Double> values, final Color traceColor) {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setImages(Images.MAIN_ICONS);
		createContents(shell, values, traceColor);
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

	private void createContents(final Shell shell, final Map<Double, Double> values, final Color traceColor) {
		shell.setLayout(getLayout());
		createGraph(shell, values, traceColor);
		createButtonBox(shell);
	}

	private Layout getLayout() {
		return GridLayoutFactory.swtDefaults().create();
	}

	private void createGraph(final Shell shell, final Map<Double, Double> values, final Color traceColor) {
		canvas = new Canvas(shell, SWT.NULL);
		final LightweightSystem lws = new LightweightSystem(canvas);
		torqueGraph = new TorqueGraph(values, traceColor, DEFAULT_LINE_WIDTH, DEFAULT_POINT_SIZE);
		lws.setContents(torqueGraph);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);

		contextMenu = new ContextMenu(canvas);
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
			public void widgetSelected(final SelectionEvent event) {
				try {
					returnCode = SWT.OK;
				}
				catch (final SWTException se) {
					logger.log(Level.FINE, se.toString(), se);
				}
				catch (final Exception e) {
					logger.log(Level.SEVERE, e.toString(), e);
				}
				finally {
					shell.close();
				}
			}
		});

		final Button cancelButton = new Button(buttonComposite, SWT.PUSH);
		cancelButton.setText(JFaceMessages.get("lbl.button.cancel"));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).minSize(SwtUtils.convertHorizontalDLUsToPixels(cancelButton, IDialogConstants.BUTTON_WIDTH), SWT.DEFAULT).applyTo(cancelButton);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent se) {
				shell.close();
			}
		});

		shell.setDefaultButton(okButton);
		return buttonComposite;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public TorqueGraph getTorqueGraph() {
		return torqueGraph;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public ContextMenu getContextMenu() {
		return contextMenu;
	}

	class ContextMenu {

		private final Menu menu;
		private final MenuItem autoScaleMenuItem;
		private final MenuItem performAutoScaleMenuItem;
		private final Menu lineWidthSubMenu;
		private final MenuItem lineWidthMenuItem;
		private final Menu pointSizeSubMenu;
		private final MenuItem pointSizeMenuItem;

		public ContextMenu(final Control control) {
			menu = new Menu(control);
			control.setMenu(menu);

			autoScaleMenuItem = new MenuItem(menu, SWT.CHECK);
			autoScaleMenuItem.setSelection(TorqueGraph.DEFAULT_AUTOSCALE);
			autoScaleMenuItem.setText(Messages.get("lbl.menu.item.autoscaling"));
			autoScaleMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (autoScaleMenuItem.getSelection()) {
						torqueGraph.getAbscissae().setAutoScale(true);
						torqueGraph.getOrdinates().setAutoScale(true);
					}
					else {
						torqueGraph.getAbscissae().setAutoScale(false);
						torqueGraph.getOrdinates().setAutoScale(false);
					}
				}
			});

			performAutoScaleMenuItem = new MenuItem(menu, SWT.PUSH);
			performAutoScaleMenuItem.setText(Messages.get("lbl.menu.item.autoscale.now"));
			performAutoScaleMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					torqueGraph.getAbscissae().performAutoScale(true);
					torqueGraph.getOrdinates().performAutoScale(true);
				}
			});

			lineWidthMenuItem = new MenuItem(menu, SWT.CASCADE);
			lineWidthMenuItem.setText(Messages.get("lbl.menu.item.line.width"));

			lineWidthSubMenu = new Menu(lineWidthMenuItem);
			lineWidthMenuItem.setMenu(lineWidthSubMenu);

			for (final byte lineWidth : LINE_WIDTH_OPTIONS) {
				final MenuItem menuItem = new MenuItem(lineWidthSubMenu, SWT.RADIO);
				menuItem.setText("&" + lineWidth);
				if (lineWidth == DEFAULT_LINE_WIDTH) {
					menuItem.setSelection(true);
					lineWidthSubMenu.setDefaultItem(menuItem);
				}
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						torqueGraph.getTrace().setLineWidth(lineWidth);
					}
				});
			}

			pointSizeMenuItem = new MenuItem(menu, SWT.CASCADE);
			pointSizeMenuItem.setText(Messages.get("lbl.menu.item.point.size"));

			pointSizeSubMenu = new Menu(pointSizeMenuItem);
			pointSizeMenuItem.setMenu(pointSizeSubMenu);

			for (final byte pointSize : POINT_SIZE_OPTIONS) {
				final MenuItem menuItem = new MenuItem(pointSizeSubMenu, SWT.RADIO);
				menuItem.setText("&" + pointSize);
				if (pointSize == DEFAULT_POINT_SIZE) {
					menuItem.setSelection(true);
					pointSizeSubMenu.setDefaultItem(menuItem);
				}
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						torqueGraph.getTrace().setPointSize(pointSize);
					}
				});
			}

			control.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(final MenuDetectEvent e) {
					menu.setVisible(true);
				}
			});
		}

		public Menu getMenu() {
			return menu;
		}

		public MenuItem getAutoScaleMenuItem() {
			return autoScaleMenuItem;
		}

		public MenuItem getPerformAutoScaleMenuItem() {
			return performAutoScaleMenuItem;
		}

		public Menu getLineWidthSubMenu() {
			return lineWidthSubMenu;
		}

		public MenuItem getLineWidthMenuItem() {
			return lineWidthMenuItem;
		}

		public Menu getPointSizeSubMenu() {
			return pointSizeSubMenu;
		}

		public MenuItem getPointSizeMenuItem() {
			return pointSizeMenuItem;
		}

	}

}
