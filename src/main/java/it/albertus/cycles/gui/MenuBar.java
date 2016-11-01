package it.albertus.cycles.gui;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cycles.engine.NumeralSystem;
import it.albertus.cycles.gui.listener.AboutListener;
import it.albertus.cycles.gui.listener.CloseListener;
import it.albertus.cycles.gui.listener.CopySelectionListener;
import it.albertus.cycles.gui.listener.CutSelectionListener;
import it.albertus.cycles.gui.listener.EditMenuBarArmListener;
import it.albertus.cycles.gui.listener.LanguageSelectionListener;
import it.albertus.cycles.gui.listener.OpenSelectionListener;
import it.albertus.cycles.gui.listener.PasteSelectionListener;
import it.albertus.cycles.gui.listener.RadixSelectionListener;
import it.albertus.cycles.gui.listener.ResetAllSelectionListener;
import it.albertus.cycles.gui.listener.ResetSingleSelectionListener;
import it.albertus.cycles.gui.listener.SaveSelectionListener;
import it.albertus.cycles.resources.Messages;
import it.albertus.cycles.resources.Messages.Language;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.cocoa.CocoaUIEnhancer;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
public class MenuBar {

	private final Menu bar;

	private final Menu fileMenu;
	private final MenuItem fileMenuHeader;
	private final MenuItem fileOpenMenuItem;
	private final MenuItem fileSaveMenuItem;
	private MenuItem fileExitMenuItem;

	private final Menu editMenu;
	private final MenuItem editMenuHeader;
	private final MenuItem editCutMenuItem;
	private final MenuItem editCopyMenuItem;
	private final MenuItem editPasteMenuItem;
	private final Menu editResetSubMenu;
	private final MenuItem editResetSubMenuItem;
	private final MenuItem editResetSingleMenuItem;
	private final MenuItem editResetAllMenuItem;

	private final Menu viewMenu;
	private final MenuItem viewMenuHeader;
	private final Menu viewRadixSubMenu;
	private final MenuItem viewRadixSubMenuItem;
	private final Map<NumeralSystem, MenuItem> viewRadixMenuItems = new EnumMap<NumeralSystem, MenuItem>(NumeralSystem.class);
	private final Menu viewLanguageSubMenu;
	private final MenuItem viewLanguageSubMenuItem;
	private final Map<Language, MenuItem> viewLanguageMenuItems = new EnumMap<Language, MenuItem>(Language.class);

	private Menu helpMenu;
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
			catch (final Throwable t) {
				t.printStackTrace();
			}
		}

		bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		// File
		fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setText(Messages.get("lbl.menu.header.file"));
		fileMenuHeader.setMenu(fileMenu);

		fileOpenMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenMenuItem.setText(Messages.get("lbl.menu.item.open") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_OPEN));
		fileOpenMenuItem.addSelectionListener(new OpenSelectionListener(gui));
		fileOpenMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_OPEN);

		fileSaveMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveMenuItem.setText(Messages.get("lbl.menu.item.saveas") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		fileSaveMenuItem.addSelectionListener(new SaveSelectionListener(gui));
		fileSaveMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SAVE);

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitMenuItem.setText(Messages.get("lbl.menu.item.exit"));
			fileExitMenuItem.addSelectionListener(closeListener);
		}

		// Edit
		editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editMenuHeader = new MenuItem(bar, SWT.CASCADE);
		editMenuHeader.setText(Messages.get("lbl.menu.header.edit"));
		editMenuHeader.setMenu(editMenu);
		editMenuHeader.addArmListener(new EditMenuBarArmListener(gui));

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

		editResetSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editResetSubMenuItem.setText(Messages.get("lbl.menu.item.reset"));

		editResetSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editResetSubMenuItem.setMenu(editResetSubMenu);

		editResetSingleMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetSingleMenuItem.setText(Messages.get("lbl.menu.item.reset.single"));
		editResetSingleMenuItem.addSelectionListener(new ResetSingleSelectionListener(gui));

		editResetAllMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetAllMenuItem.setText(Messages.get("lbl.menu.item.reset.all"));
		editResetAllMenuItem.addSelectionListener(new ResetAllSelectionListener(gui));

		// View
		viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewMenuHeader = new MenuItem(bar, SWT.CASCADE);
		viewMenuHeader.setText(Messages.get("lbl.menu.header.view"));
		viewMenuHeader.setMenu(viewMenu);

		viewRadixSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		viewRadixSubMenuItem.setText(Messages.get("lbl.menu.item.radix"));

		viewRadixSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
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

		viewLanguageSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
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
			helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
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
		fileSaveMenuItem.setText(Messages.get("lbl.menu.item.saveas") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
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
		for (final NumeralSystem numeralSystem : viewRadixMenuItems.keySet()) {
			viewRadixMenuItems.get(numeralSystem).setText(Messages.get("lbl.menu.item.radix." + numeralSystem.getRadix()));
		}
		viewLanguageSubMenuItem.setText(Messages.get("lbl.menu.item.language"));
		for (final Language language : viewLanguageMenuItems.keySet()) {
			viewLanguageMenuItems.get(language).setText(language.getLocale().getDisplayLanguage(language.getLocale()));
		}
		if (helpMenuHeader != null && !helpMenuHeader.isDisposed()) {
			helpMenuHeader.setText(Messages.get("lbl.menu.header.help"));
		}
		if (helpAboutMenuItem != null && !helpAboutMenuItem.isDisposed()) {
			helpAboutMenuItem.setText(Messages.get("lbl.menu.item.about"));
		}
	}

	public Menu getBar() {
		return bar;
	}

	public Menu getFileMenu() {
		return fileMenu;
	}

	public MenuItem getFileMenuHeader() {
		return fileMenuHeader;
	}

	public MenuItem getFileOpenMenuItem() {
		return fileOpenMenuItem;
	}

	public MenuItem getFileSaveMenuItem() {
		return fileSaveMenuItem;
	}

	public MenuItem getFileExitMenuItem() {
		return fileExitMenuItem;
	}

	public Menu getEditMenu() {
		return editMenu;
	}

	public MenuItem getEditMenuHeader() {
		return editMenuHeader;
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

	public Menu getEditResetSubMenu() {
		return editResetSubMenu;
	}

	public MenuItem getEditResetSubMenuItem() {
		return editResetSubMenuItem;
	}

	public MenuItem getEditResetSingleMenuItem() {
		return editResetSingleMenuItem;
	}

	public MenuItem getEditResetAllMenuItem() {
		return editResetAllMenuItem;
	}

	public Menu getViewMenu() {
		return viewMenu;
	}

	public MenuItem getViewMenuHeader() {
		return viewMenuHeader;
	}

	public Menu getViewRadixSubMenu() {
		return viewRadixSubMenu;
	}

	public MenuItem getViewRadixSubMenuItem() {
		return viewRadixSubMenuItem;
	}

	public Map<NumeralSystem, MenuItem> getViewRadixMenuItems() {
		return Collections.unmodifiableMap(viewRadixMenuItems);
	}

	public Menu getViewLanguageSubMenu() {
		return viewLanguageSubMenu;
	}

	public MenuItem getViewLanguageSubMenuItem() {
		return viewLanguageSubMenuItem;
	}

	public Map<Language, MenuItem> getViewLanguageMenuItems() {
		return Collections.unmodifiableMap(viewLanguageMenuItems);
	}

	public Menu getHelpMenu() {
		return helpMenu;
	}

	public MenuItem getHelpMenuHeader() {
		return helpMenuHeader;
	}

	public MenuItem getHelpAboutMenuItem() {
		return helpAboutMenuItem;
	}

}
