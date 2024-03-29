package io.github.albertus82.cyclesmod.gui;

import java.util.Locale;
import java.util.logging.Level;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import io.github.albertus82.cyclesmod.common.engine.NumeralSystem;
import io.github.albertus82.cyclesmod.common.model.VehicleType;
import io.github.albertus82.cyclesmod.common.resources.ConfigurableMessages;
import io.github.albertus82.cyclesmod.common.resources.Language;
import io.github.albertus82.cyclesmod.gui.listener.AboutListener;
import io.github.albertus82.cyclesmod.gui.listener.ArmMenuListener;
import io.github.albertus82.cyclesmod.gui.listener.CopySelectionListener;
import io.github.albertus82.cyclesmod.gui.listener.CutSelectionListener;
import io.github.albertus82.cyclesmod.gui.listener.EditMenuListener;
import io.github.albertus82.cyclesmod.gui.listener.ExitListener;
import io.github.albertus82.cyclesmod.gui.listener.OpenPowerGraphDialogListener;
import io.github.albertus82.cyclesmod.gui.listener.PasteSelectionListener;
import io.github.albertus82.cyclesmod.gui.resources.GuiMessages;
import io.github.albertus82.jface.Multilanguage;
import io.github.albertus82.jface.SwtUtils;
import io.github.albertus82.jface.cocoa.CocoaEnhancerException;
import io.github.albertus82.jface.cocoa.CocoaUIEnhancer;
import io.github.albertus82.jface.i18n.LocalizedWidgets;
import io.github.albertus82.jface.sysinfo.SystemInformationDialog;
import io.github.albertus82.util.ISupplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
@Log
@Getter
public class MenuBar implements Multilanguage {

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private final MenuItem editCutMenuItem;
	private final MenuItem editCopyMenuItem;
	private final MenuItem editPasteMenuItem;

	@Getter(AccessLevel.NONE)
	private final LocalizedWidgets localizedWidgets = new LocalizedWidgets();

	MenuBar(@NonNull final CyclesModGui gui) {
		final ExitListener exitListener = new ExitListener(gui);
		final AboutListener aboutListener = new AboutListener(gui);

		boolean cocoaMenuCreated = false;
		if (Util.isCocoa()) {
			try {
				new CocoaUIEnhancer(gui.getShell().getDisplay()).hookApplicationMenu(exitListener, aboutListener, null);
				cocoaMenuCreated = true;
			}
			catch (final CocoaEnhancerException e) {
				log.log(Level.WARNING, "Unable to enhance Cocoa UI:", e);
			}
		}

		final Menu bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		// File
		final Menu fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem fileMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, "gui.label.menu.header.file");
		fileMenuHeader.setMenu(fileMenu);

		final MenuItem fileOpenMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.open") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_OPEN));
		fileOpenMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.open();
			}
		});
		fileOpenMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_OPEN);

		final MenuItem fileCloseMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, "gui.label.menu.item.close");
		fileCloseMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.close();
			}
		});

		new MenuItem(fileMenu, SWT.SEPARATOR);

		final MenuItem fileSaveMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.save") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		fileSaveMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.save();
			}
		});
		fileSaveMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SAVE);

		newLocalizedMenuItem(fileMenu, SWT.PUSH, "gui.label.menu.item.saveas").addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.saveAs();
			}
		});

		new MenuItem(fileMenu, SWT.SEPARATOR);

		final MenuItem fileImportSubMenuItem = newLocalizedMenuItem(fileMenu, SWT.CASCADE, "gui.label.menu.item.import");

		final Menu fileImportSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileImportSubMenuItem.setMenu(fileImportSubMenu);

		newLocalizedMenuItem(fileImportSubMenu, SWT.CASCADE, "gui.label.menu.item.import.cfg").addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.importCfg();
			}
		});

		newLocalizedMenuItem(fileImportSubMenu, SWT.PUSH, "gui.label.menu.item.import.hiddenCfg").addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.loadHiddenCfg(VehicleType.values()[gui.getTabs().getTabFolder().getSelectionIndex()]);
			}
		});

		final MenuItem fileExportAsSubMenuItem = newLocalizedMenuItem(fileMenu, SWT.CASCADE, "gui.label.menu.item.export");

		final Menu fileExportAsSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileExportAsSubMenuItem.setMenu(fileExportAsSubMenu);

		final MenuItem fileExportAsCfgSubMenuItem = newLocalizedMenuItem(fileExportAsSubMenu, SWT.CASCADE, "gui.label.menu.item.export.cfg");

		final Menu fileExportAsCfgSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileExportAsCfgSubMenuItem.setMenu(fileExportAsCfgSubMenu);

		newLocalizedMenuItem(fileExportAsCfgSubMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.export.single." + gui.getMode().getGame().toString().toLowerCase(Locale.ROOT))).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.exportCfgSingle(VehicleType.values()[gui.getTabs().getTabFolder().getSelectionIndex()]);
			}
		});

		newLocalizedMenuItem(fileExportAsCfgSubMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.export.all." + gui.getMode().getGame().toString().toLowerCase(Locale.ROOT))).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.exportCfgAll();
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			newLocalizedMenuItem(fileMenu, SWT.PUSH, "gui.label.menu.item.exit").addSelectionListener(exitListener);
		}

		// Edit
		final Menu editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem editMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, "gui.label.menu.header.edit");
		editMenuHeader.setMenu(editMenu);
		final EditMenuListener editMenuListener = new EditMenuListener(gui);
		editMenu.addMenuListener(editMenuListener);
		editMenuHeader.addArmListener(editMenuListener);

		editCutMenuItem = newLocalizedMenuItem(editMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.cut") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_CUT));
		editCutMenuItem.addSelectionListener(new CutSelectionListener(gui));
		editCutMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_CUT);

		editCopyMenuItem = newLocalizedMenuItem(editMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopySelectionListener(gui));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_COPY);

		editPasteMenuItem = newLocalizedMenuItem(editMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.paste") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_PASTE));
		editPasteMenuItem.addSelectionListener(new PasteSelectionListener(gui));
		editPasteMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_PASTE);

		new MenuItem(editMenu, SWT.SEPARATOR);

		final MenuItem editPowerSubMenuItem = newLocalizedMenuItem(editMenu, SWT.CASCADE, "gui.label.menu.item.power.curve");

		final Menu editPowerSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editPowerSubMenuItem.setMenu(editPowerSubMenu);

		for (final VehicleType vehicleType : VehicleType.values()) {
			newLocalizedMenuItem(editPowerSubMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.power.curve.vehicle", vehicleType.getDescription(gui.getMode().getGame()))).addSelectionListener(new OpenPowerGraphDialogListener(gui, vehicleType));
		}

		new MenuItem(editMenu, SWT.SEPARATOR);

		final MenuItem editResetSubMenuItem = newLocalizedMenuItem(editMenu, SWT.CASCADE, "gui.label.menu.item.reset");

		final Menu editResetSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editResetSubMenuItem.setMenu(editResetSubMenu);

		newLocalizedMenuItem(editResetSubMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.reset.single." + gui.getMode().getGame().toString().toLowerCase(Locale.ROOT))).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.resetSingle(VehicleType.values()[gui.getTabs().getTabFolder().getSelectionIndex()]);
			}
		});

		newLocalizedMenuItem(editResetSubMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.reset.all." + gui.getMode().getGame().toString().toLowerCase(Locale.ROOT))).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.resetAll();
			}
		});

		// View
		final Menu viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		newLocalizedMenuItem(bar, SWT.CASCADE, "gui.label.menu.header.view").setMenu(viewMenu);

		final MenuItem viewRadixSubMenuItem = newLocalizedMenuItem(viewMenu, SWT.CASCADE, "gui.label.menu.item.radix");
		final Menu viewRadixSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewRadixSubMenuItem.setMenu(viewRadixSubMenu);

		for (final NumeralSystem numeralSystem : NumeralSystem.values()) {
			final MenuItem radixMenuItem = newLocalizedMenuItem(viewRadixSubMenu, SWT.RADIO, () -> messages.get("gui.label.menu.item.radix." + numeralSystem.getRadix()));
			radixMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					gui.setNumeralSystem(numeralSystem);
				}
			});
			if (numeralSystem.equals(gui.getNumeralSystem())) {
				radixMenuItem.setSelection(true);
			}
		}

		new MenuItem(viewMenu, SWT.SEPARATOR);

		final MenuItem viewLanguageSubMenuItem = newLocalizedMenuItem(viewMenu, SWT.CASCADE, "gui.label.menu.item.language");
		final Menu viewLanguageSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewLanguageSubMenuItem.setMenu(viewLanguageSubMenu);

		for (final Language language : Language.values()) {
			final MenuItem languageMenuItem = new MenuItem(viewLanguageSubMenu, SWT.RADIO);
			languageMenuItem.setText(language.getLocale().getDisplayLanguage(language.getLocale()));
			languageMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					gui.setLanguage(language);
				}
			});
			if (language.equals(messages.getLanguage())) {
				languageMenuItem.setSelection(true);
			}
		}

		// Help
		final Menu helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem helpMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, Util.isWindows() ? "gui.label.menu.header.help.windows" : "gui.label.menu.header.help");
		helpMenuHeader.setMenu(helpMenu);

		final MenuItem helpSystemInfoItem = newLocalizedMenuItem(helpMenu, SWT.PUSH, "gui.label.menu.item.system.info");
		helpSystemInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				SystemInformationDialog.open(gui.getShell());
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			newLocalizedMenuItem(helpMenu, SWT.PUSH, "gui.label.menu.item.about").addSelectionListener(new AboutListener(gui));
		}

		final ArmMenuListener fileMenuListener = e -> fileCloseMenuItem.setEnabled(gui.getCurrentFileName() != null && !gui.getCurrentFileName().isEmpty());
		fileMenu.addMenuListener(fileMenuListener);
		fileMenuHeader.addArmListener(fileMenuListener);
		final ArmMenuListener helpMenuListener = e -> helpSystemInfoItem.setEnabled(SystemInformationDialog.isAvailable());
		helpMenu.addMenuListener(helpMenuListener);
		helpMenuHeader.addArmListener(helpMenuListener);

		gui.getShell().setMenuBar(bar);
	}

	@Override
	public void updateLanguage() {
		localizedWidgets.resetAllTexts();
	}

	public void updateModeSpecificWidgets() {
		updateLanguage();
	}

	private MenuItem newLocalizedMenuItem(@NonNull final Menu parent, final int style, @NonNull final String messageKey) {
		return newLocalizedMenuItem(parent, style, () -> messages.get(messageKey));
	}

	private MenuItem newLocalizedMenuItem(@NonNull final Menu parent, final int style, @NonNull final ISupplier<String> textSupplier) {
		return localizedWidgets.putAndReturn(new MenuItem(parent, style), textSupplier).getKey();
	}

}
