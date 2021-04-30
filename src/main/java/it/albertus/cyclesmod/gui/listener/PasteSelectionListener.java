package it.albertus.cyclesmod.gui.listener;

import java.util.function.Supplier;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.cyclesmod.gui.Tabs;
import it.albertus.cyclesmod.gui.model.FormProperty;
import it.albertus.jface.SwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PasteSelectionListener extends SelectionAdapter {

	@NonNull private final Supplier<Tabs> tabsSupplier;

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (SwtUtils.checkClipboard(TextTransfer.getInstance())) {
			for (final FormProperty fp : tabsSupplier.get().getFormProperties().values()) {
				if (fp != null && fp.getText() != null && fp.getText().isFocusControl()) {
					fp.getText().paste();
					break;
				}
			}
		}
	}

}
