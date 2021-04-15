package it.albertus.cyclesmod.gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cyclesmod.common.data.DefaultBikes;
import it.albertus.cyclesmod.common.engine.CyclesModEngine;
import it.albertus.cyclesmod.common.engine.InvalidPropertyException;
import it.albertus.cyclesmod.common.engine.NumeralSystem;
import it.albertus.cyclesmod.common.model.BikesCfg;
import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.ConfigurableMessages;
import it.albertus.cyclesmod.common.resources.Language;
import it.albertus.cyclesmod.gui.listener.CloseListener;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.IOUtils;
import it.albertus.util.Version;
import lombok.extern.java.Log;

@Log
public class CyclesModGui extends CyclesModEngine implements IShellProvider {

	private static final String MSG_KEY_WARNING = "gui.message.warning";
	private static final String MSG_KEY_WIN_TITLE = "gui.label.window.title";

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private final Map<String, Integer> defaultProperties = new HashMap<>();
	private final Map<String, Integer> lastPersistedProperties = new HashMap<>();

	private final Shell shell;
	private final MenuBar menuBar;
	private final Tabs tabs;

	private String bikesInfFileName;

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
		shell.setImages(Images.getAppIconArray());
		shell.setText(messages.get(MSG_KEY_WIN_TITLE));
		shell.setLayout(new FillLayout());
		shell.addShellListener(new CloseListener(this));

		menuBar = new MenuBar(this);

		tabs = new Tabs(this);

		// Size...
		shell.pack();

		tabs.updateFormValues();

		setLastPersistedProperties(defaultProperties);

		// Loading custom properties...
		if (fileName != null && !fileName.trim().isEmpty()) {
			open(fileName);
		}
	}

	/* GUI entry point. */
	public static void main(final String fileName) {
		Display.setAppName(messages.get(MSG_KEY_WIN_TITLE));
		Display.setAppVersion(Version.getNumber());
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
			log.log(Level.SEVERE, message, e);
			EnhancedErrorDialog.openError(shell != null ? shell : null, messages.get(MSG_KEY_WARNING), message, IStatus.ERROR, e, Images.getAppIconArray());
		}
		finally {
			display.dispose();
		}
	}

	public void setLanguage(final Language language) {
		messages.setLanguage(language);
		shell.setRedraw(false);
		menuBar.updateLanguage();
		tabs.updateLanguage();
		shell.setRedraw(true);
	}

	public void updateModelValues(boolean lenient) {
		for (final String key : tabs.getFormProperties().keySet()) {
			applyProperty(key, tabs.getFormProperties().get(key).getValue(), lenient);
		}
	}

	public void open(final String fileName) {
		try {
			if (fileName.toLowerCase(Locale.ROOT).endsWith(".inf")) {
				final File bikesInfFile = new File(fileName);
				setBikesInf(new BikesInf(bikesInfFile));
				tabs.updateFormValues();
				setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
				bikesInfFileName = bikesInfFile.getCanonicalPath();
				shell.setText(messages.get(MSG_KEY_WIN_TITLE) + " - " + bikesInfFileName);
			}
			else if (fileName.toLowerCase(Locale.ROOT).endsWith(".cfg")) {
				bikesInfFileName = null;
				InputStream is = null;
				try {
					is = new DefaultBikes().getInputStream();
					setBikesInf(new BikesInf(is));
				}
				finally {
					IOUtils.closeQuietly(is);
				}

				final BikesCfg bikesCfg = new BikesCfg(fileName);
				for (final String key : bikesCfg.getProperties().stringPropertyNames()) {
					applyProperty(key, bikesCfg.getProperties().getProperty(key), false);
				}
				tabs.updateFormValues();
				setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
			}
			else {
				final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				messageBox.setText(messages.get(MSG_KEY_WARNING));
				messageBox.setMessage(messages.get("gui.error.file.invalid"));
				messageBox.open();
			}
		}
		catch (final Exception e) {
			log.log(Level.WARNING, e.toString(), e);
			EnhancedErrorDialog.openError(shell, messages.get(MSG_KEY_WARNING), messages.get("gui.error.file.load"), IStatus.WARNING, e, Images.getAppIconArray());
		}
	}

	public boolean save() {
		if (bikesInfFileName == null) {
			return saveAs();
		}
		else {
			final File bikesInfFile = new File(bikesInfFileName);
			if (bikesInfFile.exists() && !bikesInfFile.canWrite()) {
				return saveAs();
			}
			try {
				updateModelValues(false);
			}
			catch (final InvalidPropertyException e) {
				log.log(Level.WARNING, e.toString(), e);
				EnhancedErrorDialog.openError(shell, messages.get(MSG_KEY_WARNING), ExceptionUtils.getUIMessage(e), IStatus.WARNING, e, Images.getAppIconArray());
				return false;
			}
			try {
				getBikesInf().write(bikesInfFileName, false);
			}
			catch (final Exception e) {
				log.log(Level.WARNING, e.toString(), e);
				EnhancedErrorDialog.openError(shell, messages.get(MSG_KEY_WARNING), messages.get("gui.error.file.save"), IStatus.WARNING, e, Images.getAppIconArray());
				return false;
			}
			setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
			return true;
		}
	}

	public boolean saveAs() {
		try {
			updateModelValues(false);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, e.toString(), e);
			EnhancedErrorDialog.openError(shell, messages.get(MSG_KEY_WARNING), ExceptionUtils.getUIMessage(e), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		final FileDialog saveDialog = new FileDialog(getShell(), SWT.SAVE);
		saveDialog.setFilterExtensions(new String[] { "*.INF;*.inf" });
		saveDialog.setFileName(BikesInf.FILE_NAME);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();

		if (fileName != null && !fileName.trim().isEmpty()) {
			try {
				getBikesInf().write(fileName, false);
			}
			catch (final Exception e) {
				log.log(Level.WARNING, e.toString(), e);
				EnhancedErrorDialog.openError(shell, messages.get(MSG_KEY_WARNING), messages.get("gui.error.file.save"), IStatus.WARNING, e, Images.getAppIconArray());
				return false;
			}
			bikesInfFileName = fileName;
			shell.setText(messages.get(MSG_KEY_WIN_TITLE) + " - " + bikesInfFileName);
			setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
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
