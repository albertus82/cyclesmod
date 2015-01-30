package it.albertus.cycles.gui;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class Property {

	private Label label;
	private Text text;

	public Property(Label label, Text text) {
		this.label = label;
		this.text = text;
	}

	public Label getLabel() {
		return label;
	}

	public Text getText() {
		return text;
	}

}