package it.albertus.cyclesmod.gui;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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

import it.albertus.cyclesmod.common.data.HiddenBike;
import it.albertus.cyclesmod.common.data.InvalidSizeException;
import it.albertus.cyclesmod.common.engine.CyclesModEngine;
import it.albertus.cyclesmod.common.engine.InvalidNumberException;
import it.albertus.cyclesmod.common.engine.InvalidPropertyException;
import it.albertus.cyclesmod.common.engine.NumeralSystem;
import it.albertus.cyclesmod.common.engine.UnknownPropertyException;
import it.albertus.cyclesmod.common.engine.ValueOutOfRangeException;
import it.albertus.cyclesmod.common.model.Bike;
import it.albertus.cyclesmod.common.model.BikeType;
import it.albertus.cyclesmod.common.model.BikesCfg;
import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.ConfigurableMessages;
import it.albertus.cyclesmod.common.resources.Language;
import it.albertus.cyclesmod.gui.listener.CloseListener;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.closeable.CloseableDevice;
import it.albertus.util.Version;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class CyclesModGui implements IShellProvider, Multilanguage {

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private final CyclesModEngine engine = new CyclesModEngine(new BikesInf());

	@Getter private final Shell shell;
	@Getter private final MenuBar menuBar;
	@Getter private final Tabs tabs;

	@Getter private final Map<String, Integer> defaultProperties = Collections.unmodifiableMap(new BikesCfg(engine.getBikesInf()).getMap());
	private final Map<String, Integer> lastPersistedProperties = new HashMap<>(defaultProperties);

	private String currentFileName;

	private CyclesModGui(@NonNull final Display display) {
		// Shell creation...
		shell = new Shell(display);
		shell.setImages(Images.getAppIconArray());
		shell.setText(getWindowTitle());
		shell.setLayout(new FillLayout());
		shell.addShellListener(new CloseListener(this));

		menuBar = new MenuBar(this);

		tabs = new Tabs(this);

		// Size...
		shell.pack();

		tabs.updateFormValues();
	}

	/* GUI entry point. */
	public static void main(final String... args) {
		Display.setAppName(getWindowTitle());
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
			EnhancedErrorDialog.openError(shell != null ? shell : null, getWindowTitle(), messages.get("gui.error.unexpected"), IStatus.ERROR, e, Images.getAppIconArray());
		}
	}

	public void setLanguage(final Language language) {
		messages.setLanguage(language);
		updateLanguage();
	}

	@Override
	public void updateLanguage() {
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		try {
			menuBar.updateLanguage();
			tabs.updateLanguage();
		}
		finally {
			shell.setCursor(null);
		}
	}

	public void updateModelValues(final boolean lenient) throws InvalidPropertyException {
		final Control focused = shell.getDisplay().getFocusControl();
		if (focused != null && !focused.isDisposed()) {
			focused.notifyListeners(SWT.FocusOut, null); // force auto-correction for focused field
		}
		for (final String key : tabs.getFormProperties().keySet()) {
			try {
				engine.applyProperty(key, tabs.getFormProperties().get(key).getValue());
			}
			catch (final InvalidPropertyException e) {
				if (!lenient) {
					throw e;
				}
				else {
					log.log(Level.FINE, e, () -> "Invalid property \"" + e.getPropertyName() + "\":");
				}
			}
		}
	}

	public boolean open() {
		if (!askForSaving(messages.get("gui.label.window.title"), messages.get("gui.message.confirm.open.message"))) {
			return false;
		}
		final FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
		openDialog.setFilterExtensions(new String[] { "*.INF;*.inf;*.CFG;*.cfg" });
		final String fileName = openDialog.open();
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		return open(fileName);
	}

	private boolean open(@NonNull final String path) {
		try {
			final Path file = Paths.get(path);
			if (file.toString().toUpperCase(Locale.ROOT).endsWith(".INF")) {
				return openBikesInf(file);
			}
			else if (file.toString().toUpperCase(Locale.ROOT).endsWith(".CFG")) {
				return openBikesCfg(file);
			}
			else {
				openMessageBox(messages.get("gui.error.file.open.invalid.type"), SWT.ICON_WARNING);
				return false;
			}
		}
		catch (final InvalidPathException e) {
			log.log(Level.WARNING, "Cannot open file '" + path + "':", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.open.invalid.path"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		catch (final RuntimeException | IOException e) {
			log.log(Level.WARNING, "Cannot open file '" + path + "':", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.open.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	private boolean openBikesCfg(@NonNull final Path file) throws IOException {
		try {
			currentFileName = null;
			engine.setBikesInf(new BikesInf());
			final BikesCfg bikesCfg = new BikesCfg(file);
			int count = 0;
			for (final String key : bikesCfg.getProperties().stringPropertyNames()) {
				if (engine.applyProperty(key, bikesCfg.getProperties().getProperty(key))) {
					count++;
				}
			}
			tabs.updateFormValues();
			setLastPersistedProperties(new BikesCfg(engine.getBikesInf()).getMap());
			openMessageBox(messages.get("gui.message.file.open.customizations.applied", count), SWT.ICON_INFORMATION);
			return true;
		}
		catch (final UnknownPropertyException e) {
			openMessageBox(messages.get("gui.error.file.open.unknown.property", e.getPropertyName()), SWT.ICON_WARNING);
			return false;
		}
		catch (final InvalidNumberException e) {
			openMessageBox(messages.get("gui.error.file.open.invalid.number", e.getPropertyName(), e.getValue()), SWT.ICON_WARNING);
			return false;
		}
		catch (final ValueOutOfRangeException e) {
			openMessageBox(messages.get("gui.error.file.open.value.out.of.range", e.getPropertyName(), e.getValue(), e.getMinValue(), e.getMaxValue()), SWT.ICON_WARNING);
			return false;
		}
	}

	private boolean openBikesInf(@NonNull final Path file) throws IOException {
		try {
			engine.setBikesInf(new BikesInf(file));
			tabs.updateFormValues();
			setLastPersistedProperties(new BikesCfg(engine.getBikesInf()).getMap());
			currentFileName = file.toFile().getCanonicalPath();
			setCurrentFileModificationStatus(false);
			return true;
		}
		catch (final InvalidSizeException e) {
			openMessageBox(messages.get("gui.error.file.open.invalid.size"), SWT.ICON_WARNING);
			return false;
		}
	}

	public boolean exportAsCfg(@NonNull final BikeType bikeType) {
		try {
			updateModelValues(false);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.invalid.property", e.getPropertyName()), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		final FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		final String ext = BikesCfg.FILE_NAME.substring(1 + BikesCfg.FILE_NAME.lastIndexOf('.'));
		saveDialog.setFilterExtensions(new String[] { "*." + ext.toUpperCase(Locale.ROOT) + ";*." + ext.toLowerCase(Locale.ROOT) });
		saveDialog.setFileName(BikesCfg.FILE_NAME.substring(0, BikesCfg.FILE_NAME.lastIndexOf('.')) + bikeType.getDisplacement() + "." + ext);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		final String str = BikesCfg.createProperties(engine.getBikesInf().getBikes().get(bikeType));
		try (final Writer writer = Files.newBufferedWriter(Paths.get(fileName), BikesCfg.CHARSET)) {
			writer.write(str);
			return true;
		}
		catch (final IOException | RuntimeException e) {
			log.log(Level.WARNING, "Cannot save file as '" + fileName + "':", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	public boolean exportAllAsCfg() {
		try {
			updateModelValues(false);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.invalid.property", e.getPropertyName()), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		final FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		final String ext = BikesCfg.FILE_NAME.substring(1 + BikesCfg.FILE_NAME.lastIndexOf('.'));
		saveDialog.setFilterExtensions(new String[] { "*." + ext.toUpperCase(Locale.ROOT) + ";*." + ext.toLowerCase(Locale.ROOT) });
		saveDialog.setFileName(BikesCfg.FILE_NAME);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		final String str = BikesCfg.createProperties(engine.getBikesInf().getBikes().values().toArray(new Bike[0]));
		try (final Writer writer = Files.newBufferedWriter(Paths.get(fileName), BikesCfg.CHARSET)) {
			writer.write(str);
			return true;
		}
		catch (final IOException | RuntimeException e) {
			log.log(Level.WARNING, "Cannot save file as '" + fileName + "':", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	public boolean resetSingle(@NonNull final BikeType bikeType) {
		if (openMessageBox(messages.get("gui.message.reset.overwrite.single", bikeType.getDisplacement()), SWT.ICON_QUESTION | SWT.YES | SWT.NO) != SWT.YES) {
			return false;
		}
		try {
			doResetSingle(bikeType);
			setCurrentFileModificationStatus(isConfigurationChanged());
			return true;
		}
		catch (final RuntimeException e) {
			log.log(Level.WARNING, "Cannot reset bike " + bikeType + ':', e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.reset"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	private void doResetSingle(@NonNull final BikeType bikeType) {
		try {
			updateModelValues(true);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
		}
		engine.getBikesInf().reset(bikeType);
		tabs.updateFormValues();
	}

	public boolean resetAll() {
		if (openMessageBox(messages.get("gui.message.reset.overwrite.all"), SWT.ICON_QUESTION | SWT.YES | SWT.NO) != SWT.YES) {
			return false;
		}
		try {
			engine.getBikesInf().reset();
			tabs.updateFormValues();
			setCurrentFileModificationStatus(isConfigurationChanged());
			return true;
		}
		catch (final RuntimeException e) {
			log.log(Level.WARNING, "Cannot reset bikes:", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.reset"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	public boolean save() {
		if (currentFileName == null) {
			return saveAs();
		}
		else {
			final File bikesInfFile = new File(currentFileName);
			if (bikesInfFile.exists() && !bikesInfFile.canWrite()) {
				return saveAs();
			}
			try {
				updateModelValues(false);
			}
			catch (final InvalidPropertyException e) {
				log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
				EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.invalid.property", e.getPropertyName()), IStatus.WARNING, e, Images.getAppIconArray());
				return false;
			}
			try {
				Files.write(Paths.get(currentFileName), engine.getBikesInf().toByteArray());
				setLastPersistedProperties(new BikesCfg(engine.getBikesInf()).getMap());
				setCurrentFileModificationStatus(false);
				return true;
			}
			catch (final IOException | RuntimeException e) {
				log.log(Level.WARNING, "Cannot save file:", e);
				EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
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
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.invalid.property", e.getPropertyName()), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		final FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		final String ext = BikesInf.FILE_NAME.substring(1 + BikesInf.FILE_NAME.lastIndexOf('.'));
		saveDialog.setFilterExtensions(new String[] { "*." + ext.toUpperCase(Locale.ROOT) + ";*." + ext.toLowerCase(Locale.ROOT) });
		saveDialog.setFileName(BikesInf.FILE_NAME);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();

		if (fileName != null && !fileName.trim().isEmpty()) {
			try {
				Files.write(Paths.get(fileName), engine.getBikesInf().toByteArray());
				currentFileName = fileName;
				setCurrentFileModificationStatus(false);
				setLastPersistedProperties(new BikesCfg(engine.getBikesInf()).getMap());
				return true;
			}
			catch (final IOException | RuntimeException e) {
				log.log(Level.WARNING, "Cannot save file as '" + fileName + "':", e);
				EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
				return false;
			}
		}
		else {
			return false;
		}
	}

	public boolean loadHiddenCfg(@NonNull final BikeType type) {
		if (openMessageBox(messages.get("gui.message.hiddenCfg.overwrite", type.getDisplacement()), SWT.ICON_QUESTION | SWT.YES | SWT.NO) != SWT.YES) {
			return false;
		}
		try {
			final Properties properties = new BikesCfg(new Bike(type, HiddenBike.getByteArray())).getProperties();
			for (final String key : properties.stringPropertyNames()) {
				engine.applyProperty(key, properties.getProperty(key));
			}
			tabs.updateFormValues();
			setCurrentFileModificationStatus(isConfigurationChanged());
			return true;
		}
		catch (final InvalidPropertyException | RuntimeException e) {
			log.log(Level.WARNING, "Cannot load hidden configuration into bike " + type + ':', e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.hiddenCfg"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	private boolean isConfigurationChanged() {
		return !new BikesCfg(engine.getBikesInf()).getMap().equals(getLastPersistedProperties());
	}

	public void setCurrentFileModificationStatus(final boolean modified) {
		if (currentFileName != null && shell != null && !shell.isDisposed()) {
			final String title = getWindowTitle() + " - " + (modified ? "*" : "") + currentFileName;
			if (!title.equals(shell.getText())) {
				shell.setText(title);
			}
		}
	}

	public boolean askForSaving(@NonNull final String title, @NonNull final String message) {
		try {
			updateModelValues(true);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
		}
		if (isConfigurationChanged()) {
			final MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
			messageBox.setText(title);
			messageBox.setMessage(message);
			final int selectedButton = messageBox.open();
			switch (selectedButton) {
			case SWT.YES:
				return save();
			case SWT.NO:
				return true;
			case SWT.CANCEL:
				return false;
			default:
				throw new IllegalStateException("Invalid button code: " + selectedButton);
			}
		}
		else {
			return true;
		}
	}

	private int openMessageBox(@NonNull final String message, final int style) {
		final MessageBox messageBox = new MessageBox(shell, style);
		messageBox.setText(getWindowTitle());
		messageBox.setMessage(message);
		return messageBox.open();
	}

	public NumeralSystem getNumeralSystem() {
		return engine.getNumeralSystem();
	}

	public void setNumeralSystem(final NumeralSystem numeralSystem) {
		try {
			updateModelValues(true);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
		}
		engine.setNumeralSystem(numeralSystem);
		tabs.updateFormValues();
	}

	public Map<String, Integer> getLastPersistedProperties() {
		return Collections.unmodifiableMap(lastPersistedProperties);
	}

	private void setLastPersistedProperties(final Map<String, Integer> lastPersistedProperties) {
		this.lastPersistedProperties.clear();
		this.lastPersistedProperties.putAll(lastPersistedProperties);
	}

	public BikesInf getBikesInf() {
		return engine.getBikesInf();
	}

	public boolean isNumeric(final String value) {
		return engine.isNumeric(value);
	}

	private static String getWindowTitle() {
		return messages.get("gui.label.window.title");
	}

}
