package io.github.albertus82.cyclesmod.gui.torquegraph.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import io.github.albertus82.cyclesmod.common.model.Vehicle;
import io.github.albertus82.cyclesmod.gui.Mode;
import io.github.albertus82.cyclesmod.gui.torquegraph.TorqueGraphProvider;
import io.github.albertus82.jface.Multilanguage;
import lombok.Getter;
import lombok.NonNull;

public class TorqueGraphCanvas extends Canvas implements TorqueGraphProvider, Multilanguage { // NOSONAR This class has 6 parents which is greater than 5 authorized. Inheritance tree of classes should not be too deep (java:S110)

	@Getter
	private final SimpleTorqueGraph graph;

	private final Collection<Multilanguage> multilanguages = new ArrayList<>();

	public TorqueGraphCanvas(@NonNull final Composite parent, @NonNull final Vehicle vehicle, @NonNull final Supplier<Mode> modeSupplier) {
		super(parent, SWT.NONE);

		final LightweightSystem lws = new LightweightSystem(this);
		graph = new SimpleTorqueGraph(vehicle, modeSupplier);
		multilanguages.add(graph);
		lws.setContents(graph.getXyGraph());

		setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		multilanguages.add(new SimpleTorqueGraphContextMenu(this, graph));
	}

	@Override
	public void updateLanguage() {
		for (final Multilanguage multilanguage : multilanguages) {
			multilanguage.updateLanguage();
		}
	}

	public void updateModeSpecificWidgets() {
		graph.updateModeSpecificWidgets();
	}

}
