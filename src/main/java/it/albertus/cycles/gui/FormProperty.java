package it.albertus.cycles.gui;

import org.apache.commons.lang.StringUtils;
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
		KEY(String.class),
		GRAPH(TorqueGraph.class),
		INDEX(Integer.class);

		private final Class<?> type;

		private TextDataKey(final Class<?> type) {
			this.type = type;
		}

		public Class<?> getType() {
			return type;
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
