package it.albertus.cyclesmod.common.util;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BuildInfoTest {

	@Test
	void testBuildInfo() {
		Assertions.assertNotEquals(0, BuildInfo.INSTANCE.properties.size());
		Assertions.assertNull(BuildInfo.getProperty(UUID.randomUUID().toString()));
		Assertions.assertThrows(NullPointerException.class, () -> BuildInfo.getProperty(null));
	}

}
