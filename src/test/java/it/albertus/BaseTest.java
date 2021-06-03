package it.albertus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;

public abstract class BaseTest {

	protected static final Properties projectProperties = new Properties();

	@BeforeClass
	public static void _beforeAll() throws IOException {
		try (final InputStream is = BaseTest.class.getResourceAsStream("/project.properties")) {
			projectProperties.load(is);
		}
	}

}
