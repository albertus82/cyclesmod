package it.albertus.cyclesmod.gui.model;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class FormProperty {

	@Getter @ToString.Include private final Label label;
	@Getter @ToString.Include private final Text text;

	private TextBackup textBackup;

	public String getValue() {
		return text != null ? text.getText().trim() : "";
	}

	public void backup() {
		textBackup = new TextBackup(this.text);
	}

	public void restore() {
		if (textBackup == null) {
			throw new IllegalStateException("Call backup method first");
		}
		if (!this.text.getText().equals(textBackup.getText())) {
			this.text.setText(textBackup.getText());
		}
		if (this.text.getTextLimit() != textBackup.getTextLimit()) {
			this.text.setTextLimit(textBackup.getTextLimit());
		}
		if (!this.text.getFont().equals(textBackup.getFont())) {
			this.text.setFont(textBackup.getFont());
		}
		if (this.text.getVisible() != textBackup.isVisible()) {
			this.text.setVisible(textBackup.isVisible());
		}
		textBackup = null;
	}

}
