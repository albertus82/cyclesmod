package it.albertus.cyclesmod.gui;

import java.util.logging.Level;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.common.engine.NumeralSystem;
import it.albertus.cyclesmod.common.model.BikeType;
import it.albertus.cyclesmod.common.resources.ConfigurableMessages;
import it.albertus.cyclesmod.common.resources.Language;
import it.albertus.cyclesmod.gui.listener.AboutListener;
import it.albertus.cyclesmod.gui.listener.ArmMenuListener;
import it.albertus.cyclesmod.gui.listener.CloseListener;
import it.albertus.cyclesmod.gui.listener.CopySelectionListener;
import it.albertus.cyclesmod.gui.listener.CutSelectionListener;
import it.albertus.cyclesmod.gui.listener.EditMenuListener;
import it.albertus.cyclesmod.gui.listener.LanguageSelectionListener;
import it.albertus.cyclesmod.gui.listener.OpenPowerGraphDialogListener;
import it.albertus.cyclesmod.gui.listener.OpenSelectionListener;
import it.albertus.cyclesmod.gui.listener.PasteSelectionListener;
import it.albertus.cyclesmod.gui.listener.RadixSelectionListener;
import it.albertus.cyclesmod.gui.listener.ResetAllSelectionListener;
import it.albertus.cyclesmod.gui.listener.ResetSingleSelectionListener;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.jface.i18n.LocalizedWidgets;
import it.albertus.jface.sysinfo.SystemInformationDialog;
import it.albertus.util.ISupplier;
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

	@Getter(AccessLevel.NONE) private final LocalizedWidgets localizedWidgets = new LocalizedWidgets();

	MenuBar(@NonNull final CyclesModGui gui) {
		final CloseListener closeListener = new CloseListener(gui);
		final AboutListener aboutListener = new AboutListener(gui);

		boolean cocoaMenuCreated = false;
		if (Util.isCocoa()) {
			try {
				new CocoaUIEnhancer(gui.getShell().getDisplay()).hookApplicationMenu(closeListener, aboutListener, null);
				cocoaMenuCreated = true;
			}
			catch (final CocoaEnhancerException cee) {
				log.log(Level.WARNING, messages.get("err.cocoa.enhancer"), cee);
			}
		}

		final Menu bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		// File
		final Menu fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		newLocalizedMenuItem(bar, SWT.CASCADE, "lbl.menu.header.file").setMenu(fileMenu);

		final MenuItem fileOpenMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, () -> messages.get("lbl.menu.item.open") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_OPEN));
		fileOpenMenuItem.addSelectionListener(new OpenSelectionListener(gui));
		fileOpenMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_OPEN);

		final MenuItem fileSaveMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, () -> messages.get("lbl.menu.item.save") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		fileSaveMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.save();
			}
		});
		fileSaveMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SAVE);

		newLocalizedMenuItem(fileMenu, SWT.PUSH, "lbl.menu.item.saveas").addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.saveAs();
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			newLocalizedMenuItem(fileMenu, SWT.PUSH, "lbl.menu.item.exit").addSelectionListener(closeListener);
		}

		// Edit
		final Menu editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem editMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, "lbl.menu.header.edit");
		editMenuHeader.setMenu(editMenu);
		final EditMenuListener editMenuListener = new EditMenuListener(gui);
		editMenu.addMenuListener(editMenuListener);
		editMenuHeader.addArmListener(editMenuListener);

		editCutMenuItem = newLocalizedMenuItem(editMenu, SWT.PUSH, () -> messages.get("lbl.menu.item.cut") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_CUT));
		editCutMenuItem.addSelectionListener(new CutSelectionListener(gui::getTabs));
		editCutMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_CUT);

		editCopyMenuItem = newLocalizedMenuItem(editMenu, SWT.PUSH, () -> messages.get("lbl.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopySelectionListener(gui::getTabs));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_COPY);

		editPasteMenuItem = newLocalizedMenuItem(editMenu, SWT.PUSH, () -> messages.get("lbl.menu.item.paste") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_PASTE));
		editPasteMenuItem.addSelectionListener(new PasteSelectionListener(gui::getTabs));
		editPasteMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_PASTE);

		new MenuItem(editMenu, SWT.SEPARATOR);

		final MenuItem editPowerSubMenuItem = newLocalizedMenuItem(editMenu, SWT.CASCADE, "lbl.menu.item.power.curve");

		final Menu editPowerSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editPowerSubMenuItem.setMenu(editPowerSubMenu);

		for (final BikeType bikeType : BikeType.values()) {
			newLocalizedMenuItem(editPowerSubMenu, SWT.PUSH, () -> messages.get("lbl.menu.item.power.curve.bike", bikeType.getDisplacement())).addSelectionListener(new OpenPowerGraphDialogListener(gui, bikeType));
		}

		new MenuItem(editMenu, SWT.SEPARATOR);

		final MenuItem editResetSubMenuItem = newLocalizedMenuItem(editMenu, SWT.CASCADE, "lbl.menu.item.reset");

		final Menu editResetSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editResetSubMenuItem.setMenu(editResetSubMenu);

		newLocalizedMenuItem(editResetSubMenu, SWT.PUSH, "lbl.menu.item.reset.single").addSelectionListener(new ResetSingleSelectionListener(gui));

		newLocalizedMenuItem(editResetSubMenu, SWT.PUSH, "lbl.menu.item.reset.all").addSelectionListener(new ResetAllSelectionListener(gui));

		// View
		final Menu viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		newLocalizedMenuItem(bar, SWT.CASCADE, "lbl.menu.header.view").setMenu(viewMenu);

		final MenuItem viewRadixSubMenuItem = newLocalizedMenuItem(viewMenu, SWT.CASCADE, "lbl.menu.item.radix");
		final Menu viewRadixSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewRadixSubMenuItem.setMenu(viewRadixSubMenu);

		final RadixSelectionListener radixSelectionListener = new RadixSelectionListener(gui);

		for (final NumeralSystem numeralSystem : NumeralSystem.values()) {
			final MenuItem radixMenuItem = newLocalizedMenuItem(viewRadixSubMenu, SWT.RADIO, () -> messages.get("lbl.menu.item.radix." + numeralSystem.getRadix()));
			radixMenuItem.setData(numeralSystem);
			radixMenuItem.addSelectionListener(radixSelectionListener);
			if (numeralSystem.equals(gui.getNumeralSystem())) {
				radixMenuItem.setSelection(true);
			}
		}

		new MenuItem(viewMenu, SWT.SEPARATOR);

		final MenuItem viewLanguageSubMenuItem = newLocalizedMenuItem(viewMenu, SWT.CASCADE, "lbl.menu.item.language");
		final Menu viewLanguageSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewLanguageSubMenuItem.setMenu(viewLanguageSubMenu);

		final LanguageSelectionListener languageSelectionListener = new LanguageSelectionListener(gui);

		for (final Language language : Language.values()) {
			final MenuItem languageMenuItem = new MenuItem(viewLanguageSubMenu, SWT.RADIO);
			languageMenuItem.setText(language.getLocale().getDisplayLanguage(language.getLocale()));
			languageMenuItem.setData(language);
			languageMenuItem.addSelectionListener(languageSelectionListener);
			if (language.equals(messages.getLanguage())) {
				languageMenuItem.setSelection(true);
			}
		}

		// Help
		final Menu helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem helpMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, Util.isWindows() ? "lbl.menu.header.help.windows" : "lbl.menu.header.help");
		helpMenuHeader.setMenu(helpMenu);

		final MenuItem helpSystemInfoItem = newLocalizedMenuItem(helpMenu, SWT.PUSH, "lbl.menu.item.system.info");
		helpSystemInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				SystemInformationDialog.open(gui.getShell());
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			newLocalizedMenuItem(helpMenu, SWT.PUSH, "lbl.menu.item.about").addSelectionListener(new AboutListener(gui));
		}

		final ArmMenuListener helpMenuListener = e -> helpSystemInfoItem.setEnabled(SystemInformationDialog.isAvailable());
		helpMenu.addMenuListener(helpMenuListener);
		helpMenuHeader.addArmListener(helpMenuListener);

		gui.getShell().setMenuBar(bar);
	}

	@Override
	public void updateLanguage() {
		localizedWidgets.resetAllTexts();
	}

	private MenuItem newLocalizedMenuItem(@NonNull final Menu parent, final int style, @NonNull final String messageKey) {
		return newLocalizedMenuItem(parent, style, () -> messages.get(messageKey));
	}

	private MenuItem newLocalizedMenuItem(@NonNull final Menu parent, final int style, @NonNull final ISupplier<String> textSupplier) {
		return localizedWidgets.putAndReturn(new MenuItem(parent, style), textSupplier).getKey();
	}

}
