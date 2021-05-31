package it.albertus.cyclesmod.gui.model;

import java.util.Map;

import it.albertus.cyclesmod.common.model.Game;
import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class PowerTextData extends GenericTextData {

	public PowerTextData(final Map<Game, String> keyMap, final Map<Game, Integer> defaultValueMap, final int maxValue, final int index, final IPowerGraph powerGraph) {
		super(keyMap, defaultValueMap, maxValue);
		this.index = index;
		this.powerGraph = powerGraph;
	}

	int index;
	IPowerGraph powerGraph;

}
