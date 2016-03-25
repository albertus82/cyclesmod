package it.albertus.cycles.gui;

import it.albertus.cycles.engine.NumeralSystem;
import it.albertus.cycles.gui.listener.AboutSelectionListener;
import it.albertus.cycles.gui.listener.CloseListener;
import it.albertus.cycles.gui.listener.CopySelectionListener;
import it.albertus.cycles.gui.listener.CutSelectionListener;
import it.albertus.cycles.gui.listener.EditMenuBarArmListener;
import it.albertus.cycles.gui.listener.OpenSelectionListener;
import it.albertus.cycles.gui.listener.PasteSelectionListener;
import it.albertus.cycles.gui.listener.RadixSelectionListener;
import it.albertus.cycles.gui.listener.ResetAllSelectionListener;
import it.albertus.cycles.gui.listener.ResetSingleSelectionListener;
import it.albertus.cycles.gui.listener.SaveSelectionListener;
import it.albertus.cycles.resources.Resources;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

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
	private final MenuItem fileExitMenuItem;

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

	private final Menu helpMenu;
	private final MenuItem helpMenuHeader;
	private final MenuItem helpAboutMenuItem;

	public MenuBar(final CyclesModGui gui) {
		bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		// File
		fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setText(Resources.get("lbl.menu.header.file"));
		fileMenuHeader.setMenu(fileMenu);

		fileOpenMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenMenuItem.setText(Resources.get("lbl.menu.item.open") + GuiUtils.getMod1ShortcutLabel(GuiUtils.KEY_OPEN));
		fileOpenMenuItem.addSelectionListener(new OpenSelectionListener(gui));
		fileOpenMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_OPEN);

		fileSaveMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveMenuItem.setText(Resources.get("lbl.menu.item.saveas") + GuiUtils.getMod1ShortcutLabel(GuiUtils.KEY_SAVE));
		fileSaveMenuItem.addSelectionListener(new SaveSelectionListener(gui));
		fileSaveMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_SAVE);

		new MenuItem(fileMenu, SWT.SEPARATOR);

		fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitMenuItem.setText(Resources.get("lbl.menu.item.exit"));
		fileExitMenuItem.addSelectionListener(new CloseListener(gui));

		// Edit
		editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editMenuHeader = new MenuItem(bar, SWT.CASCADE);
		editMenuHeader.setText(Resources.get("lbl.menu.header.edit"));
		editMenuHeader.setMenu(editMenu);
		editMenuHeader.addArmListener(new EditMenuBarArmListener(gui));

		editCutMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCutMenuItem.setText(Resources.get("lbl.menu.item.cut") + GuiUtils.getMod1ShortcutLabel(GuiUtils.KEY_CUT));
		editCutMenuItem.addSelectionListener(new CutSelectionListener(gui));
		editCutMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_CUT);

		editCopyMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCopyMenuItem.setText(Resources.get("lbl.menu.item.copy") + GuiUtils.getMod1ShortcutLabel(GuiUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopySelectionListener(gui));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_COPY);

		editPasteMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editPasteMenuItem.setText(Resources.get("lbl.menu.item.paste") + GuiUtils.getMod1ShortcutLabel(GuiUtils.KEY_PASTE));
		editPasteMenuItem.addSelectionListener(new PasteSelectionListener(gui));
		editPasteMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_PASTE);

		new MenuItem(editMenu, SWT.SEPARATOR);

		editResetSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editResetSubMenuItem.setText(Resources.get("lbl.menu.item.reset"));

		editResetSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editResetSubMenuItem.setMenu(editResetSubMenu);

		editResetSingleMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetSingleMenuItem.setText(Resources.get("lbl.menu.item.reset.single"));
		editResetSingleMenuItem.addSelectionListener(new ResetSingleSelectionListener(gui));

		editResetAllMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetAllMenuItem.setText(Resources.get("lbl.menu.item.reset.all"));
		editResetAllMenuItem.addSelectionListener(new ResetAllSelectionListener(gui));

		// View
		viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewMenuHeader = new MenuItem(bar, SWT.CASCADE);
		viewMenuHeader.setText(Resources.get("lbl.menu.header.view"));
		viewMenuHeader.setMenu(viewMenu);

		viewRadixSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		viewRadixSubMenuItem.setText(Resources.get("lbl.menu.item.radix"));

		viewRadixSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewRadixSubMenuItem.setMenu(viewRadixSubMenu);

		final RadixSelectionListener radixSelectionListener = new RadixSelectionListener(gui);

		for (final NumeralSystem numeralSystem : NumeralSystem.values()) {
			final MenuItem radixMenuItem = new MenuItem(viewRadixSubMenu, SWT.RADIO);
			radixMenuItem.setText(Resources.get("lbl.menu.item.radix." + numeralSystem.getRadix()));
			radixMenuItem.setData(numeralSystem);
			radixMenuItem.addSelectionListener(radixSelectionListener);
			viewRadixMenuItems.put(numeralSystem, radixMenuItem);
		}

		viewRadixMenuItems.get(gui.getNumeralSystem()).setSelection(true); // Default

		// Help
		helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
		helpMenuHeader.setText(Resources.get("lbl.menu.header.help"));
		helpMenuHeader.setMenu(helpMenu);

		helpAboutMenuItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutMenuItem.setText(Resources.get("lbl.menu.item.about"));
		helpAboutMenuItem.addSelectionListener(new AboutSelectionListener(gui));

		gui.getShell().setMenuBar(bar);
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
