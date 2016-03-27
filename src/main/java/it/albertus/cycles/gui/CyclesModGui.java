package it.albertus.cycles.gui;

import it.albertus.cycles.data.DefaultBikes;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.engine.NumeralSystem;
import it.albertus.cycles.gui.listener.CloseListener;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Resources;
import it.albertus.cycles.resources.Resources.Language;
import it.albertus.util.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CyclesModGui extends CyclesModEngine implements IShellProvider {

	private final Map<String, Integer> defaultProperties = new HashMap<String, Integer>();
	private final Map<String, Integer> lastPersistedProperties = new HashMap<String, Integer>();
	private final TextFormatter textFormatter = new TextFormatter(this);

	private final Shell shell;
	private final MenuBar menuBar;
	private final Tabs tabs;

	/** GUI entry point. */
	public static void start(final String fileName) throws IOException {
		final Display display = new Display();
		final Shell shell = new CyclesModGui(display, fileName).getShell();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private CyclesModGui(final Display display, final String fileName) throws IOException {
		// Loading default properties...
		setBikesInf(new BikesInf(new DefaultBikes().getInputStream()));
		defaultProperties.putAll(new BikesCfg(getBikesInf()).getMap());

		// Shell creation...
		shell = new Shell(display);
		shell.setImages(Images.MAIN_ICONS);
		shell.setText(Resources.get("win.title"));
		shell.setLayout(new FillLayout());
		shell.addListener(SWT.Close, new CloseListener(this));

		menuBar = new MenuBar(this);

		tabs = new Tabs(this);

		// Size...
		shell.pack();

		updateFormValues();

		// Loading custom properties...
		if (StringUtils.isNotBlank(fileName)) {
			load(fileName, false);
		}
		else {
			setLastPersistedProperties(defaultProperties);
		}
	}

	public void updateLanguage(final Language language) {
		Resources.setLanguage(language);
		menuBar.setTexts();
		tabs.setTexts();
	}

	public void updateFormValues() {
		final Map<String, Integer> properties = new BikesCfg(getBikesInf()).getMap();

		// Consistency check...
		if (properties.size() != tabs.getFormProperties().size()) {
			throw new IllegalStateException(Resources.get("err.properties.number"));
		}

		// Update screen values...
		for (final String key : tabs.getFormProperties().keySet()) {
			if (!properties.containsKey(key)) {
				throw new RuntimeException(Resources.get("err.property.missing", key));
			}
			final Text field = tabs.getFormProperties().get(key).getText();

			// Update field max length...
			final int textLimit;
			if (isSettingsProperty(key)) {
				textLimit = Integer.toString(Settings.MAX_VALUE, getNumeralSystem().getRadix()).length();
			}
			else if (isGearboxProperty(key)) {
				textLimit = Integer.toString(Gearbox.MAX_VALUE, getNumeralSystem().getRadix()).length();
			}
			else if (isTorqueProperty(key)) {
				textLimit = Integer.toString(Torque.MAX_VALUE, getNumeralSystem().getRadix()).length();
			}
			else {
				throw new IllegalStateException(Resources.get("err.unsupported.property", key, tabs.getFormProperties().get(key).getValue()));
			}
			if (field.getTextLimit() != textLimit) {
				field.setTextLimit(textLimit);
			}

			// Update field value...
			final String text = Integer.toString(properties.get(key), getNumeralSystem().getRadix());
			if (!field.getText().equals(text)) {
				field.setText(text);
			}

			// Update tooltip text...
			final String toolTipText = Resources.get("msg.tooltip.default", Integer.toString(((Integer) field.getData(FormProperty.TextDataKey.DEFAULT.toString())), getNumeralSystem().getRadix()).toUpperCase());
			if (field.getToolTipText() == null || !field.getToolTipText().equals(toolTipText)) {
				field.setToolTipText(toolTipText);
			}

			// Update font style...
			textFormatter.updateFontStyle(field);
		}

		// Update torque graphs...
		for (final Bike bike : getBikesInf().getBikes()) {
			final TorqueGraph graph = tabs.getTorqueGraphs().get(bike.getType());
			for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
				graph.getValues()[i] = bike.getTorque().getCurve()[i];
			}
			graph.refresh();
		}
	}

	public void updateModelValues(boolean lenient) {
		for (final String key : tabs.getFormProperties().keySet()) {
			applyProperty(key, tabs.getFormProperties().get(key).getValue(), lenient);
		}
	}

	public void load(final String fileName, final boolean successMessage) {
		try {
			if (StringUtils.endsWithIgnoreCase(fileName, ".inf")) {
				final File bikesInfFile = new File(fileName);
				setBikesInf(new BikesInf(bikesInfFile));
				updateFormValues();
				setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
				shell.setText(Resources.get("win.title") + " - " + bikesInfFile.getCanonicalPath());
				if (successMessage) {
					final MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Resources.get("msg.completed"));
					messageBox.setMessage(Resources.get("msg.file.loaded", fileName));
					messageBox.open();
				}
			}
			else if (StringUtils.endsWithIgnoreCase(fileName, ".cfg")) {
				setBikesInf(new BikesInf(new DefaultBikes().getInputStream()));

				final BikesCfg bikesCfg = new BikesCfg(fileName);
				short changesCount = 0;
				for (final String key : bikesCfg.getProperties().stringPropertyNames()) {
					if (applyProperty(key, bikesCfg.getProperties().getProperty(key), false)) {
						changesCount++;
					}
				}
				updateFormValues();
				setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
				if (successMessage) {
					final MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Resources.get("msg.completed"));
					messageBox.setMessage(Resources.get("msg.customizations.applied", changesCount));
					messageBox.open();
				}
			}
			else {
				final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				messageBox.setText(Resources.get("msg.warning"));
				messageBox.setMessage(Resources.get("err.file.invalid"));
				messageBox.open();
			}
		}
		catch (final Exception e) {
			System.err.println(ExceptionUtils.getLogMessage(e));
			final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
			messageBox.setText(Resources.get("msg.warning"));
			messageBox.setMessage(Resources.get("err.file.load", ExceptionUtils.getUIMessage(e)));
			messageBox.open();
		}
	}

	public boolean save(final boolean successMessage) {
		try {
			updateModelValues(false);
		}
		catch (InvalidPropertyException ipe) {
			System.err.println(ExceptionUtils.getLogMessage(ipe));
			MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
			messageBox.setText(Resources.get("msg.warning"));
			messageBox.setMessage(ExceptionUtils.getUIMessage(ipe));
			messageBox.open();
			return false;
		}
		final FileDialog saveDialog = new FileDialog(getShell(), SWT.SAVE);
		saveDialog.setFilterExtensions(new String[] { "*.INF; *.inf" });
		saveDialog.setFileName(BikesInf.FILE_NAME);
		saveDialog.setOverwrite(true);
		final String fileName = saveDialog.open();

		if (StringUtils.isNotBlank(fileName)) {
			try {
				getBikesInf().write(fileName);
			}
			catch (final Exception e) {
				System.err.println(ExceptionUtils.getLogMessage(e));
				final MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
				messageBox.setText(Resources.get("msg.warning"));
				messageBox.setMessage(Resources.get("err.file.save", ExceptionUtils.getUIMessage(e)));
				messageBox.open();
				return false;
			}
			shell.setText(Resources.get("win.title") + " - " + fileName);
			setLastPersistedProperties(new BikesCfg(getBikesInf()).getMap());
			if (successMessage) {
				final MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION);
				messageBox.setText(Resources.get("msg.completed"));
				messageBox.setMessage(Resources.get("msg.file.saved", fileName));
				messageBox.open();
			}
			return true;
		}
		else {
			return false;
		}
	}

	public boolean canCut() {
		return canCopy();
	}

	public boolean canCopy() {
		for (final FormProperty fp : tabs.getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().getSelectionText() != null && fp.getText().getSelectionText().length() != 0) {
				return true;
			}
		}
		return false;
	}

	public boolean canPaste() {
		final Clipboard clipboard = new Clipboard(this.getShell().getDisplay());
		final TransferData[] clipboardAvailableTypes = clipboard.getAvailableTypes();
		clipboard.dispose();
		boolean enabled = false;
		for (final TransferData clipboardType : clipboardAvailableTypes) {
			if (TextTransfer.getInstance().isSupportedType(clipboardType)) {
				enabled = true;
				break;
			}
		}
		if (enabled) {
			for (final FormProperty fp : tabs.getFormProperties().values()) {
				if (fp != null && fp.getText() != null && fp.getText().isFocusControl()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void setNumeralSystem(final NumeralSystem numeralSystem) {
		updateModelValues(true);
		super.setNumeralSystem(numeralSystem);
		updateFormValues();
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

	public TextFormatter getTextFormatter() {
		return textFormatter;
	}

}
