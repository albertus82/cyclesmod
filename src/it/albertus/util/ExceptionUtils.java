package it.albertus.util;

import org.apache.commons.lang.StringUtils;

public class ExceptionUtils {

	public static String getGUIMessage(Throwable throwable) {
		String message = "";
		if (throwable != null) {
			if (StringUtils.isNotBlank(throwable.getLocalizedMessage())) {
				message = throwable.getLocalizedMessage();
			}
			else if (StringUtils.isNotBlank(throwable.getMessage())) {
				message = throwable.getMessage();
			}
			else {
				message = throwable.getClass().getSimpleName();
			}

			if (StringUtils.isNotBlank(message) && !message.endsWith(".")) {
				message += ".";
			}
		}
		return message;
	}

	public static String getLogMessage(Throwable throwable) {
		String message = "";
		if (throwable != null) {
			if (StringUtils.isNotBlank(throwable.getLocalizedMessage()) || StringUtils.isNotBlank(throwable.getMessage())) {
				message = throwable.getClass().getSimpleName() + ": " + (StringUtils.isNotBlank(throwable.getLocalizedMessage()) ? throwable.getLocalizedMessage() : throwable.getMessage());
			}

			if (StringUtils.isNotBlank(message) && !message.endsWith(".")) {
				message += ".";
			}
		}
		return message;
	}
}