package it.albertus.cycles.gui;

import org.eclipse.swt.SWT;

public class GuiUtils {

	public static final char KEY_SELECT_ALL = 'a';
	public static final char KEY_COPY = 'c';
	public static final char KEY_OPEN = 'o';
	public static final char KEY_SAVE = 's';

	public static String getMod1KeyLabel() {
		return SWT.MOD1 == SWT.COMMAND ? "    \u2318" : "\tCtrl+";
	}

}
