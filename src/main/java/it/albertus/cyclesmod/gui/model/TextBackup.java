package it.albertus.cyclesmod.gui.model;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Text;

import lombok.NonNull;
import lombok.Value;

@Value
public class TextBackup {

	String text;
	int textLimit;
	Font font;
	boolean visible;

	public TextBackup(@NonNull final Text source) {
		this.text = source.getText();
		this.textLimit = source.getTextLimit();
		this.font = source.getFont();
		this.visible = source.getVisible();
	}

}
