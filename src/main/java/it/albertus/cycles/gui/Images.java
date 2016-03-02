package it.albertus.cycles.gui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class Images {

	/* Icona principale dell'applicazione (in vari formati) */
	public static final Image[] ICONS_TOOLS = loadIcons("tools.ico");

	private static Image[] loadIcons(final String fileName) {
		final InputStream is = Images.class.getResourceAsStream(fileName);
		final ImageData[] images = new ImageLoader().load(is);
		try {
			is.close();
		}
		catch (IOException ioe) {}
		final Image[] icons = new Image[images.length];
		int i = 0;
		for (final ImageData id : images) {
			icons[i++] = new Image(Display.getCurrent(), id);
		}
		return icons;
	}

}
