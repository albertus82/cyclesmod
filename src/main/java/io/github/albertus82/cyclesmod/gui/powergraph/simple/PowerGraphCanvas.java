package io.github.albertus82.cyclesmod.gui.powergraph.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import io.github.albertus82.cyclesmod.common.model.Vehicle;
import io.github.albertus82.cyclesmod.gui.Mode;
import io.github.albertus82.cyclesmod.gui.powergraph.PowerGraphProvider;
import io.github.albertus82.jface.Multilanguage;
import lombok.Getter;
import lombok.NonNull;

public class PowerGraphCanvas extends Canvas implements PowerGraphProvider, Multilanguage { // NOSONAR This class has 6 parents which is greater than 5 authorized. Inheritance tree of classes should not be too deep (java:S110)

	@Getter
	private final SimplePowerGraph powerGraph;

	private final Collection<Multilanguage> multilanguages = new ArrayList<>();

	public PowerGraphCanvas(@NonNull final Composite parent, @NonNull final Vehicle vehicle, @NonNull final Supplier<Mode> modeSupplier) {
		super(parent, SWT.NONE);

		final LightweightSystem lws = new LightweightSystem(this);
		powerGraph = new SimplePowerGraph(vehicle, modeSupplier);
		multilanguages.add(powerGraph);
		lws.setContents(powerGraph.getXyGraph());

		setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		multilanguages.add(new SimplePowerGraphContextMenu(this, powerGraph));
	}

	@Override
	public void updateLanguage() {
		for (final Multilanguage multilanguage : multilanguages) {
			multilanguage.updateLanguage();
		}
	}

	public void updateModeSpecificWidgets() {
		powerGraph.updateModeSpecificWidgets();
	}

}
