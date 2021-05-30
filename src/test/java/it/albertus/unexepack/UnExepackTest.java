package it.albertus.unexepack;

import org.junit.Assert;
import org.junit.Test;

public class UnExepackTest {

	@Test
	public void testDecodeExeLen() {
		Assert.assertEquals(0, UnExepack.decodeExeLen(0, 0));
		Assert.assertEquals(1, UnExepack.decodeExeLen(1, 1));
		Assert.assertEquals(511, UnExepack.decodeExeLen(511, 1));
		Assert.assertEquals(512, UnExepack.decodeExeLen(0, 1));
		Assert.assertEquals(513, UnExepack.decodeExeLen(1, 2));
		Assert.assertEquals(0xFFFF * 512 - 1, UnExepack.decodeExeLen(511, 0xFFFF));
		Assert.assertEquals(0xFFFF * 512, UnExepack.decodeExeLen(0, 0xFFFF));

		// When e_cp == 0, e_cblp must be 0, otherwise it would encode a negative length.
		Assert.assertEquals(-1, UnExepack.decodeExeLen(1, 0));
		Assert.assertEquals(-1, UnExepack.decodeExeLen(511, 0));
		// e_cblp must be <= 511.
		Assert.assertEquals(-1, UnExepack.decodeExeLen(512, 1));
	}

}
