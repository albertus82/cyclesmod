package it.albertus.cycles.gui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FormProperty {

	public enum LabelDataKey {
		KEY(String.class),
		ARGUMENT(String.class);

		private final Class<?> type;

		private LabelDataKey(final Class<?> type) {
			this.type = type;
		}

		public Class<?> getType() {
			return type;
		}
	}

	public enum TextDataKey {
		DEFAULT(Integer.class),
		GRAPH(TorqueGraph.class),
		INDEX(Integer.class),
		KEY(String.class),
		MAX(Integer.class),
		SIZE(Integer.class);

		private final Class<?> type;

		private TextDataKey(final Class<?> type) {
			this.type = type;
		}

		public Class<?> getType() {
			return type;
		}
	}

	private class TextBackup {
		private final String text;
		private final int textLimit;
		private final Font font;
		private final boolean visible;

		public TextBackup(final Text source) {
			this.text = source.getText();
			this.textLimit = source.getTextLimit();
			this.font = source.getFont();
			this.visible = source.getVisible();
		}
	}

	private final Label label;
	private final Text text;
	private TextBackup textBackup = null;

	FormProperty(final Label label, final Text text) {
		this.label = label;
		this.text = text;
	}

	public Label getLabel() {
		return label;
	}

	public Text getText() {
		return text;
	}

	public String getValue() {
		if (text != null) {
			return StringUtils.trimToEmpty(text.getText());
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
		return "FormProperty [label=" + label + ", text=" + text + "]";
	}

}
