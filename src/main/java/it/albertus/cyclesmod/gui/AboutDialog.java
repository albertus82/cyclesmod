package it.albertus.cyclesmod.gui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import it.albertus.cyclesmod.gui.listener.LinkSelectionListener;
import it.albertus.cyclesmod.resources.Messages;

public class AboutDialog extends Dialog {

	private String message = "";
	private String applicationUrl = "";
	private String iconUrl = "";

	public AboutDialog(final Shell parent) {
		this(parent, SWT.SHEET); // SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL
	}

	public AboutDialog(final Shell parent, final int style) {
		super(parent, style);
	}

	public void open() {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		shell.open();
	}

	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(2, false));

		final Label icon = new Label(shell, SWT.NONE);
		icon.setImage(Images.getAppIconArray()[2]);
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false, 0, 3);
		icon.setLayoutData(gridData);

		final Label info = new Label(shell, SWT.NONE);
		info.setText(this.message);
		gridData = new GridData(SWT.LEAD, SWT.CENTER, false, true);
		info.setLayoutData(gridData);

		final Link linkProject = new Link(shell, SWT.NONE);
		linkProject.setText("<a href=\"" + getApplicationUrl() + "\">" + getApplicationUrl() + "</a>");
		gridData = new GridData(SWT.LEAD, SWT.CENTER, false, true);
		linkProject.setLayoutData(gridData);
		linkProject.addSelectionListener(new LinkSelectionListener());

		final Link linkIcon = new Link(shell, SWT.NONE);
		String url = getIconUrl().startsWith("http") ? getIconUrl() : "http://" + getIconUrl();
		linkIcon.setText(Messages.get("msg.info.icon") + " <a href=\"" + url + "\">" + getIconUrl() + "</a>");
		gridData = new GridData(SWT.LEAD, SWT.CENTER, false, true);
		linkIcon.setLayoutData(gridData);
		linkIcon.addSelectionListener(new LinkSelectionListener());

		final Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText(Messages.get("lbl.button.ok"));
		final GC gc = new GC(okButton);
		gc.setFont(okButton.getFont());
		final FontMetrics fontMetrics = gc.getFontMetrics();
		final int buttonWidth = org.eclipse.jface.dialogs.Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
		gc.dispose();
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).minSize(buttonWidth, SWT.DEFAULT).applyTo(okButton);
		okButton.setFocus();
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});
		shell.setDefaultButton(okButton);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getApplicationUrl() {
		return applicationUrl;
	}

	public void setApplicationUrl(String applicationUrl) {
		this.applicationUrl = applicationUrl;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

}
