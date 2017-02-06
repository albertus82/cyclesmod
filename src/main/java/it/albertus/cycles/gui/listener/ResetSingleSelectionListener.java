package it.albertus.cycles.gui.listener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.logging.LoggerFactory;

public class ResetSingleSelectionListener extends SelectionAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ResetSingleSelectionListener.class);

	private final CyclesModGui gui;

	public ResetSingleSelectionListener(CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final BikeType type = BikeType.values()[gui.getTabs().getTabFolder().getSelectionIndex()];
		MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setText(Messages.get("msg.warning"));
		messageBox.setMessage(Messages.get("msg.reset.overwrite.single", type.getDisplacement()));
		int choose = messageBox.open();
		if (choose == SWT.YES) {
			try {
				reset(type);
			}
			catch (final Exception e) {
				logger.log(Level.WARNING, e.toString(), e);
				messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
				messageBox.setText(Messages.get("msg.warning"));
				messageBox.setMessage(Messages.get("err.reset", ExceptionUtils.getUIMessage(e)));
				messageBox.open();
			}
		}
	}

	private void reset(final BikeType type) throws IOException {
		gui.updateModelValues(true);
		gui.getBikesInf().reset(type);
		gui.getTabs().updateFormValues();
	}

}
