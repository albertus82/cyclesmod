package it.albertus.cyclesmod.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.logging.Level;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.listener.LinkSelectionListener;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.closeable.CloseableResource;
import it.albertus.util.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
public class AboutDialog extends Dialog {

	private static final float MONITOR_SIZE_DIVISOR = 1.2f;
	private static final float TITLE_FONT_SIZE_MULTIPLIER = 1.6f;

	private static final int ICON_VERTICAL_SIZE_DLUS = 26;
	private static final int SCROLLABLE_VERTICAL_SIZE_DLUS = 34; // scrollbar issues below 34 on GTK

	private static final String SYM_NAME_FONT_APPNAME = AboutDialog.class.getName() + ".appname";
	private static final String SYM_NAME_FONT_DEFAULT = AboutDialog.class.getName() + ".default";

	private final LinkSelectionListener linkSelectionListener = new LinkSelectionListener();

	private Text appLicenseText;
	private ScrolledComposite thirdPartyScrolledComposite;

	public AboutDialog(@NonNull final Shell parent) {
		this(parent, SWT.SHEET | SWT.RESIZE);
	}

	private AboutDialog(@NonNull final Shell parent, final int style) {
		super(parent, style);
		this.setText(Messages.get("gui.label.about.title"));
	}

	public void open() {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setImages(Images.getAppIconArray());
		createContents(shell);
		constrainShellSize(shell);
		shell.open();
	}

	private void createContents(@NonNull final Shell shell) {
		GridLayoutFactory.swtDefaults().applyTo(shell);

		createHeaderComposite(shell, GridDataFactory.swtDefaults().create());

		createAcknowledgementsGroup(shell, GridDataFactory.fillDefaults().grab(true, false).create());

		addUnobtrusiveSeparator(shell);

		final Link appLicenseLink = new Link(shell, SWT.WRAP);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(appLicenseLink);
		appLicenseLink.setText(Messages.get("gui.label.about.license", buildAnchor(Messages.get("gui.label.about.license.gpl.a.href"), Messages.get("gui.label.about.license.gpl.a.text"))));
		appLicenseLink.addSelectionListener(linkSelectionListener);

		appLicenseText = new Text(shell, SWT.BORDER | SWT.V_SCROLL);
		appLicenseText.setText(loadTextResource("/META-INF/LICENSE.txt"));
		appLicenseText.setEditable(false);
		appLicenseText.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, SwtUtils.convertVerticalDLUsToPixels(appLicenseText, SCROLLABLE_VERTICAL_SIZE_DLUS)).applyTo(appLicenseText);

		addUnobtrusiveSeparator(shell);

		final Label thirdPartySoftwareLabel = new Label(shell, SWT.WRAP);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(thirdPartySoftwareLabel);
		thirdPartySoftwareLabel.setText(Messages.get("gui.label.about.3rdparty"));

		thirdPartyScrolledComposite = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.H_SCROLL);
		thirdPartyScrolledComposite.setLayout(new FillLayout());
		thirdPartyScrolledComposite.setExpandVertical(true);
		thirdPartyScrolledComposite.setExpandHorizontal(true);
		final ThirdPartySoftwareTable thirdPartySoftwareTable = new ThirdPartySoftwareTable(thirdPartyScrolledComposite, null);
		thirdPartyScrolledComposite.setContent(thirdPartySoftwareTable.getTableViewer().getControl());
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, SwtUtils.convertVerticalDLUsToPixels(thirdPartyScrolledComposite, SCROLLABLE_VERTICAL_SIZE_DLUS)).applyTo(thirdPartyScrolledComposite);

		final Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText(Messages.get("gui.label.button.ok"));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(okButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).minSize(buttonWidth, SWT.DEFAULT).applyTo(okButton);
		okButton.setFocus();
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent se) {
				shell.close();
			}
		});
		shell.setDefaultButton(okButton);
	}

	private void createHeaderComposite(@NonNull final Composite parent, final Object layoutData) {
		final Composite headerComposite = new Composite(parent, SWT.NONE);
		if (layoutData != null) {
			headerComposite.setLayoutData(layoutData);
		}
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(headerComposite);

		final Label iconLabel = new Label(headerComposite, SWT.NONE);
		GridDataFactory.swtDefaults().span(1, 2).grab(true, false).applyTo(iconLabel);
		for (final Entry<Rectangle, Image> entry : Images.getAppIconMap().entrySet()) {
			final int pixels = SwtUtils.convertVerticalDLUsToPixels(iconLabel, ICON_VERTICAL_SIZE_DLUS);
			if (entry.getKey().height <= pixels) {
				log.log(Level.FINE, "{0,number,#} DLUs -> {1,number,#} pixels -> {2}", new Object[] { ICON_VERTICAL_SIZE_DLUS, pixels, entry });
				iconLabel.setImage(entry.getValue());
				break;
			}
		}

		GridDataFactory.swtDefaults().span(1, 2).applyTo(new Label(headerComposite, SWT.NONE)); // Invisible separator

		final Label applicationNameLabel = new Label(headerComposite, SWT.WRAP);
		final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (!fontRegistry.hasValueFor(SYM_NAME_FONT_APPNAME)) {
			final FontData[] fontData = applicationNameLabel.getFont().getFontData();
			for (final FontData fd : fontData) {
				fd.setHeight(Math.round(fd.getHeight() * TITLE_FONT_SIZE_MULTIPLIER));
			}
			fontRegistry.put(SYM_NAME_FONT_APPNAME, fontData);
		}
		applicationNameLabel.setFont(fontRegistry.getBold(SYM_NAME_FONT_APPNAME));
		GridDataFactory.swtDefaults().grab(true, false).applyTo(applicationNameLabel);
		applicationNameLabel.setText(Messages.get("gui.message.application.name"));

		final Link versionAndHomePageLink = new Link(headerComposite, SWT.NONE);
		Date versionDate;
		try {
			versionDate = Version.getDate();
		}
		catch (final ParseException e) {
			log.log(Level.WARNING, "Invalid version date:", e);
			versionDate = new Date();
		}
		final String version = Messages.get("gui.label.about.version", Version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(versionDate));
		final String homePageAnchor = buildAnchor(Messages.get("gui.message.project.url"), Messages.get("gui.label.about.home.page"));
		versionAndHomePageLink.setText(version + " - " + homePageAnchor);
		if (!fontRegistry.hasValueFor(SYM_NAME_FONT_DEFAULT)) {
			fontRegistry.put(SYM_NAME_FONT_DEFAULT, versionAndHomePageLink.getFont().getFontData());
		}
		versionAndHomePageLink.setFont(fontRegistry.getBold(SYM_NAME_FONT_DEFAULT));
		GridDataFactory.swtDefaults().grab(true, false).applyTo(versionAndHomePageLink);
		versionAndHomePageLink.addSelectionListener(linkSelectionListener);
	}

	private void createAcknowledgementsGroup(@NonNull final Composite parent, final Object layoutData) {
		final Group acknowledgementsGroup = new Group(parent, SWT.NONE);
		if (layoutData != null) {
			acknowledgementsGroup.setLayoutData(layoutData);
		}
		acknowledgementsGroup.setForeground(acknowledgementsGroup.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		acknowledgementsGroup.setText(Messages.get("gui.label.about.acknowledgements"));
		GridLayoutFactory.swtDefaults().applyTo(acknowledgementsGroup);

		final Link acknowledgementsIconLink = new Link(acknowledgementsGroup, SWT.WRAP);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(acknowledgementsIconLink);
		acknowledgementsIconLink.setText(Messages.get("gui.label.about.acknowledgements.icon", buildAnchor(Messages.get("gui.label.about.acknowledgements.icon.a.href"), Messages.get("gui.label.about.acknowledgements.icon.a.text"))));
		acknowledgementsIconLink.addSelectionListener(linkSelectionListener);
	}

	private void constrainShellSize(@NonNull final Shell shell) {
		int appLicenseTextWidth = appLicenseText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		log.log(Level.FINE, "appLicenseTextWidth: {0,number,#}", appLicenseTextWidth);
		if (appLicenseText.getVerticalBar() != null && !appLicenseText.getVerticalBar().isDisposed()) {
			appLicenseTextWidth += Math.round(appLicenseText.getVerticalBar().getSize().x * 1.5f);
		}
		int thirdPartyScrolledCompositeWidth = thirdPartyScrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		log.log(Level.FINE, "thirdPartyScrolledCompositeWidth: {0,number,#}", thirdPartyScrolledCompositeWidth);
		if (thirdPartyScrolledComposite.getVerticalBar() != null && !thirdPartyScrolledComposite.getVerticalBar().isDisposed()) {
			thirdPartyScrolledCompositeWidth += Math.round(thirdPartyScrolledComposite.getVerticalBar().getSize().x * 1.5f);
		}
		final int clientWidth = shell.getMonitor().getClientArea().width;
		log.log(Level.FINE, "clientWidth: {0,number,#}", clientWidth);
		int shellInitialWidth = Math.max(appLicenseTextWidth, thirdPartyScrolledCompositeWidth);
		if (shellInitialWidth > clientWidth / MONITOR_SIZE_DIVISOR) {
			shellInitialWidth = Math.round(clientWidth / MONITOR_SIZE_DIVISOR);
		}

		final Point shellInitialSize = new Point(shellInitialWidth, shell.getSize().y);
		log.log(Level.FINE, "shellInitialSize: {0}", shellInitialSize);
		shell.setSize(shellInitialSize);

		final Point shellMinimumSize = new Point(appLicenseTextWidth, shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
		log.log(Level.FINE, "shellMinimumSize: {0}", shellMinimumSize);
		shell.setMinimumSize(shellMinimumSize);
	}

	private String loadTextResource(@NonNull final String name) {
		final StringBuilder text = new StringBuilder();
		try (final InputStream is = getClass().getResourceAsStream(name); final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8); final BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				text.append(System.lineSeparator()).append(line);
			}
		}
		catch (final Exception e) {
			log.log(Level.WARNING, "Cannot load text resource " + name + ':', e);
		}
		return text.length() <= System.lineSeparator().length() ? "" : text.substring(System.lineSeparator().length());
	}

	private static String buildAnchor(@NonNull final String href, @NonNull final String label) {
		return new StringBuilder("<a href=\"").append(href).append("\">").append(label).append("</a>").toString();
	}

	private static void addUnobtrusiveSeparator(@NonNull final Composite parent) {
		final Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR | SWT.SHADOW_NONE); // Invisible separator
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(separator);
	}

	@Getter
	private static class ThirdPartySoftwareTable {

		private static final byte COL_IDX_THIRDPARTY_NAME = 0;
		private static final byte COL_IDX_THIRDPARTY_AUTHOR = 1;
		private static final byte COL_IDX_THIRDPARTY_LICENSE = 2;
		private static final byte COL_IDX_THIRDPARTY_HOMEPAGE = 3;

		private final TableViewer tableViewer;

		private ThirdPartySoftwareTable(@NonNull final Composite parent, final Object layoutData) {
			tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
			ColumnViewerToolTipSupport.enableFor(tableViewer);
			final Table table = tableViewer.getTable();
			if (layoutData != null) {
				table.setLayoutData(layoutData);
			}
			table.setLinesVisible(true);
			table.setHeaderVisible(true);

			final TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			nameColumn.getColumn().setText(Messages.get("gui.label.about.3rdparty.name"));
			nameColumn.setLabelProvider(new TextColumnLabelProvider(ThirdPartySoftware::getName));

			final TableViewerColumn authorColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			authorColumn.getColumn().setText(Messages.get("gui.label.about.3rdparty.author"));
			authorColumn.setLabelProvider(new TextColumnLabelProvider(ThirdPartySoftware::getAuthor));

			final TableViewerColumn licenseColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			licenseColumn.setLabelProvider(new LinkStyledCellLabelProvider(Messages.get("gui.label.about.3rdparty.license"), ThirdPartySoftware::getLicenseUri));

			final TableViewerColumn homePageColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			homePageColumn.setLabelProvider(new LinkStyledCellLabelProvider(Messages.get("gui.label.about.3rdparty.homepage"), ThirdPartySoftware::getHomePageUri));

			tableViewer.add(ThirdPartySoftware.loadFromProperties().toArray());

			packColumns();

			configureMouseListener();
			configureMouseMoveListener();
		}

		private void configureMouseListener() {
			tableViewer.getTable().addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(final MouseEvent e) {
					if (e.button == 1) {
						final ViewerCell cell = tableViewer.getCell(new Point(e.x, e.y));
						if (cell != null && cell.getElement() instanceof ThirdPartySoftware) {
							final ThirdPartySoftware element = (ThirdPartySoftware) cell.getElement();
							if (cell.getColumnIndex() == COL_IDX_THIRDPARTY_LICENSE) {
								Program.launch(element.getLicenseUri().toString());
							}
							else if (cell.getColumnIndex() == COL_IDX_THIRDPARTY_HOMEPAGE) {
								Program.launch(element.getHomePageUri().toString());
							}
						}
					}
				}
			});
		}

		private void configureMouseMoveListener() {
			final Table table = tableViewer.getTable();
			final Control parent = table.getParent();
			table.addMouseMoveListener(e -> {
				final ViewerCell cell = tableViewer.getCell(new Point(e.x, e.y));
				if (cell != null && cell.getColumnIndex() != COL_IDX_THIRDPARTY_NAME && cell.getColumnIndex() != COL_IDX_THIRDPARTY_AUTHOR) {
					if (parent.getCursor() == null) {
						parent.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
					}
				}
				else if (parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND).equals(parent.getCursor())) {
					parent.setCursor(null);
				}
			});
		}

		private void packColumns() {
			for (final TableColumn column : tableViewer.getTable().getColumns()) {
				packColumn(column);
			}
		}

		private static void packColumn(final TableColumn column) {
			column.pack();
			if (Util.isCocoa()) { // colmuns are badly resized on Cocoa, more space is actually needed
				try (final CloseableResource<GC> cr = new CloseableResource<>(new GC(column.getParent()))) {
					column.setWidth(column.getWidth() + cr.getResource().stringExtent("  ").x);
				}
			}
			else if (Util.isGtk()) { // colmuns are badly resized on GTK, more space is actually needed
				try (final CloseableResource<GC> cr = new CloseableResource<>(new GC(column.getParent()))) {
					column.setWidth(column.getWidth() + cr.getResource().stringExtent(" ").x);
				}
			}
		}

		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		private static class TextColumnLabelProvider extends ColumnLabelProvider {

			private final Function<ThirdPartySoftware, String> textFunction;

			@Override
			public String getText(final Object element) {
				if (element instanceof ThirdPartySoftware) {
					final ThirdPartySoftware thirdPartySoftware = (ThirdPartySoftware) element;
					return textFunction.apply(thirdPartySoftware);
				}
				else {
					return super.getText(element);
				}
			}
		}

		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		private static class LinkStyledCellLabelProvider extends StyledCellLabelProvider { // NOSONAR This class has 6 parents which is greater than 5 authorized. Inheritance tree of classes should not be too deep (java:S110)

			private final String label;
			private final Function<ThirdPartySoftware, URI> toolTipTextFunction;

			@Override
			public void update(final ViewerCell cell) {
				setLinkStyle(cell, label);
				super.update(cell);
			}

			@Override
			public String getToolTipText(final Object element) {
				if (element instanceof ThirdPartySoftware) {
					final ThirdPartySoftware thirdPartySoftware = (ThirdPartySoftware) element;
					return String.valueOf(toolTipTextFunction.apply(thirdPartySoftware));
				}
				else {
					return super.getToolTipText(element);
				}
			}

			private static void setLinkStyle(final ViewerCell cell, final String label) {
				cell.setForeground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND));
				cell.setText(label);
				final StyleRange styleRange = new StyleRange();
				styleRange.underline = true;
				styleRange.length = label.length();
				cell.setStyleRanges(new StyleRange[] { styleRange });
			}
		}

		@Getter(AccessLevel.PRIVATE)
		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		private static class ThirdPartySoftware implements Comparable<ThirdPartySoftware> {

			private final String name;
			private final String author;
			private final URI licenseUri;
			private final URI homePageUri;

			private static Collection<ThirdPartySoftware> loadFromProperties() {
				final Properties properties = new Properties();
				try (final InputStream is = ThirdPartySoftware.class.getResourceAsStream("3rdparty.properties")) {
					if (is != null) {
						properties.load(is);
					}
				}
				catch (final IOException e) {
					throw new UncheckedIOException(e);
				}
				final Collection<ThirdPartySoftware> set = new TreeSet<>();
				for (byte i = 1; i < Byte.MAX_VALUE; i++) {
					final String name = properties.getProperty(i + ".name");
					if (name == null) {
						break;
					}
					set.add(new ThirdPartySoftware(name, properties.getProperty(i + ".author"), URI.create(properties.getProperty(i + ".licenseUri")), URI.create(properties.getProperty(i + ".homePageUri"))));
				}
				return set;
			}

			@Override
			public boolean equals(final Object obj) {
				if (this == obj) {
					return true;
				}
				if (!(obj instanceof ThirdPartySoftware)) {
					return false;
				}
				final ThirdPartySoftware other = (ThirdPartySoftware) obj;
				return name.equalsIgnoreCase(other.name);
			}

			@Override
			public int hashCode() {
				return name.toLowerCase().hashCode();
			}

			@Override
			public int compareTo(final ThirdPartySoftware o) {
				return name.compareToIgnoreCase(o.name);
			}
		}
	}

}
