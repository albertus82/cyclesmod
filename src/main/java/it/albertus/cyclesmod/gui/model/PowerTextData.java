package it.albertus.cyclesmod.gui.model;

import it.albertus.cyclesmod.gui.powergraph.IPowerGraph;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class PowerTextData extends GenericTextData {

	public PowerTextData(final int defaultValue, final String key, final int size, final int maxValue, final int index, final IPowerGraph powerGraph) {
		super(defaultValue, key, size, maxValue);
		this.index = index;
		this.powerGraph = powerGraph;
	}

	int index;
	IPowerGraph powerGraph;

}
