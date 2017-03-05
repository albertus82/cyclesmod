package it.albertus.cycles.gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cycles.data.DefaultBikes;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.engine.NumeralSystem;
import it.albertus.cycles.gui.listener.CloseListener;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Messages;
import it.albertus.cycles.resources.Messages.Language;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.IOUtils;
import it.albertus.util.StringUtils;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class CyclesModGui extends CyclesModEngine implements IShellProvider {

	private static final Logger logger = LoggerFactory.getLogger(CyclesModGui.class);

	private static final String MSG_KEY_WARNING = "msg.warning";
	private static final String MSG_KEY_COMPLETED = "msg.completed";
	private static final String MSG_KEY_WIN_TITLE = "win.title";

	private final Map<String, Integer> defaultProperties = new HashMap<String, Integer>();
	private final Map<String, Integer> lastPersistedProperties = new HashMap<String, Integer>();

	private final Shell shell;
	private final MenuBar menuBar;
	private final Tabs tabs;

	private CyclesModGui(final Display display, final String fileName) throws IOException {
		// Loading default properties...
		InputStream is = null;
		try {
			is = new DefaultBikes().getInputStream();
			setBikesInf(new BikesInf(is));
		}
		finally {
			IOUtils.closeQuietly(is);
		}
		defaultProperties.putAll(new BikesCfg(getBikesInf()).getMap());

		// Shell creation...
		shell = new Shell(display);
		shell.setImages(Images.MAIN_ICONS);
		shell.setText(Messages.get(MSG_KEY_WIN_TITLE));
		shell.setLayout(new FillLayout());
		shell.addShellListener(new CloseListener(this));

		menuBar = new MenuBar(this);

		tabs = new Tabs(this);

		// Size...
		shell.pack();

		tabs.updateFormValues();

		// Loading custom properties...
		if (StringUtils.isNotBlank(fileName)) {
			load(fileName, false);
		}
		else {
			setLastPersistedProperties(defaultProperties);
		}
	}

	/* GUI entry point. */
	public static void start(final String fileName) {
		Display.setAppName(Messages.get(MSG_KEY_WIN_TITLE));
		Display.setAppVersion(Version.getInstance().getNumber());
		final Display display = Display.getDefault();
		Shell shell = null;
		try {
			final CyclesModGui gui = new CyclesModGui(display, fileName);
			shell = gui.getShell();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.isDisposed() && !display.readAndDispatch()) {
					display.sleep();
				}
			}
		}
		catch (final Exception e) {
			final String message = e.toString();
			logger.log(Level.SEVERE, message, e);
			EnhancedErrorDialog.openError(shell != null ? shell : null, Messages.get(MSG_KEY_WARNING), message, IStatus.ERROR, e, Images.MAIN_ICONS);
		}
		finally {
			display.dispose();
		}
	}

	public void setLanguage(final Language language) {
		Messages.setLanguage(language);
		shell.setRedraw(false);
		menuBar.updateTexts();
		tabs.updateTexts();
		shell.setRedraw(true);
	}

	public void updateModelValues(boolean lenient) {
		for (final String key : tabs.getFormProperties().keySet()) {
			applyProperty(key, tabs.getFormProperties().get(key).getValue(), lenient);
		}
	}

	public void load(final String fileName, final boolean successMessage) {
		try {
			if (fileName.toLowerCase().endsWith(".inf")) {
				final File bikesInfFile = new File(fileName);
				setBikesInf(new BikesInf(bikesInfFile));
				tabs.updateFormValues();
				setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
				shell.setText(Messages.get(MSG_KEY_WIN_TITLE) + " - " + bikesInfFile.getCanonicalPath());
				if (successMessage) {
					final MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Messages.get(MSG_KEY_COMPLETED));
					messageBox.setMessage(Messages.get("msg.file.loaded", fileName));
					messageBox.open();
				}
			}
			else if (fileName.toLowerCase().endsWith(".cfg")) {
				InputStream is = null;
				try {
					is = new DefaultBikes().getInputStream();
					setBikesInf(new BikesInf(is));
				}
				finally {
					IOUtils.closeQuietly(is);
				}

				final BikesCfg bikesCfg = new BikesCfg(fileName);
				short changesCount = 0;
				for (final String key : bikesCfg.getProperties().stringPropertyNames()) {
					if (applyProperty(key, bikesCfg.getProperties().getProperty(key), false)) {
						changesCount++;
					}
				}
				tabs.updateFormValues();
				setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
				if (successMessage) {
					final MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Messages.get(MSG_KEY_COMPLETED));
					messageBox.setMessage(Messages.get("msg.customizations.applied", changesCount));
					messageBox.open();
				}
			}
			else {
				final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				messageBox.setText(Messages.get(MSG_KEY_WARNING));
				messageBox.setMessage(Messages.get("err.file.invalid"));
				messageBox.open();
			}
		}
		catch (final Exception e) {
			logger.log(Level.WARNING, e.toString(), e);
			EnhancedErrorDialog.openError(shell, Messages.get(MSG_KEY_WARNING), Messages.get("err.file.load"), IStatus.WARNING, e, Images.MAIN_ICONS);
		}
	}

	public boolean save(final boolean successMessage) {
		try {
			updateModelValues(false);
		}
		catch (final InvalidPropertyException e) {
			logger.log(Level.WARNING, e.toString(), e);
			EnhancedErrorDialog.openError(shell, Messages.get(MSG_KEY_WARNING), ExceptionUtils.getUIMessage(e), IStatus.WARNING, e, Images.MAIN_ICONS);
			return false;
		}
		final FileDialog saveDialog = new FileDialog(getShell(), SWT.SAVE);
		saveDialog.setFilterExtensions(new String[] { "*.INF;*.inf" });
		saveDialog.setFileName(BikesInf.FILE_NAME);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();

		if (StringUtils.isNotBlank(fileName)) {
			try {
				getBikesInf().write(fileName);
			}
			catch (final Exception e) {
				logger.log(Level.WARNING, e.toString(), e);
				EnhancedErrorDialog.openError(shell, Messages.get(MSG_KEY_WARNING), Messages.get("err.file.save"), IStatus.WARNING, e, Images.MAIN_ICONS);
				return false;
			}
			shell.setText(Messages.get(MSG_KEY_WIN_TITLE) + " - " + fileName);
			setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
			if (successMessage) {
				final MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION);
				messageBox.setText(Messages.get(MSG_KEY_COMPLETED));
				messageBox.setMessage(Messages.get("msg.file.saved", fileName));
				messageBox.open();
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void setNumeralSystem(final NumeralSystem numeralSystem) {
		updateModelValues(true);
		super.setNumeralSystem(numeralSystem);
		tabs.updateFormValues();
	}

	public Map<String, Integer> getLastPersistedProperties() {
		return Collections.unmodifiableMap(lastPersistedProperties);
	}

	private void setLastPersistedProperties(final Map<String, Integer> lastPersistedProperties) {
		this.lastPersistedProperties.clear();
		this.lastPersistedProperties.putAll(lastPersistedProperties);
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public Tabs getTabs() {
		return tabs;
	}

	public Map<String, Integer> getDefaultProperties() {
		return Collections.unmodifiableMap(defaultProperties);
	}

}
