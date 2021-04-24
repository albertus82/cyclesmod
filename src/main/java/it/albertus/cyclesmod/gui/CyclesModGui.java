package it.albertus.cyclesmod.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cyclesmod.common.data.InvalidSizeException;
import it.albertus.cyclesmod.common.engine.CyclesModEngine;
import it.albertus.cyclesmod.common.engine.InvalidNumberException;
import it.albertus.cyclesmod.common.engine.InvalidPropertyException;
import it.albertus.cyclesmod.common.engine.NumeralSystem;
import it.albertus.cyclesmod.common.engine.UnknownPropertyException;
import it.albertus.cyclesmod.common.engine.ValueOutOfRangeException;
import it.albertus.cyclesmod.common.model.BikesCfg;
import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.ConfigurableMessages;
import it.albertus.cyclesmod.common.resources.Language;
import it.albertus.cyclesmod.gui.listener.CloseListener;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.closeable.CloseableDevice;
import it.albertus.util.Version;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class CyclesModGui extends CyclesModEngine implements IShellProvider {

	private static final String GUI_LABEL_WINDOW_TITLE = "gui.label.window.title";

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private final Map<String, Integer> defaultProperties = new HashMap<>();
	private final Map<String, Integer> lastPersistedProperties = new HashMap<>();

	@Getter private final Shell shell;
	@Getter private final MenuBar menuBar;
	@Getter private final Tabs tabs;

	private String bikesInfFileName;

	private CyclesModGui(@NonNull final Display display) {
		// Loading default properties...
		setBikesInf(new BikesInf());
		defaultProperties.putAll(new BikesCfg(getBikesInf()).getMap());

		// Shell creation...
		shell = new Shell(display);
		shell.setImages(Images.getAppIconArray());
		shell.setText(messages.get(GUI_LABEL_WINDOW_TITLE));
		shell.setLayout(new FillLayout());
		shell.addShellListener(new CloseListener(this));

		menuBar = new MenuBar(this);

		tabs = new Tabs(this);

		// Size...
		shell.pack();

		tabs.updateFormValues();

		setLastPersistedProperties(defaultProperties);
	}

	/* GUI entry point. */
	public static void main(final String... args) {
		Display.setAppName(messages.get(GUI_LABEL_WINDOW_TITLE));
		Display.setAppVersion(Version.getNumber());
		Shell shell = null;
		try (final CloseableDevice<Display> cd = new CloseableDevice<>(Display.getDefault())) {
			final Display display = cd.getDevice();
			final CyclesModGui gui = new CyclesModGui(display);
			shell = gui.getShell();
			shell.open();
			// Loading custom properties...
			if (args != null && args.length > 0 && args[0] != null) {
				gui.open(args[0]);
			}
			while (!shell.isDisposed()) {
				if (!display.isDisposed() && !display.readAndDispatch()) {
					display.sleep();
				}
			}
		}
		catch (final RuntimeException | Error e) { // NOSONAR Catch Exception instead of Error. Throwable and Error should not be caught (java:S1181)
			log.log(Level.SEVERE, "An unexpected error has occurred:", e);
			EnhancedErrorDialog.openError(shell != null ? shell : null, messages.get(GUI_LABEL_WINDOW_TITLE), messages.get("gui.error.unexpected"), IStatus.ERROR, e, Images.getAppIconArray());
		}
	}

	public void setLanguage(final Language language) {
		messages.setLanguage(language);
		shell.setRedraw(false);
		menuBar.updateLanguage();
		tabs.updateLanguage();
		shell.setRedraw(true);
	}

	public void updateModelValues(final boolean lenient) throws ValueOutOfRangeException, InvalidNumberException, UnknownPropertyException {
		final Control focused = shell.getDisplay().getFocusControl();
		if (focused != null && !focused.isDisposed()) {
			focused.notifyListeners(SWT.FocusOut, null); // force auto-correction for focused field
		}
		for (final String key : tabs.getFormProperties().keySet()) {
			try {
				applyProperty(key, tabs.getFormProperties().get(key).getValue(), lenient);
			}
			catch (final ValueOutOfRangeException | InvalidNumberException | UnknownPropertyException e) {
				if (!lenient) {
					throw e;
				}
			}
		}
	}

	public void open(@NonNull final String path) {
		try {
			final Path file = Paths.get(path);
			if (file.toString().toLowerCase(Locale.ROOT).endsWith(".inf")) {
				openBikesInf(file);
			}
			else if (file.toString().toLowerCase(Locale.ROOT).endsWith(".cfg")) {
				openBikesCfg(file);
			}
			else {
				openMessageBox(messages.get("gui.error.file.open.invalid.type"), SWT.ICON_WARNING);
			}
		}
		catch (final InvalidPathException e) {
			log.log(Level.WARNING, "Cannot open file '" + path + "':", e);
			EnhancedErrorDialog.openError(shell, messages.get(GUI_LABEL_WINDOW_TITLE), messages.get("gui.error.file.open.invalid.path"), IStatus.WARNING, e, Images.getAppIconArray());
		}
		catch (final RuntimeException | IOException e) {
			log.log(Level.WARNING, "Cannot open file '" + path + "':", e);
			EnhancedErrorDialog.openError(shell, messages.get(GUI_LABEL_WINDOW_TITLE), messages.get("gui.error.file.open.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
		}
	}

	private void openBikesCfg(@NonNull final Path file) throws IOException {
		try {
			bikesInfFileName = null;
			setBikesInf(new BikesInf());
			final BikesCfg bikesCfg = new BikesCfg(file);
			for (final String key : bikesCfg.getProperties().stringPropertyNames()) {
				applyProperty(key, bikesCfg.getProperties().getProperty(key), false);
			}
			tabs.updateFormValues();
			setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
		}
		catch (final UnknownPropertyException e) {
			openMessageBox(messages.get("gui.error.file.open.unknown.property", e.getPropertyName()), SWT.ICON_WARNING);
		}
		catch (final InvalidNumberException e) {
			openMessageBox(messages.get("gui.error.file.open.invalid.number", e.getPropertyName(), e.getValue()), SWT.ICON_WARNING);
		}
		catch (final ValueOutOfRangeException e) {
			openMessageBox(messages.get("gui.error.file.open.value.out.of.range", e.getPropertyName(), e.getValue(), e.getMinValue(), e.getMaxValue()), SWT.ICON_WARNING);
		}
	}

	private void openBikesInf(@NonNull final Path file) throws IOException {
		try {
			setBikesInf(new BikesInf(file));
			tabs.updateFormValues();
			setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
			bikesInfFileName = file.toFile().getCanonicalPath();
			shell.setText(messages.get(GUI_LABEL_WINDOW_TITLE) + " - " + bikesInfFileName);
		}
		catch (final InvalidSizeException e) {
			openMessageBox(messages.get("gui.error.file.open.invalid.size"), SWT.ICON_WARNING);
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
				log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
				EnhancedErrorDialog.openError(shell, messages.get(GUI_LABEL_WINDOW_TITLE), messages.get("gui.error.file.save.invalid.property", e.getPropertyName()), IStatus.WARNING, e, Images.getAppIconArray());
				return false;
			}
			try {
				Files.write(Paths.get(bikesInfFileName), getBikesInf().toByteArray());
				setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
				return true;
			}
			catch (final IOException | RuntimeException e) {
				log.log(Level.WARNING, "Cannot save file:", e);
				EnhancedErrorDialog.openError(shell, messages.get(GUI_LABEL_WINDOW_TITLE), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
				return false;
			}
		}
	}

	public boolean saveAs() {
		try {
			updateModelValues(false);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
			EnhancedErrorDialog.openError(shell, messages.get(GUI_LABEL_WINDOW_TITLE), messages.get("gui.error.file.save.invalid.property", e.getPropertyName()), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		final FileDialog saveDialog = new FileDialog(getShell(), SWT.SAVE);
		saveDialog.setFilterExtensions(new String[] { "*.INF;*.inf" });
		saveDialog.setFileName(BikesInf.FILE_NAME);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();

		if (fileName != null && !fileName.trim().isEmpty()) {
			try {
				Files.write(Paths.get(fileName), getBikesInf().toByteArray());
				bikesInfFileName = fileName;
				shell.setText(messages.get(GUI_LABEL_WINDOW_TITLE) + " - " + bikesInfFileName);
				setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
				return true;
			}
			catch (final IOException | RuntimeException e) {
				log.log(Level.WARNING, "Cannot save file as '" + fileName + "':", e);
				EnhancedErrorDialog.openError(shell, messages.get(GUI_LABEL_WINDOW_TITLE), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
				return false;
			}
		}
		else {
			return false;
		}
	}

	private void openMessageBox(@NonNull final String message, final int style) {
		final MessageBox messageBox = new MessageBox(shell, style);
		messageBox.setText(messages.get(GUI_LABEL_WINDOW_TITLE));
		messageBox.setMessage(message);
		messageBox.open();
	}

	@Override
	public void setNumeralSystem(final NumeralSystem numeralSystem) {
		try {
			updateModelValues(true);
		}
		catch (final ValueOutOfRangeException | InvalidNumberException | UnknownPropertyException e) {
			log.log(Level.INFO, e.getMessage(), e);
		}
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

	public Map<String, Integer> getDefaultProperties() {
		return Collections.unmodifiableMap(defaultProperties);
	}

}
