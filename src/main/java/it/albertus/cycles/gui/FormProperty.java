package it.albertus.cycles.gui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FormProperty {

	public enum DataKey {
		DEFAULT("default"),
		KEY("key"),
		GRAPH("graph"),
		INDEX("index");

		private final String key;

		private DataKey(final String key) {
			this.key = key;
		}

		@Override
		public String toString() {
			return key;
		}
	}

	private final Label label;
	private final Text text;

	public FormProperty(final Label label, final Text text) {
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
