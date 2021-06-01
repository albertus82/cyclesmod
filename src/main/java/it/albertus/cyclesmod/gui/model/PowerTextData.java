package it.albertus.cyclesmod.gui.model;

import java.util.Map;

import it.albertus.cyclesmod.gui.Mode;
import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class PowerTextData extends GenericTextData {

	int index;
	IPowerGraph powerGraph;

	public PowerTextData(final Map<Mode, String> keyMap, final Map<Mode, Integer> defaultValueMap, final int maxValue, final int index, final IPowerGraph powerGraph) {
		super(keyMap, defaultValueMap, maxValue);
		this.index = index;
		this.powerGraph = powerGraph;
	}

}
