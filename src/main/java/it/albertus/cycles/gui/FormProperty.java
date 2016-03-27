package it.albertus.cycles.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class FormProperty {

	private static final int[] EventTypes = { SWT.Resize, SWT.Move, SWT.Dispose, SWT.DragDetect, SWT.FocusIn,
			SWT.FocusOut, SWT.Gesture, SWT.Help, SWT.KeyUp, SWT.KeyDown, SWT.MenuDetect, SWT.Modify, SWT.MouseDown,
			SWT.MouseUp, SWT.MouseDoubleClick, SWT.MouseMove, SWT.MouseEnter, SWT.MouseExit, SWT.MouseHover,
			SWT.MouseWheel, SWT.Paint, SWT.Selection, SWT.Touch, SWT.Traverse, SWT.Verify };

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
		INDEX(Integer.class),
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
	private final Map<Integer, List<Listener>> textListeners = new TreeMap<Integer, List<Listener>>();
	private TextBackup textBackup;
	private boolean textListenerStatus = true;

	public FormProperty(final Label label, final Text text) {
		this.label = label;
		this.text = text;
		for (final int eventType : EventTypes) {
			for (final Listener listener : text.getListeners(eventType)) {
				if (!this.textListeners.containsKey(eventType)) {
					this.textListeners.put(eventType, new ArrayList<Listener>());
				}
				this.textListeners.get(eventType).add(listener);
			}
		}
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

	public void backup(final boolean disableListeners) {
		backupText(disableListeners);
	}

	public void restore(final boolean enableListeners) {
		restoreText(enableListeners);
	}

	private void backupText(final boolean disableListeners) {
		if (disableListeners) {
			disableTextListeners();
		}
		textBackup = new TextBackup(this.text);
	}

	private void restoreText(final boolean enableListeners) {
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
		if (enableListeners) {
			enableTextListeners();
		}
	}

	public int enableTextListeners() {
		int count = 0;
		if (!textListenerStatus) {
			for (final int eventType : textListeners.keySet()) {
				for (final Listener listener : textListeners.get(eventType)) {
					text.addListener(eventType, listener);
					count++;
				}
			}
			textListenerStatus = true;
		}
		return count;
	}

	public int disableTextListeners() {
		int count = 0;
		if (textListenerStatus) {
			for (final int eventType : textListeners.keySet()) {
				for (final Listener listener : textListeners.get(eventType)) {
					text.removeListener(eventType, listener);
					count++;
				}
			}
			textListenerStatus = false;
		}
		return count;
	}

	@Override
	public String toString() {
		return "FormProperty [label=" + label + ", text=" + text + "]";
	}

}
