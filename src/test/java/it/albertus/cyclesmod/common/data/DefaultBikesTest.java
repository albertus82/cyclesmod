package it.albertus.cyclesmod.common.data;

import java.io.IOException;
import java.util.zip.CRC32;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.cyclesmod.common.model.Inf;

public class DefaultBikesTest {

	@Test
	public void test() throws IOException {
		final byte[] bytes = DefaultBikes.getByteArray();
		Assert.assertEquals(Inf.FILE_SIZE, bytes.length);

		final CRC32 crc = new CRC32();
		crc.update(bytes);
		Assert.assertEquals(0x28A33682L, crc.getValue());
	}

}
