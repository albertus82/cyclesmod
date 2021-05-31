package it.albertus.cyclesmod.gui;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

import it.albertus.cyclesmod.common.data.DefaultCars;
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
import it.albertus.cyclesmod.common.model.Cfg;
import it.albertus.cyclesmod.common.model.Inf;
import it.albertus.cyclesmod.common.resources.ConfigurableMessages;
import it.albertus.cyclesmod.common.resources.Language;
import it.albertus.cyclesmod.gui.listener.CloseListener;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.closeable.CloseableDevice;
import it.albertus.unexepack.InvalidDosHeaderException;
import it.albertus.unexepack.InvalidExepackHeaderException;
import it.albertus.unexepack.UnExepack;
import it.albertus.util.Version;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class CyclesModGui implements IShellProvider, Multilanguage {

	private static final String[] RESERVED_FILE_NAMES = { "GPCGA.EXE", "GPEGA.EXE", "GPTDY.EXE" };

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private final CyclesModEngine engine = new CyclesModEngine(new Inf());

	@Getter private final Shell shell;
	@Getter private final MenuBar menuBar;
	@Getter private final Tabs tabs;

	@Getter private final Map<String, Integer> defaultProperties = Collections.unmodifiableMap(new Cfg(engine.getBikesInf()).getMap());
	private final Map<String, Integer> lastPersistedProperties = new HashMap<>(defaultProperties);

	private Mode mode = Mode.DEFAULT;
	private String currentFileName;
	private byte[] gpcOriginalExeBytes;

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
		openDialog.setFilterExtensions(new String[] { "*.INF;*.inf;*.EXE;*.exe" });
		final String fileName = openDialog.open();
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		return open(fileName);
	}

	private boolean open(@NonNull final String path) {
		try {
			final Path file = Paths.get(path);
			if (!file.toFile().exists() || Files.isDirectory(file)) {
				openMessageBox(messages.get("gui.error.file.open.not.found"), SWT.ICON_WARNING);
				return false;
			}
			if (file.toString().toUpperCase(Locale.ROOT).endsWith(".INF")) {
				setMode(Mode.CYCLES);
				return openInf(file);
			}
			else if (file.toString().toUpperCase(Locale.ROOT).endsWith(".EXE")) {
				setMode(Mode.GPC);
				return openExe(file);
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

	public boolean importCfg() {
		final FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
		openDialog.setFilterExtensions(new String[] { "*.CFG;*.cfg" });
		final String fileName = openDialog.open();
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		return importCfg(fileName);
	}

	private boolean importCfg(@NonNull final String path) {
		try {
			final Path file = Paths.get(path);
			if (!file.toFile().exists() || Files.isDirectory(file)) {
				openMessageBox(messages.get("gui.error.file.open.not.found"), SWT.ICON_WARNING);
				return false;
			}
			if (file.toString().toUpperCase(Locale.ROOT).endsWith(".CFG")) {
				return importCfg(file);
			}
			else {
				openMessageBox(messages.get("gui.error.file.open.invalid.type"), SWT.ICON_WARNING);
				return false;
			}
		}
		catch (final InvalidPathException e) {
			log.log(Level.WARNING, "Cannot import file '" + path + "':", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.open.invalid.path"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		catch (final RuntimeException | IOException e) {
			log.log(Level.WARNING, "Cannot import file '" + path + "':", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.open.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	private boolean importCfg(@NonNull final Path file) throws IOException {
		try {
			final Cfg bikesCfg = new Cfg(file);
			int count = 0;
			final NumeralSystem backup = engine.getNumeralSystem();
			engine.setNumeralSystem(NumeralSystem.DEFAULT);
			for (final String key : bikesCfg.getProperties().stringPropertyNames()) {
				if (engine.applyProperty(key, bikesCfg.getProperties().getProperty(key))) {
					count++;
				}
			}
			engine.setNumeralSystem(backup);
			tabs.updateFormValues();
			if (count > 0) {
				setCurrentFileModificationStatus(true);
			}
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

	private boolean openInf(@NonNull final Path file) throws IOException {
		try {
			engine.setBikesInf(new Inf(file));
			updateGuiStatusAfterOpening(file);
			return true;
		}
		catch (final InvalidSizeException e) {
			openMessageBox(messages.get("gui.error.file.open.invalid.size"), SWT.ICON_WARNING);
			return false;
		}
	}

	private boolean openExe(@NonNull final Path file) throws IOException {
		if (Files.size(file) > 0x30000) {
			openMessageBox(messages.get("gui.error.file.open.invalid.size"), SWT.ICON_WARNING);
			return false;
		}
		try {
			final byte[] unpackedExec = unpackExec(file);
			if (UnExepack.memmem(unpackedExec, DefaultCars.getByteArray()) == -1) {
				openMessageBox(messages.get("gui.error.file.open.invalid.type"), SWT.ICON_WARNING);
				return false;
			}
			if (!file.toFile().setReadOnly()) {
				log.log(Level.INFO, "Cannot set read only flag for file: {0}.", file);
			}
			gpcOriginalExeBytes = unpackedExec;
			engine.setBikesInf(new Inf(DefaultCars.getByteArray()));
			updateGuiStatusAfterOpening(file);
			return true;
		}
		catch (final InvalidDosHeaderException e) {
			openMessageBox(messages.get("gui.error.file.open.invalid.type"), SWT.ICON_WARNING);
			return false;
		}
	}

	private static byte[] unpackExec(@NonNull final Path file) throws IOException, InvalidDosHeaderException {
		final byte[] originalExec = Files.readAllBytes(file);
		try {
			return UnExepack.unpack(originalExec);
		}
		catch (final InvalidExepackHeaderException e) {
			log.log(Level.FINE, "File '" + file + "' may be already unpacked:", e);
			return originalExec;
		}
		catch (final RuntimeException e) {
			log.log(Level.WARNING, "Cannot unpack '" + file + "':", e);
			return originalExec;
		}
	}

	private void updateGuiStatusAfterOpening(final Path file) throws IOException {
		tabs.updateFormValues();
		setLastPersistedProperties(new Cfg(engine.getBikesInf()).getMap());
		currentFileName = file.toFile().getCanonicalPath();
		setCurrentFileModificationStatus(false);
	}

	public boolean exportCfgSingle(@NonNull final BikeType bikeType) {
		try {
			updateModelValues(false);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.invalid.property", e.getPropertyName()), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		final FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		final String ext = Cfg.FILE_NAME.substring(1 + Cfg.FILE_NAME.lastIndexOf('.'));
		saveDialog.setFilterExtensions(new String[] { "*." + ext.toUpperCase(Locale.ROOT) + ";*." + ext.toLowerCase(Locale.ROOT) });
		saveDialog.setFileName(Cfg.FILE_NAME.substring(0, Cfg.FILE_NAME.lastIndexOf('.')) + bikeType.getDisplacement() + "." + ext);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		final String str = Cfg.createProperties(engine.getBikesInf().getBikes().get(bikeType));
		try (final Writer writer = Files.newBufferedWriter(Paths.get(fileName), Cfg.CHARSET)) {
			writer.write(str);
			return true;
		}
		catch (final IOException | RuntimeException e) {
			log.log(Level.WARNING, "Cannot save file as '" + fileName + "':", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	public boolean exportCfgAll() {
		try {
			updateModelValues(false);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.invalid.property", e.getPropertyName()), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		final FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		final String ext = Cfg.FILE_NAME.substring(1 + Cfg.FILE_NAME.lastIndexOf('.'));
		saveDialog.setFilterExtensions(new String[] { "*." + ext.toUpperCase(Locale.ROOT) + ";*." + ext.toLowerCase(Locale.ROOT) });
		saveDialog.setFileName(Cfg.FILE_NAME);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		final String str = Cfg.createProperties(engine.getBikesInf().getBikes().values().toArray(new Bike[0]));
		try (final Writer writer = Files.newBufferedWriter(Paths.get(fileName), Cfg.CHARSET)) {
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
		if (currentFileName == null || Arrays.stream(RESERVED_FILE_NAMES).anyMatch(n -> currentFileName.toUpperCase(Locale.ROOT).endsWith(n.toUpperCase(Locale.ROOT)))) {
			return saveAs();
		}
		final Path destFile = Paths.get(currentFileName);
		if (destFile.toFile().exists() && !destFile.toFile().canWrite()) {
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
			final byte[] bytes;
			if (Mode.GPC.equals(mode)) { // EXE
				final int offset = UnExepack.memmem(gpcOriginalExeBytes, DefaultCars.getByteArray());
				bytes = new byte[gpcOriginalExeBytes.length];
				System.arraycopy(gpcOriginalExeBytes, 0, bytes, 0, offset);
				System.arraycopy(engine.getBikesInf().toByteArray(), 0, bytes, offset, Inf.FILE_SIZE);
				System.arraycopy(gpcOriginalExeBytes, offset + Inf.FILE_SIZE, bytes, offset + Inf.FILE_SIZE, gpcOriginalExeBytes.length - offset - Inf.FILE_SIZE);
			}
			else { // INF
				bytes = engine.getBikesInf().toByteArray();
			}
			Files.write(destFile, bytes);
			setLastPersistedProperties(new Cfg(engine.getBikesInf()).getMap());
			setCurrentFileModificationStatus(false);
			return true;
		}
		catch (final IOException | RuntimeException e) {
			log.log(Level.WARNING, "Cannot save file:", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
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
		if (Mode.GPC.equals(mode)) {
			return saveAsExe();
		}
		else {
			return saveAsInf();
		}
	}

	private boolean saveAsInf() {
		final FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		final String ext = Inf.FILE_NAME.substring(1 + Inf.FILE_NAME.lastIndexOf('.'));
		saveDialog.setFilterExtensions(new String[] { "*." + ext.toUpperCase(Locale.ROOT) + ";*." + ext.toLowerCase(Locale.ROOT) });
		saveDialog.setFileName(Inf.FILE_NAME);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();
		if (fileName != null && !fileName.trim().isEmpty()) {
			try {
				Files.write(Paths.get(fileName), engine.getBikesInf().toByteArray());
				currentFileName = fileName;
				setCurrentFileModificationStatus(false);
				setLastPersistedProperties(new Cfg(engine.getBikesInf()).getMap());
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

	private boolean saveAsExe() {
		final FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		saveDialog.setFilterExtensions(new String[] { "*.EXE;*.exe" });
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();
		if (fileName != null && !fileName.trim().isEmpty()) {
			if (Arrays.stream(RESERVED_FILE_NAMES).anyMatch(n -> fileName.toUpperCase(Locale.ROOT).endsWith(n.toUpperCase(Locale.ROOT)))) {
				openMessageBox("Cannot overwrite original EXE game files.", SWT.ICON_WARNING);
				return false;
			}
			try {
				final int offset = UnExepack.memmem(gpcOriginalExeBytes, DefaultCars.getByteArray());
				final byte[] newExe = new byte[gpcOriginalExeBytes.length];
				System.arraycopy(gpcOriginalExeBytes, 0, newExe, 0, offset);
				System.arraycopy(engine.getBikesInf().toByteArray(), 0, newExe, offset, Inf.FILE_SIZE);
				System.arraycopy(gpcOriginalExeBytes, offset + Inf.FILE_SIZE, newExe, offset + Inf.FILE_SIZE, gpcOriginalExeBytes.length - offset - Inf.FILE_SIZE);
				Files.write(Paths.get(fileName), newExe);
				currentFileName = fileName;
				setCurrentFileModificationStatus(false);
				setLastPersistedProperties(new Cfg(engine.getBikesInf()).getMap());
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
			final NumeralSystem backup = engine.getNumeralSystem();
			engine.setNumeralSystem(NumeralSystem.DEFAULT);
			final Properties properties = new Cfg(new Bike(type, HiddenBike.getByteArray())).getProperties();
			for (final String key : properties.stringPropertyNames()) {
				engine.applyProperty(key, properties.getProperty(key));
			}
			engine.setNumeralSystem(backup);
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
		return !new Cfg(engine.getBikesInf()).getMap().equals(getLastPersistedProperties());
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

	public Inf getBikesInf() {
		return engine.getBikesInf();
	}

	public boolean isNumeric(final String value) {
		return engine.isNumeric(value);
	}

	private void setMode(final Mode mode) {
		this.mode = mode;
		switch (mode) {
		case CYCLES:
			gpcOriginalExeBytes = null;
			// Set cycles defaults
			// Set cycles labels
			// Set cycles reset & hidden listeners
			break;
		case GPC:
			// Set gpc defaults
			// Set gpc labels
			// Set gpc reset & hidden listeners
			break;
		default:
			throw new IllegalArgumentException("Unknown mode: " + mode);
		}
	}

	private static String getWindowTitle() {
		return messages.get("gui.label.window.title");
	}

}
