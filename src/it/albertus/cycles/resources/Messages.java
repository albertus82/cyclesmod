package it.albertus.cycles.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

public class Messages {

	private static final ResourceBundle messages = ResourceBundle.getBundle(Messages.class.getName().toLowerCase());

	public static String get(final String key, final Object... params) {
		List<String> stringParams = new ArrayList<String>(params.length);
		for (Object param : params) {
			stringParams.add(param != null ? param.toString() : "");
		}
		return StringUtils.trimToEmpty(MessageFormat.format(messages.getString(key), stringParams.toArray()));
	}

}