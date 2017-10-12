package it.albertus.cyclesmod.gui;

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

import it.albertus.cyclesmod.engine.NumeralSystem;
import it.albertus.cyclesmod.gui.listener.AboutListener;
import it.albertus.cyclesmod.gui.listener.CloseListener;
import it.albertus.cyclesmod.gui.listener.CopySelectionListener;
import it.albertus.cyclesmod.gui.listener.CutSelectionListener;
import it.albertus.cyclesmod.gui.listener.EditMenuListener;
import it.albertus.cyclesmod.gui.listener.HelpMenuListener;
import it.albertus.cyclesmod.gui.listener.LanguageSelectionListener;
import it.albertus.cyclesmod.gui.listener.OpenSelectionListener;
import it.albertus.cyclesmod.gui.listener.OpenTorqueGraphDialogListener;
import it.albertus.cyclesmod.gui.listener.PasteSelectionListener;
import it.albertus.cyclesmod.gui.listener.RadixSelectionListener;
import it.albertus.cyclesmod.gui.listener.ResetAllSelectionListener;
import it.albertus.cyclesmod.gui.listener.ResetSingleSelectionListener;
import it.albertus.cyclesmod.model.Bike.BikeType;
import it.albertus.cyclesmod.resources.Messages;
import it.albertus.cyclesmod.resources.Messages.Language;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.jface.sysinfo.SystemInformationDialog;
import it.albertus.util.logging.LoggerFactory;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
public class MenuBar {

	private static final String LBL_MENU_HEADER_FILE = "lbl.menu.header.file";
	private static final String LBL_MENU_ITEM_OPEN = "lbl.menu.item.open";
	private static final String LBL_MENU_ITEM_SAVE = "lbl.menu.item.save";
	private static final String LBL_MENU_ITEM_SAVEAS = "lbl.menu.item.saveas";
	private static final String LBL_MENU_ITEM_EXIT = "lbl.menu.item.exit";
	private static final String LBL_MENU_HEADER_EDIT = "lbl.menu.header.edit";
	private static final String LBL_MENU_ITEM_CUT = "lbl.menu.item.cut";
	private static final String LBL_MENU_ITEM_COPY = "lbl.menu.item.copy";
	private static final String LBL_MENU_ITEM_PASTE = "lbl.menu.item.paste";
	private static final String LBL_MENU_ITEM_TORQUE_CURVE = "lbl.menu.item.torque.curve";
	private static final String LBL_MENU_ITEM_TORQUE_CURVE_BIKE = "lbl.menu.item.torque.curve.bike";
	private static final String LBL_MENU_ITEM_RESET = "lbl.menu.item.reset";
	private static final String LBL_MENU_ITEM_RESET_SINGLE = "lbl.menu.item.reset.single";
	private static final String LBL_MENU_ITEM_RESET_ALL = "lbl.menu.item.reset.all";
	private static final String LBL_MENU_HEADER_VIEW = "lbl.menu.header.view";
	private static final String LBL_MENU_ITEM_RADIX = "lbl.menu.item.radix";
	private static final String LBL_MENU_ITEM_LANGUAGE = "lbl.menu.item.language";
	private static final String LBL_MENU_HEADER_HELP = "lbl.menu.header.help";
	private static final String LBL_MENU_HEADER_HELP_WINDOWS = "lbl.menu.header.help.windows";
	private static final String LBL_MENU_ITEM_SYSTEM_INFO = "lbl.menu.item.system.info";
	private static final String LBL_MENU_ITEM_ABOUT = "lbl.menu.item.about";

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

	private final MenuItem helpMenuHeader;
	private final MenuItem helpSystemInfoItem;
	private MenuItem helpAboutItem;

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
		fileMenuHeader.setText(Messages.get(LBL_MENU_HEADER_FILE));
		fileMenuHeader.setMenu(fileMenu);

		fileOpenMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenMenuItem.setText(Messages.get(LBL_MENU_ITEM_OPEN) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_OPEN));
		fileOpenMenuItem.addSelectionListener(new OpenSelectionListener(gui));
		fileOpenMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_OPEN);

		fileSaveMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveMenuItem.setText(Messages.get(LBL_MENU_ITEM_SAVE) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		fileSaveMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.save();
			}
		});
		fileSaveMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SAVE);

		fileSaveAsMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveAsMenuItem.setText(Messages.get(LBL_MENU_ITEM_SAVEAS));
		fileSaveAsMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.saveAs();
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitMenuItem.setText(Messages.get(LBL_MENU_ITEM_EXIT));
			fileExitMenuItem.addSelectionListener(closeListener);
		}

		// Edit
		final Menu editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editMenuHeader = new MenuItem(bar, SWT.CASCADE);
		editMenuHeader.setText(Messages.get(LBL_MENU_HEADER_EDIT));
		editMenuHeader.setMenu(editMenu);
		final EditMenuListener editMenuListener = new EditMenuListener(gui);
		editMenu.addMenuListener(editMenuListener);
		editMenuHeader.addArmListener(editMenuListener);

		editCutMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCutMenuItem.setText(Messages.get(LBL_MENU_ITEM_CUT) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_CUT));
		editCutMenuItem.addSelectionListener(new CutSelectionListener(gui));
		editCutMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_CUT);

		editCopyMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCopyMenuItem.setText(Messages.get(LBL_MENU_ITEM_COPY) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopySelectionListener(gui));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_COPY);

		editPasteMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editPasteMenuItem.setText(Messages.get(LBL_MENU_ITEM_PASTE) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_PASTE));
		editPasteMenuItem.addSelectionListener(new PasteSelectionListener(gui));
		editPasteMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_PASTE);

		new MenuItem(editMenu, SWT.SEPARATOR);

		editTorqueSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editTorqueSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_TORQUE_CURVE));

		final Menu editTorqueSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editTorqueSubMenuItem.setMenu(editTorqueSubMenu);

		for (final BikeType bikeType : BikeType.values()) {
			final MenuItem editTorqueMenuItem = new MenuItem(editTorqueSubMenu, SWT.PUSH);
			editTorqueMenuItem.setText(Messages.get(LBL_MENU_ITEM_TORQUE_CURVE_BIKE, bikeType.getDisplacement()));
			editTorqueMenuItem.addSelectionListener(new OpenTorqueGraphDialogListener(gui, bikeType));
			editTorqueMenuItems.put(bikeType, editTorqueMenuItem);
		}

		new MenuItem(editMenu, SWT.SEPARATOR);

		editResetSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editResetSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_RESET));

		final Menu editResetSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editResetSubMenuItem.setMenu(editResetSubMenu);

		editResetSingleMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetSingleMenuItem.setText(Messages.get(LBL_MENU_ITEM_RESET_SINGLE));
		editResetSingleMenuItem.addSelectionListener(new ResetSingleSelectionListener(gui));

		editResetAllMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetAllMenuItem.setText(Messages.get(LBL_MENU_ITEM_RESET_ALL));
		editResetAllMenuItem.addSelectionListener(new ResetAllSelectionListener(gui));

		// View
		final Menu viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewMenuHeader = new MenuItem(bar, SWT.CASCADE);
		viewMenuHeader.setText(Messages.get(LBL_MENU_HEADER_VIEW));
		viewMenuHeader.setMenu(viewMenu);

		viewRadixSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		viewRadixSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_RADIX));

		final Menu viewRadixSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewRadixSubMenuItem.setMenu(viewRadixSubMenu);

		final RadixSelectionListener radixSelectionListener = new RadixSelectionListener(gui);

		for (final NumeralSystem numeralSystem : NumeralSystem.values()) {
			final MenuItem radixMenuItem = new MenuItem(viewRadixSubMenu, SWT.RADIO);
			radixMenuItem.setText(Messages.get(LBL_MENU_ITEM_RADIX + '.' + numeralSystem.getRadix()));
			radixMenuItem.setData(numeralSystem);
			radixMenuItem.addSelectionListener(radixSelectionListener);
			viewRadixMenuItems.put(numeralSystem, radixMenuItem);
		}

		viewRadixMenuItems.get(gui.getNumeralSystem()).setSelection(true); // Default

		new MenuItem(viewMenu, SWT.SEPARATOR);

		viewLanguageSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		viewLanguageSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_LANGUAGE));

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
		final Menu helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
		helpMenuHeader.setText(Messages.get(Util.isWindows() ? LBL_MENU_HEADER_HELP_WINDOWS : LBL_MENU_HEADER_HELP));
		helpMenuHeader.setMenu(helpMenu);

		helpSystemInfoItem = new MenuItem(helpMenu, SWT.PUSH);
		helpSystemInfoItem.setText(Messages.get(LBL_MENU_ITEM_SYSTEM_INFO));
		helpSystemInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				SystemInformationDialog.open(gui.getShell());
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
			helpAboutItem.setText(Messages.get(LBL_MENU_ITEM_ABOUT));
			helpAboutItem.addSelectionListener(new AboutListener(gui));
		}

		final HelpMenuListener helpMenuListener = new HelpMenuListener(helpSystemInfoItem);
		helpMenu.addMenuListener(helpMenuListener);
		helpMenuHeader.addArmListener(helpMenuListener);

		gui.getShell().setMenuBar(bar);
	}

	public void updateTexts() {
		fileMenuHeader.setText(Messages.get(LBL_MENU_HEADER_FILE));
		fileOpenMenuItem.setText(Messages.get(LBL_MENU_ITEM_OPEN) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_OPEN));
		fileSaveMenuItem.setText(Messages.get(LBL_MENU_ITEM_SAVE) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		fileSaveAsMenuItem.setText(Messages.get(LBL_MENU_ITEM_SAVEAS));
		if (fileExitMenuItem != null && !fileExitMenuItem.isDisposed()) {
			fileExitMenuItem.setText(Messages.get(LBL_MENU_ITEM_EXIT));
		}
		editMenuHeader.setText(Messages.get(LBL_MENU_HEADER_EDIT));
		editCutMenuItem.setText(Messages.get(LBL_MENU_ITEM_CUT) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_CUT));
		editCopyMenuItem.setText(Messages.get(LBL_MENU_ITEM_COPY) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editPasteMenuItem.setText(Messages.get(LBL_MENU_ITEM_PASTE) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_PASTE));
		editResetSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_RESET));
		editResetSingleMenuItem.setText(Messages.get(LBL_MENU_ITEM_RESET_SINGLE));
		editResetAllMenuItem.setText(Messages.get(LBL_MENU_ITEM_RESET_ALL));
		viewMenuHeader.setText(Messages.get(LBL_MENU_HEADER_VIEW));
		viewRadixSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_RADIX));
		for (final Entry<NumeralSystem, MenuItem> entry : viewRadixMenuItems.entrySet()) {
			entry.getValue().setText(Messages.get(LBL_MENU_ITEM_RADIX + '.' + +entry.getKey().getRadix()));
		}
		editTorqueSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_TORQUE_CURVE));
		for (final Entry<BikeType, MenuItem> entry : editTorqueMenuItems.entrySet()) {
			entry.getValue().setText(Messages.get(LBL_MENU_ITEM_TORQUE_CURVE_BIKE, entry.getKey().getDisplacement()));
		}
		viewLanguageSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_LANGUAGE));
		for (final Entry<Language, MenuItem> entry : viewLanguageMenuItems.entrySet()) {
			entry.getValue().setText(entry.getKey().getLocale().getDisplayLanguage(entry.getKey().getLocale()));
		}
		helpMenuHeader.setText(Messages.get(Util.isWindows() ? LBL_MENU_HEADER_HELP_WINDOWS : LBL_MENU_HEADER_HELP));
		helpSystemInfoItem.setText(Messages.get(LBL_MENU_ITEM_SYSTEM_INFO));
		if (helpAboutItem != null && !helpAboutItem.isDisposed()) {
			helpAboutItem.setText(Messages.get(LBL_MENU_ITEM_ABOUT));
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
