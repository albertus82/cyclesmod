package io.github.albertus82;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {

	protected static final Properties projectProperties = new Properties();

	@BeforeAll
	static void _beforeAll() throws IOException {
		try (final InputStream is = BaseTest.class.getResourceAsStream("/project.properties")) {
			projectProperties.load(is);
		}
	}

}
