package it.albertus.cycles.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.cycles.model.BikesInf;
import it.albertus.util.IOUtils;
import it.albertus.util.logging.LoggerFactory;

public class DefaultBikesTest {

	private static final Logger logger = LoggerFactory.getLogger(DefaultBikesTest.class);

	@Test
	public void test() throws IOException {
		final InputStream is = new DefaultBikes().getInputStream();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			IOUtils.copy(is, os, 128);
		}
		finally {
			IOUtils.closeQuietly(os, is);
		}
		Assert.assertEquals(BikesInf.FILE_SIZE, os.size());
		final CRC32 crc = new CRC32();
		crc.update(os.toByteArray());
		Assert.assertEquals(DefaultBikes.CRC, crc.getValue());
	}

	@Test
	public void testReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
		final Field declaredField = DefaultBikes.class.getDeclaredField("DEFAULT");
		declaredField.setAccessible(true);
		final Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(declaredField, declaredField.getModifiers() & ~Modifier.FINAL);

		final int newLength = BikesInf.FILE_SIZE - 1 - (int) (Math.random() * 100);
		logger.info(Integer.toString(newLength));
		declaredField.set(null, Arrays.copyOf((byte[]) declaredField.get(null), newLength));
		try {
			new DefaultBikes();
			Assert.assertFalse(true);
		}
		catch (final StreamCorruptedException e) {
			logger.log(Level.INFO, e.toString(), e);
			Assert.assertTrue(true);
		}
	}

}
