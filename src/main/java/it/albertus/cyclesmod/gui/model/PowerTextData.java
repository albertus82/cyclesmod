package it.albertus.cyclesmod.gui.model;

import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class PowerTextData extends GenericTextData {

	public PowerTextData(final String key, final int defaultValue, final int maxValue, final int index, final IPowerGraph powerGraph) {
		super(key, defaultValue, maxValue);
		this.index = index;
		this.powerGraph = powerGraph;
	}

	int index;
	IPowerGraph powerGraph;

}
