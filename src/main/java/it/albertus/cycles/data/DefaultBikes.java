package it.albertus.cycles.data;

import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.openjpa.lib.util.Base16Encoder;

public class DefaultBikes {

	private static final byte[] DEFAULT = Base16Encoder.decode("0600A82FB0367201EE02EE02EE02EE02620262021C25000000B000990085007300650059191919191919191919191919191919191919191919191919191919191919191919191919191919191919191919191919191919191919191919191919191C1C1C1C1C1C1F1F1F1F1F1F1F21232324242424242424242424242424212121212121212121190F05000000000000000000000600A82FB0367201EE02EE02EE02EE02760276021C25000000A00088007300610056004E252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252A2A2A2A2A2A2A2A2D2F2F2F2F2F3234373737373737373737373732323232323232322F2D28231C140F0A050000000000000006003831B03672010C030C03EE02EE028A028A02222400000092007C00690059004E00442F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F414141414141414141414141414B4B4B4B4B4B4B4B4B4B4B4B4B4B4B4B4B4B4B4B505050505050504B4B4B4B4B4B4B4B4B4B4B4641372D23140A050000000000");

	private final ByteArrayInputStream inputStream;

	public ByteArrayInputStream getInputStream() {
		return inputStream;
	}

	public DefaultBikes() throws IOException {
		this.inputStream = openBikesInfInputStream();
	}

	public ByteArrayInputStream openBikesInfInputStream() throws IOException {
		final Checksum crc = new CRC32();
		crc.update(DEFAULT, 0, DEFAULT.length);
		if (crc.getValue() != BikesInf.FILE_CRC) {
			throw new StreamCorruptedException(Resources.get("err.original.file.corrupted.crc", BikesInf.FILE_NAME, String.format("%08X", BikesInf.FILE_CRC), String.format("%08X", crc.getValue())));
		}
		if (DEFAULT.length != BikesInf.FILE_SIZE) {
			throw new StreamCorruptedException(Resources.get("err.original.file.corrupted.size", BikesInf.FILE_NAME, BikesInf.FILE_SIZE, DEFAULT.length));
		}
		return new ByteArrayInputStream(DEFAULT);
	}

}
