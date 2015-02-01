package it.albertus.cycles.gui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FormProperty {

	private final Label label;
	private final Text text;

	public FormProperty(Label label, Text text) {
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
		if ( text != null ) {
			return StringUtils.trimToEmpty( text.getText() );
		}
		else {
			return "";
		}
	}

}