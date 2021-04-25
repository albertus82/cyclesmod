package it.albertus.cyclesmod.gui;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class FormProperty {

	@Getter
	@RequiredArgsConstructor
	public enum TextDataKey {
		DEFAULT(Integer.class),
		GRAPH(IPowerGraph.class),
		INDEX(Integer.class),
		KEY(String.class),
		MAX(Integer.class),
		SIZE(Integer.class);

		private final Class<?> type;
	}

	@Value
	private class TextBackup {
		String text;
		int textLimit;
		Font font;
		boolean visible;

		public TextBackup(final Text source) {
			this.text = source.getText();
			this.textLimit = source.getTextLimit();
			this.font = source.getFont();
			this.visible = source.getVisible();
		}
	}

	@Getter private final Text text;

	private TextBackup textBackup = null;

	public String getValue() {
		if (text != null) {
			return text.getText().trim();
		}
		else {
			return "";
		}
	}

	public void backup() {
		backupText();
	}

	public void restore() {
		restoreText();
	}

	private void backupText() {
		textBackup = new TextBackup(this.text);
	}

	private void restoreText() {
		if (textBackup == null) {
			throw new IllegalStateException("Call backup method first!");
		}
		if (!this.text.getText().equals(textBackup.text)) {
			this.text.setText(textBackup.text);
		}
		if (this.text.getTextLimit() != textBackup.textLimit) {
			this.text.setTextLimit(textBackup.textLimit);
		}
		if (!this.text.getFont().equals(textBackup.font)) {
			this.text.setFont(textBackup.font);
		}
		if (this.text.getVisible() != textBackup.visible) {
			this.text.setVisible(textBackup.visible);
		}
		textBackup = null;
	}

	@Override
	public String toString() {
		return "FormProperty [text=" + text + "]";
	}

}
