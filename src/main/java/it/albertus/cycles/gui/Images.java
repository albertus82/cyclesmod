package it.albertus.cycles.gui;

import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import it.albertus.util.IOUtils;

public class Images {

	/* Icona principale dell'applicazione (in vari formati) */
	static final Image[] MAIN_ICONS = loadIcons("main.ico");

	private Images() {
		throw new IllegalAccessError();
	}

	private static Image[] loadIcons(final String fileName) {
		final InputStream is = Images.class.getResourceAsStream(fileName);
		final ImageData[] images = new ImageLoader().load(is);
		IOUtils.closeQuietly(is);
		final Image[] icons = new Image[images.length];
		int i = 0;
		for (final ImageData id : images) {
			icons[i++] = new Image(Display.getCurrent(), id);
		}
		return icons;
	}

}
