package it.albertus.cycles.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

public class Resources {

	private static final ResourceBundle resources = ResourceBundle.getBundle(Resources.class.getName().toLowerCase());

	public static String get(final String key, final Object... params) {
		List<String> stringParams = new ArrayList<String>(params.length);
		for (Object param : params) {
			stringParams.add(param != null ? param.toString() : "");
		}
		return StringUtils.trimToEmpty(MessageFormat.format(resources.getString(key), stringParams.toArray()));
	}

}
