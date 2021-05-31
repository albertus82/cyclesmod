package it.albertus.cyclesmod.gui;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.naming.SizeLimitExceededException;

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
import it.albertus.cyclesmod.common.data.HiddenCar;
import it.albertus.cyclesmod.common.data.InvalidSizeException;
import it.albertus.cyclesmod.common.engine.CyclesModEngine;
import it.albertus.cyclesmod.common.engine.InvalidNumberException;
import it.albertus.cyclesmod.common.engine.InvalidPropertyException;
import it.albertus.cyclesmod.common.engine.NumeralSystem;
import it.albertus.cyclesmod.common.engine.UnknownPropertyException;
import it.albertus.cyclesmod.common.engine.ValueOutOfRangeException;
import it.albertus.cyclesmod.common.model.Game;
import it.albertus.cyclesmod.common.model.Vehicle;
import it.albertus.cyclesmod.common.model.VehicleType;
import it.albertus.cyclesmod.common.model.VehiclesCfg;
import it.albertus.cyclesmod.common.model.VehiclesInf;
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

	@Getter private Mode mode = Mode.DEFAULT;

	private final CyclesModEngine engine = new CyclesModEngine(new VehiclesInf(mode.getGame()));

	@Getter private final Shell shell;
	@Getter private final MenuBar menuBar;
	@Getter private final Tabs tabs;

	@Getter private final Map<Game, Map<String, Integer>> defaultProperties = new EnumMap<>(Game.class);
	private final Map<String, Integer> lastPersistedProperties;

	private String currentFileName;
	private byte[] gpcOriginalExeBytes;

	private CyclesModGui(@NonNull final Display display) {
		for (final Game game : Game.values()) {
			defaultProperties.put(game, Collections.unmodifiableMap(new VehiclesCfg(game, new VehiclesInf(game)).getMap()));
		}
		lastPersistedProperties = new HashMap<>(defaultProperties.get(mode.getGame()));

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
		for (final String key : tabs.getFormProperties().get(mode.getGame()).keySet()) {
			try {
				engine.applyProperty(key, tabs.getFormProperties().get(mode.getGame()).get(key).getValue());
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
			final VehiclesCfg vehiclesCfg = new VehiclesCfg(file);
			int count = 0;
			final NumeralSystem backup = engine.getNumeralSystem();
			engine.setNumeralSystem(NumeralSystem.DEFAULT);
			for (final String key : vehiclesCfg.getProperties().stringPropertyNames()) {
				if (engine.applyProperty(key, vehiclesCfg.getProperties().getProperty(key))) {
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
			engine.setVehiclesInf(new VehiclesInf(file));
			updateGuiStatusAfterOpening(file);
			return true;
		}
		catch (final InvalidSizeException e) {
			openMessageBox(messages.get("gui.error.file.open.invalid.size"), SWT.ICON_WARNING);
			return false;
		}
	}

	private boolean openExe(@NonNull final Path file) throws IOException {
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
			engine.setVehiclesInf(new VehiclesInf(DefaultCars.getByteArray()));
			updateGuiStatusAfterOpening(file);
			return true;
		}
		catch (final SizeLimitExceededException e) {
			log.log(Level.FINE, "File '" + file + "' is too large:", e);
			openMessageBox(messages.get("gui.error.file.open.invalid.size"), SWT.ICON_WARNING);
			return false;
		}
		catch (final InvalidDosHeaderException e) {
			log.log(Level.FINE, "File '" + file + "' is not a valid MS-DOS executable:", e);
			openMessageBox(messages.get("gui.error.file.open.invalid.type"), SWT.ICON_WARNING);
			return false;
		}
	}

	private static byte[] unpackExec(@NonNull final Path file) throws IOException, SizeLimitExceededException, InvalidDosHeaderException {
		final int sizeLimit = 0x30000; // 192 KiB, no GPC executable seems to be larger than 105 KiB, even unpacked. 
		if (Files.size(file) > sizeLimit) {
			throw new SizeLimitExceededException("The input file size exceeds the limit of " + sizeLimit + " bytes");
		}
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
		setLastPersistedProperties(new VehiclesCfg(mode.getGame(), engine.getVehiclesInf()).getMap());
		currentFileName = file.toFile().getCanonicalPath();
		setCurrentFileModificationStatus(false);
	}

	public boolean exportCfgSingle(@NonNull final VehicleType vehicleType) {
		try {
			updateModelValues(false);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.invalid.property", e.getPropertyName()), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
		final FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		final String proposedFileName = VehiclesCfg.getFileName(mode.getGame(), vehicleType);
		final String fileExtension = proposedFileName.substring(1 + proposedFileName.lastIndexOf('.'));
		saveDialog.setFilterExtensions(new String[] { "*." + fileExtension.toUpperCase(Locale.ROOT) + ";*." + fileExtension.toLowerCase(Locale.ROOT) });
		saveDialog.setFileName(proposedFileName);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		final String str = VehiclesCfg.createProperties(mode.getGame(), engine.getVehiclesInf().getVehicles().get(vehicleType));
		try (final Writer writer = Files.newBufferedWriter(Paths.get(fileName), VehiclesCfg.CHARSET)) {
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
		final String proposedFileName = VehiclesCfg.getFileName(mode.getGame());
		final String fileExtension = proposedFileName.substring(1 + proposedFileName.lastIndexOf('.'));
		saveDialog.setFilterExtensions(new String[] { "*." + fileExtension.toUpperCase(Locale.ROOT) + ";*." + fileExtension.toLowerCase(Locale.ROOT) });
		saveDialog.setFileName(proposedFileName);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		final String str = VehiclesCfg.createProperties(mode.getGame(), engine.getVehiclesInf().getVehicles().values().toArray(new Vehicle[0]));
		try (final Writer writer = Files.newBufferedWriter(Paths.get(fileName), VehiclesCfg.CHARSET)) {
			writer.write(str);
			return true;
		}
		catch (final IOException | RuntimeException e) {
			log.log(Level.WARNING, "Cannot save file as '" + fileName + "':", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	public boolean resetSingle(@NonNull final VehicleType vehicleType) {
		if (openMessageBox(messages.get("gui.message.reset.overwrite.single", vehicleType.getDisplacement()), SWT.ICON_QUESTION | SWT.YES | SWT.NO) != SWT.YES) {
			return false;
		}
		try {
			doResetSingle(vehicleType);
			setCurrentFileModificationStatus(isConfigurationChanged());
			return true;
		}
		catch (final RuntimeException e) {
			log.log(Level.WARNING, "Cannot reset vehicle " + vehicleType + ':', e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.reset"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	private void doResetSingle(@NonNull final VehicleType vehicleType) {
		try {
			updateModelValues(true);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
		}
		engine.getVehiclesInf().reset(mode.getGame(), vehicleType);
		tabs.updateFormValues();
	}

	public boolean resetAll() {
		if (openMessageBox(messages.get("gui.message.reset.overwrite.all"), SWT.ICON_QUESTION | SWT.YES | SWT.NO) != SWT.YES) {
			return false;
		}
		try {
			engine.getVehiclesInf().reset(mode.getGame());
			tabs.updateFormValues();
			setCurrentFileModificationStatus(isConfigurationChanged());
			return true;
		}
		catch (final RuntimeException e) {
			log.log(Level.WARNING, "Cannot reset vehicles:", e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.reset"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	public boolean save() {
		if (currentFileName == null || Arrays.stream(RESERVED_FILE_NAMES).anyMatch(reservedFileName -> reservedFileName.equalsIgnoreCase(Paths.get(currentFileName).getFileName().toString()))) {
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
				System.arraycopy(engine.getVehiclesInf().toByteArray(), 0, bytes, offset, VehiclesInf.FILE_SIZE);
				System.arraycopy(gpcOriginalExeBytes, offset + VehiclesInf.FILE_SIZE, bytes, offset + VehiclesInf.FILE_SIZE, gpcOriginalExeBytes.length - offset - VehiclesInf.FILE_SIZE);
			}
			else if (Mode.CYCLES.equals(mode)) { // INF
				bytes = engine.getVehiclesInf().toByteArray();
			}
			else {
				throw new IllegalStateException("Unknown mode: " + mode);
			}
			Files.write(destFile, bytes);
			setLastPersistedProperties(new VehiclesCfg(mode.getGame(), engine.getVehiclesInf()).getMap());
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
		switch (mode) {
		case CYCLES:
			return saveAsInf();
		case GPC:
			return saveAsExe();
		default:
			throw new IllegalStateException("Unknown mode: " + mode);
		}
	}

	private boolean saveAsInf() {
		final FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		final String proposedFileName = VehiclesInf.getFileName(mode.getGame());
		final String fileExtension = proposedFileName.substring(1 + proposedFileName.lastIndexOf('.'));
		saveDialog.setFilterExtensions(new String[] { "*." + fileExtension.toUpperCase(Locale.ROOT) + ";*." + fileExtension.toLowerCase(Locale.ROOT) });
		saveDialog.setFileName(proposedFileName);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();
		if (fileName != null && !fileName.trim().isEmpty()) {
			try {
				Files.write(Paths.get(fileName), engine.getVehiclesInf().toByteArray());
				currentFileName = fileName;
				setCurrentFileModificationStatus(false);
				setLastPersistedProperties(new VehiclesCfg(mode.getGame(), engine.getVehiclesInf()).getMap());
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
		final String userChoosenFileName = saveDialog.open();
		if (userChoosenFileName != null && !userChoosenFileName.trim().isEmpty()) {
			if (Arrays.stream(RESERVED_FILE_NAMES).anyMatch(reservedFileName -> reservedFileName.equalsIgnoreCase(Paths.get(userChoosenFileName).getFileName().toString()))) {
				openMessageBox("Cannot overwrite original EXE game files.", SWT.ICON_WARNING);
				return false;
			}
			try {
				final int offset = UnExepack.memmem(gpcOriginalExeBytes, DefaultCars.getByteArray());
				final byte[] newExe = new byte[gpcOriginalExeBytes.length];
				System.arraycopy(gpcOriginalExeBytes, 0, newExe, 0, offset);
				System.arraycopy(engine.getVehiclesInf().toByteArray(), 0, newExe, offset, VehiclesInf.FILE_SIZE);
				System.arraycopy(gpcOriginalExeBytes, offset + VehiclesInf.FILE_SIZE, newExe, offset + VehiclesInf.FILE_SIZE, gpcOriginalExeBytes.length - offset - VehiclesInf.FILE_SIZE);
				Files.write(Paths.get(userChoosenFileName), newExe);
				currentFileName = userChoosenFileName;
				setCurrentFileModificationStatus(false);
				setLastPersistedProperties(new VehiclesCfg(mode.getGame(), engine.getVehiclesInf()).getMap());
				return true;
			}
			catch (final IOException | RuntimeException e) {
				log.log(Level.WARNING, "Cannot save file as '" + userChoosenFileName + "':", e);
				EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.file.save.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
				return false;
			}
		}
		else {
			return false;
		}
	}

	public boolean loadHiddenCfg(@NonNull final VehicleType type) {
		if (openMessageBox(messages.get("gui.message.hiddenCfg.overwrite", type.getDisplacement()), SWT.ICON_QUESTION | SWT.YES | SWT.NO) != SWT.YES) {
			return false;
		}
		try {
			final NumeralSystem backup = engine.getNumeralSystem();
			engine.setNumeralSystem(NumeralSystem.DEFAULT);
			final byte[] byteArray;
			switch (mode) {
			case CYCLES:
				byteArray = HiddenBike.getByteArray();
				break;
			case GPC:
				byteArray = HiddenCar.getByteArray();
				break;
			default:
				throw new IllegalStateException("Unknown mode: " + mode);
			}
			final Properties properties = new VehiclesCfg(mode.getGame(), new Vehicle(type, byteArray)).getProperties();
			for (final String key : properties.stringPropertyNames()) {
				engine.applyProperty(key, properties.getProperty(key));
			}
			engine.setNumeralSystem(backup);
			tabs.updateFormValues();
			setCurrentFileModificationStatus(isConfigurationChanged());
			return true;
		}
		catch (final InvalidPropertyException | RuntimeException e) {
			log.log(Level.WARNING, "Cannot load hidden configuration into vehicle " + type + ':', e);
			EnhancedErrorDialog.openError(shell, getWindowTitle(), messages.get("gui.error.hiddenCfg"), IStatus.WARNING, e, Images.getAppIconArray());
			return false;
		}
	}

	private boolean isConfigurationChanged() {
		return !new VehiclesCfg(mode.getGame(), engine.getVehiclesInf()).getMap().equals(getLastPersistedProperties());
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

	public VehiclesInf getVehiclesInf() {
		return engine.getVehiclesInf();
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
