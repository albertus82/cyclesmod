package it.albertus.cycles.gui;

import it.albertus.cycles.gui.listener.LinkSelectionListener;
import it.albertus.cycles.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class AboutDialog extends Dialog {

	private String message = "";
	private String linkProject = "";
	private String linkIcon = "";

	public AboutDialog(Shell parent) {
		this(parent, SWT.SHEET); // SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL
	}

	public AboutDialog(Shell parent, int style) {
		super(parent, style);
	}

	public void open() {
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(2, false));

		final Label icon = new Label(shell, SWT.NONE);
		icon.setImage(Images.ICONS_TOOLS[5]);
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false, 0, 3);
		icon.setLayoutData(gridData);

		final Label info = new Label(shell, SWT.NONE);
		info.setText(this.message);
		gridData = new GridData(SWT.LEAD, SWT.CENTER, false, true);
		info.setLayoutData(gridData);

		final Link linkProject = new Link(shell, SWT.NONE);
		linkProject.setText("<a href=\"" + getLinkProject() + "\">" + getLinkProject() + "</a>");
		gridData = new GridData(SWT.LEAD, SWT.CENTER, false, true);
		linkProject.setLayoutData(gridData);
		linkProject.addSelectionListener(new LinkSelectionListener());

		final Link linkIcon = new Link(shell, SWT.NONE);
		String url = getLinkIcon().startsWith("http") ? getLinkIcon() : "http://" + getLinkIcon();
		linkIcon.setText(Resources.get("msg.info.icon") + " <a href=\"" + url + "\">" + getLinkIcon() + "</a>");
		gridData = new GridData(SWT.LEAD, SWT.CENTER, false, true);
		linkIcon.setLayoutData(gridData);
		linkIcon.addSelectionListener(new LinkSelectionListener());

		Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText(Resources.get("lbl.button.ok"));
		gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 0);
		gridData.minimumWidth = 64;
		okButton.setLayoutData(gridData);
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

	public String getLinkProject() {
		return linkProject;
	}

	public void setLinkProject(String linkProject) {
		this.linkProject = linkProject;
	}

	public String getLinkIcon() {
		return linkIcon;
	}

	public void setLinkIcon(String linkIcon) {
		this.linkIcon = linkIcon;
	}

}
