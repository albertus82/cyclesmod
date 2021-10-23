package io.github.albertus82.cyclesmod.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BuildInfoTest {

	private static final Collection<String> expectedPropertyNames = Arrays.asList("project.version", "version.timestamp");

	@Test
	void testApi() {
		Assertions.assertNull(BuildInfo.getProperty(UUID.randomUUID().toString()));
		Assertions.assertThrows(NullPointerException.class, () -> BuildInfo.getProperty(null));
		Assertions.assertDoesNotThrow(() -> BuildInfo.getProperty(""));
	}

	@Test
	void testExpectedProperties() {
		Assertions.assertEquals(expectedPropertyNames.size(), BuildInfo.INSTANCE.properties.size());
		Assertions.assertEquals(new TreeSet<>(expectedPropertyNames), new TreeSet<>(BuildInfo.INSTANCE.properties.keySet()));
		for (final String name : expectedPropertyNames) {
			final String value = BuildInfo.getProperty(name);
			Assertions.assertNotNull(value);
			Assertions.assertNotEquals(0, value.length());
			Assertions.assertFalse(value.contains("${"), name + '=' + value);
		}
	}

}
