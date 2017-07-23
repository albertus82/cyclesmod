package it.albertus.cycles.gui;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cycles.engine.NumeralSystem;
import it.albertus.cycles.gui.listener.AboutListener;
import it.albertus.cycles.gui.listener.CloseListener;
import it.albertus.cycles.gui.listener.CopySelectionListener;
import it.albertus.cycles.gui.listener.CutSelectionListener;
import it.albertus.cycles.gui.listener.EditMenuListener;
import it.albertus.cycles.gui.listener.LanguageSelectionListener;
import it.albertus.cycles.gui.listener.OpenSelectionListener;
import it.albertus.cycles.gui.listener.OpenTorqueGraphDialogListener;
import it.albertus.cycles.gui.listener.PasteSelectionListener;
import it.albertus.cycles.gui.listener.RadixSelectionListener;
import it.albertus.cycles.gui.listener.ResetAllSelectionListener;
import it.albertus.cycles.gui.listener.ResetSingleSelectionListener;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.resources.Messages;
import it.albertus.cycles.resources.Messages.Language;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.util.logging.LoggerFactory;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
public class MenuBar {

	private static final Logger logger = LoggerFactory.getLogger(MenuBar.class);

	private final MenuItem fileMenuHeader;
	private final MenuItem fileOpenMenuItem;
	private final MenuItem fileSaveMenuItem;
	private final MenuItem fileSaveAsMenuItem;
	private MenuItem fileExitMenuItem;

	private final MenuItem editMenuHeader;
	private final MenuItem editCutMenuItem;
	private final MenuItem editCopyMenuItem;
	private final MenuItem editPasteMenuItem;
	private final MenuItem editTorqueSubMenuItem;
	private final Map<BikeType, MenuItem> editTorqueMenuItems = new EnumMap<BikeType, MenuItem>(BikeType.class);
	private final MenuItem editResetSubMenuItem;
	private final MenuItem editResetSingleMenuItem;
	private final MenuItem editResetAllMenuItem;

	private final MenuItem viewMenuHeader;
	private final MenuItem viewRadixSubMenuItem;
	private final Map<NumeralSystem, MenuItem> viewRadixMenuItems = new EnumMap<NumeralSystem, MenuItem>(NumeralSystem.class);
	private final MenuItem viewLanguageSubMenuItem;
	private final Map<Language, MenuItem> viewLanguageMenuItems = new EnumMap<Language, MenuItem>(Language.class);

	private MenuItem helpMenuHeader;
	private MenuItem helpAboutMenuItem;

	MenuBar(final CyclesModGui gui) {
		final CloseListener closeListener = new CloseListener(gui);
		final AboutListener aboutListener = new AboutListener(gui);

		boolean cocoaMenuCreated = false;
		if (Util.isCocoa()) {
			try {
				new CocoaUIEnhancer(gui.getShell().getDisplay()).hookApplicationMenu(closeListener, aboutListener, null);
				cocoaMenuCreated = true;
			}
			catch (final CocoaEnhancerException cee) {
				logger.log(Level.WARNING, Messages.get("err.cocoa.enhancer"), cee);
			}
		}

		final Menu bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		// File
		final Menu fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setText(Messages.get("lbl.menu.header.file"));
		fileMenuHeader.setMenu(fileMenu);

		fileOpenMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenMenuItem.setText(Messages.get("lbl.menu.item.open") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_OPEN));
		fileOpenMenuItem.addSelectionListener(new OpenSelectionListener(gui));
		fileOpenMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_OPEN);

		fileSaveMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveMenuItem.setText(Messages.get("lbl.menu.item.save") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		fileSaveMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.save();
			}
		});
		fileSaveMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SAVE);

		fileSaveAsMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveAsMenuItem.setText(Messages.get("lbl.menu.item.saveas"));
		fileSaveAsMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.saveAs();
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitMenuItem.setText(Messages.get("lbl.menu.item.exit"));
			fileExitMenuItem.addSelectionListener(closeListener);
		}

		// Edit
		final Menu editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editMenuHeader = new MenuItem(bar, SWT.CASCADE);
		editMenuHeader.setText(Messages.get("lbl.menu.header.edit"));
		editMenuHeader.setMenu(editMenu);
		final EditMenuListener editMenuListener = new EditMenuListener(gui);
		editMenu.addMenuListener(editMenuListener);
		editMenuHeader.addArmListener(editMenuListener);

		editCutMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCutMenuItem.setText(Messages.get("lbl.menu.item.cut") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_CUT));
		editCutMenuItem.addSelectionListener(new CutSelectionListener(gui));
		editCutMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_CUT);

		editCopyMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCopyMenuItem.setText(Messages.get("lbl.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopySelectionListener(gui));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_COPY);

		editPasteMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editPasteMenuItem.setText(Messages.get("lbl.menu.item.paste") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_PASTE));
		editPasteMenuItem.addSelectionListener(new PasteSelectionListener(gui));
		editPasteMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_PASTE);

		new MenuItem(editMenu, SWT.SEPARATOR);

		editTorqueSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editTorqueSubMenuItem.setText(Messages.get("lbl.menu.item.torque.curve"));

		final Menu editTorqueSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editTorqueSubMenuItem.setMenu(editTorqueSubMenu);

		for (final BikeType bikeType : BikeType.values()) {
			final MenuItem editTorqueMenuItem = new MenuItem(editTorqueSubMenu, SWT.PUSH);
			editTorqueMenuItem.setText(Messages.get("lbl.menu.item.torque.curve.bike", bikeType.getDisplacement()));
			editTorqueMenuItem.addSelectionListener(new OpenTorqueGraphDialogListener(gui, bikeType));
			editTorqueMenuItems.put(bikeType, editTorqueMenuItem);
		}

		new MenuItem(editMenu, SWT.SEPARATOR);

		editResetSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editResetSubMenuItem.setText(Messages.get("lbl.menu.item.reset"));

		final Menu editResetSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editResetSubMenuItem.setMenu(editResetSubMenu);

		editResetSingleMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetSingleMenuItem.setText(Messages.get("lbl.menu.item.reset.single"));
		editResetSingleMenuItem.addSelectionListener(new ResetSingleSelectionListener(gui));

		editResetAllMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetAllMenuItem.setText(Messages.get("lbl.menu.item.reset.all"));
		editResetAllMenuItem.addSelectionListener(new ResetAllSelectionListener(gui));

		// View
		final Menu viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewMenuHeader = new MenuItem(bar, SWT.CASCADE);
		viewMenuHeader.setText(Messages.get("lbl.menu.header.view"));
		viewMenuHeader.setMenu(viewMenu);

		viewRadixSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		viewRadixSubMenuItem.setText(Messages.get("lbl.menu.item.radix"));

		final Menu viewRadixSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewRadixSubMenuItem.setMenu(viewRadixSubMenu);

		final RadixSelectionListener radixSelectionListener = new RadixSelectionListener(gui);

		for (final NumeralSystem numeralSystem : NumeralSystem.values()) {
			final MenuItem radixMenuItem = new MenuItem(viewRadixSubMenu, SWT.RADIO);
			radixMenuItem.setText(Messages.get("lbl.menu.item.radix." + numeralSystem.getRadix()));
			radixMenuItem.setData(numeralSystem);
			radixMenuItem.addSelectionListener(radixSelectionListener);
			viewRadixMenuItems.put(numeralSystem, radixMenuItem);
		}

		viewRadixMenuItems.get(gui.getNumeralSystem()).setSelection(true); // Default

		new MenuItem(viewMenu, SWT.SEPARATOR);

		viewLanguageSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		viewLanguageSubMenuItem.setText(Messages.get("lbl.menu.item.language"));

		final Menu viewLanguageSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewLanguageSubMenuItem.setMenu(viewLanguageSubMenu);

		final LanguageSelectionListener languageSelectionListener = new LanguageSelectionListener(gui);

		for (final Language language : Language.values()) {
			final MenuItem languageMenuItem = new MenuItem(viewLanguageSubMenu, SWT.RADIO);
			languageMenuItem.setText(language.getLocale().getDisplayLanguage(language.getLocale()));
			languageMenuItem.setData(language);
			languageMenuItem.addSelectionListener(languageSelectionListener);
			viewLanguageMenuItems.put(language, languageMenuItem);
		}

		viewLanguageMenuItems.get(Messages.getLanguage()).setSelection(true); // Default

		// Help
		if (!cocoaMenuCreated) {
			final Menu helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
			helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
			helpMenuHeader.setText(Messages.get("lbl.menu.header.help"));
			helpMenuHeader.setMenu(helpMenu);

			helpAboutMenuItem = new MenuItem(helpMenu, SWT.PUSH);
			helpAboutMenuItem.setText(Messages.get("lbl.menu.item.about"));
			helpAboutMenuItem.addSelectionListener(aboutListener);
		}

		gui.getShell().setMenuBar(bar);
	}

	public void updateTexts() {
		fileMenuHeader.setText(Messages.get("lbl.menu.header.file"));
		fileOpenMenuItem.setText(Messages.get("lbl.menu.item.open") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_OPEN));
		fileSaveMenuItem.setText(Messages.get("lbl.menu.item.save") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		fileSaveAsMenuItem.setText(Messages.get("lbl.menu.item.saveas"));
		if (fileExitMenuItem != null && !fileExitMenuItem.isDisposed()) {
			fileExitMenuItem.setText(Messages.get("lbl.menu.item.exit"));
		}
		editMenuHeader.setText(Messages.get("lbl.menu.header.edit"));
		editCutMenuItem.setText(Messages.get("lbl.menu.item.cut") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_CUT));
		editCopyMenuItem.setText(Messages.get("lbl.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editPasteMenuItem.setText(Messages.get("lbl.menu.item.paste") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_PASTE));
		editResetSubMenuItem.setText(Messages.get("lbl.menu.item.reset"));
		editResetSingleMenuItem.setText(Messages.get("lbl.menu.item.reset.single"));
		editResetAllMenuItem.setText(Messages.get("lbl.menu.item.reset.all"));
		viewMenuHeader.setText(Messages.get("lbl.menu.header.view"));
		viewRadixSubMenuItem.setText(Messages.get("lbl.menu.item.radix"));
		for (final Entry<NumeralSystem, MenuItem> entry : viewRadixMenuItems.entrySet()) {
			entry.getValue().setText(Messages.get("lbl.menu.item.radix." + entry.getKey().getRadix()));
		}
		editTorqueSubMenuItem.setText(Messages.get("lbl.menu.item.torque.curve"));
		for (final Entry<BikeType, MenuItem> entry : editTorqueMenuItems.entrySet()) {
			entry.getValue().setText(Messages.get("lbl.menu.item.torque.curve.bike", entry.getKey().getDisplacement()));
		}
		viewLanguageSubMenuItem.setText(Messages.get("lbl.menu.item.language"));
		for (final Entry<Language, MenuItem> entry : viewLanguageMenuItems.entrySet()) {
			entry.getValue().setText(entry.getKey().getLocale().getDisplayLanguage(entry.getKey().getLocale()));
		}
		if (helpMenuHeader != null && !helpMenuHeader.isDisposed()) {
			helpMenuHeader.setText(Messages.get("lbl.menu.header.help"));
		}
		if (helpAboutMenuItem != null && !helpAboutMenuItem.isDisposed()) {
			helpAboutMenuItem.setText(Messages.get("lbl.menu.item.about"));
		}
	}

	public MenuItem getEditCutMenuItem() {
		return editCutMenuItem;
	}

	public MenuItem getEditCopyMenuItem() {
		return editCopyMenuItem;
	}

	public MenuItem getEditPasteMenuItem() {
		return editPasteMenuItem;
	}

}
