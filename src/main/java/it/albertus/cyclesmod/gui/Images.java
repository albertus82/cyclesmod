package it.albertus.cyclesmod.gui;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import it.albertus.util.IOUtils;

public class Images {

	// Main application icon (in various formats)
	private static final Collection<Image> mainIcons = new LinkedHashSet<Image>();

	private Images() {
		throw new IllegalAccessError();
	}

	static {
		InputStream stream = null;
		try {
			stream = Images.class.getResourceAsStream("main.ico");
			for (final ImageData data : new ImageLoader().load(stream)) {
				mainIcons.add(new Image(Display.getCurrent(), data));
			}
		}
		finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static Image[] getMainIcons() {
		return mainIcons.toArray(new Image[mainIcons.size()]);
	}

}
