package it.albertus.cyclesmod.gui.model;

import lombok.Data;

@Data
public class GenericTextData {

	private final String key;
	private final int defaultValue;
	private final int maxValue;

	public int getSize() {
		return Integer.toString(maxValue).length();
	}

}
