package io.github.albertus82.cyclesmod.gui.listener;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

import io.github.albertus82.cyclesmod.gui.CyclesModGui;
import io.github.albertus82.cyclesmod.gui.TextFormatter;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@RequiredArgsConstructor
public class PropertyFocusListener implements FocusListener {

	@NonNull
	protected final CyclesModGui gui;

	@Getter
	private boolean enabled = true;

	private String before;

	@Override
	public void focusLost(@NonNull final FocusEvent fe) {
		if (enabled && fe.widget instanceof Text) {
			final Text text = (Text) fe.widget;
			final TextFormatter textFormatter = gui.getTabs().getTextFormatter();
			textFormatter.clean(text);
			textFormatter.updateFontStyle(text);
			if (before != null && !text.getText().equals(before)) {
				gui.setCurrentFileModificationStatus(true);
			}
		}
	}

	@Override
	public void focusGained(@NonNull final FocusEvent fe) {
		if (enabled && fe.widget instanceof Text) {
			final Text text = (Text) fe.widget;
			before = text.getText();
			text.selectAll();
		}
	}

	public void reset() {
		before = null;
	}

}
