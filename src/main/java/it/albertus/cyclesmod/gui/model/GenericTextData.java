package it.albertus.cyclesmod.gui.model;

import java.util.Collections;
import java.util.Map;

import it.albertus.cyclesmod.gui.Mode;
import lombok.Data;

@Data
public class GenericTextData {

	private final Map<Mode, String> keyMap;
	private final Map<Mode, Integer> defaultValueMap;
	private final int maxValue;

	public GenericTextData(final Map<Mode, String> keyMap, final Map<Mode, Integer> defaultValueMap, final int maxValue) {
		this.keyMap = Collections.unmodifiableMap(keyMap);
		this.defaultValueMap = Collections.unmodifiableMap(defaultValueMap);
		this.maxValue = maxValue;
	}

	public int getSize() {
		return Integer.toString(maxValue).length();
	}

}
