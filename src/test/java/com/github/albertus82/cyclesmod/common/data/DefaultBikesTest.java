package com.github.albertus82.cyclesmod.common.data;

import java.io.IOException;
import java.util.zip.CRC32;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.albertus82.cyclesmod.common.model.VehiclesInf;

class DefaultBikesTest {

	@Test
	void test() throws IOException {
		final byte[] bytes = DefaultBikes.getByteArray();
		Assertions.assertEquals(VehiclesInf.FILE_SIZE, bytes.length);

		final CRC32 crc = new CRC32();
		crc.update(bytes);
		Assertions.assertEquals(0x28A33682L, crc.getValue());
	}

}
