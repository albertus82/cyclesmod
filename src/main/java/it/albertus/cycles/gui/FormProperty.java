package it.albertus.cycles.gui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FormProperty {

	public static final String KEY_DEFAULT = "default";
	public static final String KEY_KEY = "key";
	public static final String KEY_GRAPH = "graph";
	public static final String KEY_INDEX = "index";

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
		if (text != null) {
			return StringUtils.trimToEmpty(text.getText());
		}
		else {
			return "";
		}
	}

	@Override
	public String toString() {
		return "FormProperty [label=" + label + ", text=" + text + "]";
	}

}
