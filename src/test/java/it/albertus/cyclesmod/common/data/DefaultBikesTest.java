package it.albertus.cyclesmod.common.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.util.IOUtils;

public class DefaultBikesTest {

	@Test
	public void test() throws IOException {
		try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			try (final InputStream is = DefaultBikes.getInputStream()) {
				IOUtils.copy(is, os, 128);
			}
			Assert.assertEquals(BikesInf.FILE_SIZE, os.size());

			final CRC32 crc = new CRC32();
			crc.update(os.toByteArray());
			Assert.assertEquals(DefaultBikes.CRC, crc.getValue());

			crc.reset();
			crc.update(DefaultBikes.getByteArray());
			Assert.assertEquals(DefaultBikes.CRC, crc.getValue());
		}
	}

}
