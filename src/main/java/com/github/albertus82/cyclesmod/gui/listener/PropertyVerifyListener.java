package com.github.albertus82.cyclesmod.gui.listener;

import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

import com.github.albertus82.cyclesmod.gui.CyclesModGui;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@RequiredArgsConstructor
public class PropertyVerifyListener implements VerifyListener {

	@NonNull
	protected final CyclesModGui gui;

	@Getter
	private boolean enabled = true;

	@Override
	public void verifyText(@NonNull final VerifyEvent ve) {
		if (enabled) {
			ve.text = ve.text.trim();
			if (!ve.text.isEmpty() && !gui.isNumeric(ve.text)) {
				ve.doit = false;
			}
			else {
				if (gui.getNumeralSystem().getRadix() > 10 && gui.getNumeralSystem().getRadix() <= 36) {
					ve.text = ve.text.toUpperCase(Locale.ROOT); // Hex letters case.
				}
			}
		}
	}

}
