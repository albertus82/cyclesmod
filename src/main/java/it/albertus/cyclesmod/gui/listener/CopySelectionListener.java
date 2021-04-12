package it.albertus.cyclesmod.gui.listener;

import java.util.function.Supplier;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.cyclesmod.gui.FormProperty;
import it.albertus.cyclesmod.gui.Tabs;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CopySelectionListener extends SelectionAdapter {

	@NonNull private final Supplier<Tabs> tabsSupplier;

	@Override
	public void widgetSelected(final SelectionEvent se) {
		for (final FormProperty fp : tabsSupplier.get().getFormProperties().values()) {
			if (fp != null && fp.getText() != null && fp.getText().isFocusControl() && fp.getText().getSelectionText() != null && !fp.getText().getSelectionText().isEmpty()) {
				fp.getText().copy();
				break;
			}
		}
	}

}
