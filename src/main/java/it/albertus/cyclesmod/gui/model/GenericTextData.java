package it.albertus.cyclesmod.gui.model;

import java.util.Collections;
import java.util.Map;

import it.albertus.cyclesmod.common.model.Game;
import lombok.Data;

@Data
public class GenericTextData {

	private final Map<Game, String> keyMap;
	private final Map<Game, Integer> defaultValueMap;
	private final int maxValue;

	public GenericTextData(final Map<Game, String> keyMap, final Map<Game, Integer> defaultValueMap, final int maxValue) {
		this.keyMap = Collections.unmodifiableMap(keyMap);
		this.defaultValueMap = Collections.unmodifiableMap(defaultValueMap);
		this.maxValue = maxValue;
	}

	public int getSize() {
		return Integer.toString(maxValue).length();
	}

}
