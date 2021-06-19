package it.albertus.unexepack;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import it.albertus.BaseTest;
import lombok.extern.java.Log;

@Log
public class UnExepackTest extends BaseTest {

	private static final Map<String, String> digests = new HashMap<>();

	@BeforeClass
	public static void beforeAll() {
		digests.put("A.EXE", "592744c31541044c673e4647cc75f853abbc508d7bc56dcbcae9578e07f2f39c");
		digests.put("B.EXE", "d5d3127e36ec942fd8f722714f6604f28a2fed5c305e5c91648d16285396cf2b");
		digests.put("C.EXE", "0a02d7e93da2b92914142daeea344c4b8d8adfce6dfae44118c46b65024ceaef");
		digests.put("D.EXE", "5fd858d7e7a4d10955d86496f7b19b1adebcb32eb0fb54f7733893ddca20a2d8");
		digests.put("E.EXE", "9402a6f4ca05cdbb26d612f3ab79b72bce2899da96259d1d042ef164d6f159a6");
		digests.put("F.EXE", "c82518469a2c2f06a24f8fb37a59459ae1ce19186dd71095e3ed5b3695d0f6ca");
		digests.put("G.EXE", "c91aa00c7de39088a2b5b92e89850ea8aaa3590eda2f1d729a5425c029673fcb");
		digests.put("H.EXE", "9402a6f4ca05cdbb26d612f3ab79b72bce2899da96259d1d042ef164d6f159a6");
		digests.put("I.EXE", "e9df83a3a7c4c47e544b1c3023986eb99be330ed779a3ede8ac61cdcfb22d046");
		digests.put("J.EXE", "ceb273d445168ca60dd5ac458f7207fe03aa21986521903a08c177346ea8a0f2");
		digests.put("K.EXE", "9b8dc9e4208ef1b4a2af3b516c23a64007f55e9b24ab4fd019f68d454d380c8a");
		digests.put("L.EXE", "f5923fa58a07523a8ca850684b6c96737ac9cde9e938f16f1d48105bc9e8267b");
		digests.put("M.EXE", "8f397594e14eaf2b3e6174c4c747366dde1ef864a82d292c08225af4452537eb");
	}

	@Test
	public void testUnpack() throws IOException, InvalidHeaderException {
		final String propertyName = "testSecret";
		final String secret = System.getProperty(propertyName);
		if (secret == null) {
			log.log(Level.WARNING, "Missing system property ''{0}'', skipping unpacking test.", propertyName);
		}
		Assume.assumeNotNull(secret);
		final Path path = Paths.get(projectProperties.getProperty("project.build.testSourceDirectory"), "..", "resources", "exepacked.7z");
		final SevenZFile sevenZFile = new SevenZFile(path.toFile(), secret.toCharArray());
		SevenZArchiveEntry entry;
		while ((entry = sevenZFile.getNextEntry()) != null) {
			log.log(Level.INFO, "{0}", entry.getName());
			final byte[] buf = new byte[(int) entry.getSize()];
			Assert.assertEquals(entry.getSize(), sevenZFile.read(buf));
			Assert.assertEquals(-1, sevenZFile.read());
			Assert.assertEquals(digests.get(entry.getName()), DigestUtils.sha256Hex(UnExepack.unpack(buf)));
		}
	}

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
