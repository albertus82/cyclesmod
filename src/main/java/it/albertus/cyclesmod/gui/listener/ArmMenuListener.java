package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.TypedEvent;

@FunctionalInterface
public interface ArmMenuListener extends ArmListener, MenuListener {

	/**
	 * Sent when a menu is shown, is armed, or 'about to be selected'.
	 * 
	 * @param e an event containing information about the operation
	 */
	void menuArmed(TypedEvent e);

	@Override
	default void menuShown(final MenuEvent e) {
		menuArmed(e);
	}

	@Override
	default void widgetArmed(final ArmEvent e) {
		menuArmed(e);
	}

	@Override
	default void menuHidden(final MenuEvent e) {/* Ignore */}

}
