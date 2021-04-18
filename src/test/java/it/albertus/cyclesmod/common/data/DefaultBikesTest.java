package it.albertus.cyclesmod.common.data;

import java.io.IOException;
import java.util.zip.CRC32;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.cyclesmod.common.model.BikesInf;

public class DefaultBikesTest {

	@Test
	public void test() throws IOException {
		final byte[] bytes = DefaultBikes.getByteArray();
		Assert.assertEquals(BikesInf.FILE_SIZE, bytes.length);

		final CRC32 crc = new CRC32();
		crc.update(bytes);
		Assert.assertEquals(DefaultBikes.CRC32, crc.getValue());
	}

}
