package io.github.albertus82.cyclesmod.gui.model;

import java.util.Map;

import io.github.albertus82.cyclesmod.gui.Mode;
import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraph;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class TorqueTextData extends GenericTextData {

	int index;
	TorqueGraph powerGraph;

	public TorqueTextData(final Map<Mode, String> keyMap, final Map<Mode, Integer> defaultValueMap, final int maxValue, final int index, final TorqueGraph powerGraph) {
		super(keyMap, defaultValueMap, maxValue);
		this.index = index;
		this.powerGraph = powerGraph;
	}

}
