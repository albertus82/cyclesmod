package it.albertus.cycles.gui;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.gui.listener.CloseListener;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Resources;
import it.albertus.util.ExceptionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

	private final Map<String, FormProperty> formProperties = new HashMap<String, FormProperty>();
	private final Map<Bike.Type, TorqueGraph> torqueGraphs = new EnumMap<Bike.Type, TorqueGraph>(Bike.Type.class);
	private final Map<String, Integer> defaultProperties = new TreeMap<String, Integer>();
	private final Map<String, Integer> lastPersistedProperties = new TreeMap<String, Integer>();
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

	public void toNumericProperties(final BikesCfg bikesCfg, final Map<String, Integer> destination) {
		destination.clear();
		for (String key : bikesCfg.getProperties().stringPropertyNames()) {
			destination.put(key, Integer.valueOf(bikesCfg.getProperties().getProperty(key), 10));
		}
	}

	private CyclesModGui(final Display display, final String fileName) throws IOException {
		// Loading default properties...
		setBikesInf(new BikesInf(new BikesZip().getInputStream()));
		toNumericProperties(new BikesCfg(getBikesInf()), defaultProperties);
		lastPersistedProperties.putAll(defaultProperties);

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
	}

	public void updateFormValues() {
		final Map<String, Integer> properties = new TreeMap<String, Integer>();
		toNumericProperties(new BikesCfg(getBikesInf()), properties);

		// Consistency check...
		if (properties.size() != formProperties.size()) {
			throw new IllegalStateException(Resources.get("err.properties.number"));
		}

		// Update screen values...
		for (final String key : formProperties.keySet()) {
			if (!properties.containsKey(key)) {
				throw new RuntimeException(Resources.get("err.property.missing", key));
			}
			final Text field = formProperties.get(key).getText();

			// Update field value...
			field.setText(Integer.toString(properties.get(key), getRadix()));

			// Update font style...
			textFormatter.updateFontStyle(field);
		}

		// Update torque graphs...
		for (final Bike bike : getBikesInf().getBikes()) {
			final TorqueGraph graph = torqueGraphs.get(bike.getType());
			for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
				graph.getValues()[i] = bike.getTorque().getCurve()[i];
			}
			graph.refresh();
		}
	}

	public void updateModelValues(boolean lenient) {
		for (final String key : formProperties.keySet()) {
			applyProperty(key, formProperties.get(key).getValue(), lenient);
		}
	}

	public void load(final String fileName, final boolean successMessage) {
		try {
			if (StringUtils.endsWithIgnoreCase(fileName, ".inf")) {
				setBikesInf(new BikesInf(fileName));
				updateFormValues();
				toNumericProperties(new BikesCfg(getBikesInf()), lastPersistedProperties);
				if (successMessage) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Resources.get("msg.completed"));
					messageBox.setMessage(Resources.get("msg.file.loaded", fileName));
					messageBox.open();
				}
			}
			else if (StringUtils.endsWithIgnoreCase(fileName, ".cfg")) {
				setBikesInf(new BikesInf(new BikesZip().getInputStream()));

				BikesCfg bikesCfg = new BikesCfg(fileName);
				short changesCount = 0;
				for (Object objectKey : bikesCfg.getProperties().keySet()) {
					String key = (String) objectKey;
					if (applyProperty(key, bikesCfg.getProperties().getProperty(key), false)) {
						changesCount++;
					}
				}
				updateFormValues();
				toNumericProperties(new BikesCfg(getBikesInf()), lastPersistedProperties);
				if (successMessage) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Resources.get("msg.completed"));
					messageBox.setMessage(Resources.get("msg.customizations.applied", changesCount));
					messageBox.open();
				}
			}
			else {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				messageBox.setText(Resources.get("msg.warning"));
				messageBox.setMessage(Resources.get("err.file.invalid"));
				messageBox.open();
			}
		}
		catch (Exception e) {
			System.err.println(ExceptionUtils.getLogMessage(e));
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
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
		FileDialog saveDialog = new FileDialog(getShell(), SWT.SAVE);
		saveDialog.setFilterExtensions(new String[] { "*.INF; *.inf" });
		saveDialog.setFileName(BikesInf.FILE_NAME);
		saveDialog.setOverwrite(true);
		String fileName = saveDialog.open();

		if (StringUtils.isNotBlank(fileName)) {
			try {
				getBikesInf().write(fileName);
			}
			catch (Exception e) {
				System.err.println(ExceptionUtils.getLogMessage(e));
				MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
				messageBox.setText(Resources.get("msg.warning"));
				messageBox.setMessage(Resources.get("err.file.save", ExceptionUtils.getUIMessage(e)));
				messageBox.open();
				return false;
			}
			toNumericProperties(new BikesCfg(getBikesInf()), lastPersistedProperties);
			if (successMessage) {
				MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION);
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
		for (final FormProperty fp : this.getFormProperties().values()) {
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
			for (final FormProperty fp : this.getFormProperties().values()) {
				if (fp != null && fp.getText() != null && fp.getText().isFocusControl()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void setRadix(final int radix) {
		updateModelValues(true);
		super.setRadix(radix);
		updateFormValues();
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	public Map<String, FormProperty> getFormProperties() {
		return formProperties;
	}

	public Map<Bike.Type, TorqueGraph> getTorqueGraphs() {
		return torqueGraphs;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public Tabs getTabs() {
		return tabs;
	}

	public Map<String, Integer> getLastPersistedProperties() {
		return lastPersistedProperties;
	}

	public Map<String, Integer> getDefaultProperties() {
		return Collections.unmodifiableMap(defaultProperties);
	}

	public TextFormatter getTextFormatter() {
		return textFormatter;
	}

}
