package it.albertus.cycles.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.cycles.model.BikesInf;
import it.albertus.util.IOUtils;

public class DefaultBikesTest {

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

}
