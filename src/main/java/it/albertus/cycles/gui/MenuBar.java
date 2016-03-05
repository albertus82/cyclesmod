package it.albertus.cycles.gui;

import it.albertus.cycles.gui.listener.AboutSelectionListener;
import it.albertus.cycles.gui.listener.CloseListener;
import it.albertus.cycles.gui.listener.CopySelectionListener;
import it.albertus.cycles.gui.listener.EditArmListener;
import it.albertus.cycles.gui.listener.OpenSelectionListener;
import it.albertus.cycles.gui.listener.PasteSelectionListener;
import it.albertus.cycles.gui.listener.ResetAllSelectionListener;
import it.albertus.cycles.gui.listener.ResetSingleSelectionListener;
import it.albertus.cycles.gui.listener.SaveSelectionListener;
import it.albertus.cycles.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class MenuBar {

	private final Menu menuBar;

	private final Menu fileMenu;
	private final MenuItem fileMenuHeader;
	private final MenuItem fileOpenMenuItem;
	private final MenuItem fileSaveMenuItem;
	private final MenuItem fileExitMenuItem;

	private final Menu editMenu;
	private final MenuItem editMenuHeader;
	private final MenuItem editCopyMenuItem;
	private final MenuItem editPasteMenuItem;
	private final Menu editResetSubMenu;
	private final MenuItem editResetSubMenuItem;
	private final MenuItem editResetSingleMenuItem;
	private final MenuItem editResetAllMenuItem;

	private final Menu helpMenu;
	private final MenuItem helpMenuHeader;
	private final MenuItem helpAboutMenuItem;

	public MenuBar(final CyclesModGui gui) {
		menuBar = new Menu(gui.getShell(), SWT.BAR); // Barra

		// File
		fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText(Resources.get("lbl.menu.header.file"));
		fileMenuHeader.setMenu(fileMenu);

		fileOpenMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenMenuItem.setText(Resources.get("lbl.menu.item.open") + GuiUtils.getMod1KeyLabel() + Character.toUpperCase(GuiUtils.KEY_OPEN));
		fileOpenMenuItem.addSelectionListener(new OpenSelectionListener(gui));
		fileOpenMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_OPEN);

		fileSaveMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveMenuItem.setText(Resources.get("lbl.menu.item.saveas") + GuiUtils.getMod1KeyLabel() + Character.toUpperCase(GuiUtils.KEY_SAVE));
		fileSaveMenuItem.addSelectionListener(new SaveSelectionListener(gui));
		fileSaveMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_SAVE);

		new MenuItem(fileMenu, SWT.SEPARATOR);

		fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitMenuItem.setText(Resources.get("lbl.menu.item.exit"));
		fileExitMenuItem.addSelectionListener(new CloseListener(gui));

		// Edit
		editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		editMenuHeader.setText(Resources.get("lbl.menu.header.edit"));
		editMenuHeader.setMenu(editMenu);
		editMenuHeader.addArmListener(new EditArmListener(gui));

		editCopyMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCopyMenuItem.setText(Resources.get("lbl.menu.item.copy") + GuiUtils.getMod1KeyLabel() + Character.toUpperCase(GuiUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopySelectionListener(gui));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_COPY);

		editPasteMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editPasteMenuItem.setText(Resources.get("lbl.menu.item.paste") + GuiUtils.getMod1KeyLabel() + Character.toUpperCase(GuiUtils.KEY_PASTE));
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

		// Help
		helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText(Resources.get("lbl.menu.header.help"));
		helpMenuHeader.setMenu(helpMenu);

		helpAboutMenuItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutMenuItem.setText(Resources.get("lbl.menu.item.about"));
		helpAboutMenuItem.addSelectionListener(new AboutSelectionListener(gui));

		gui.getShell().setMenuBar(menuBar);
	}

	public Menu getMenuBar() {
		return menuBar;
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
