package it.albertus.cyclesmod.common.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.naming.SizeLimitExceededException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DataUtils {

	static byte[] inflate(@NonNull final String deflatedBase64, final int expectedSize, final long expectedCrc32) throws DataFormatException, InvalidSizeException, InvalidChecksumException {
		final Inflater inflater = new Inflater();
		inflater.setInput(Base64.getDecoder().decode(deflatedBase64));
		final byte[] bytes = new byte[expectedSize];
		try {
			final int actualSize = inflater.inflate(bytes);
			if (actualSize != expectedSize) {
				throw new InvalidSizeException(expectedSize, actualSize);
			}
		}
		finally {
			inflater.end();
		}
		final long actualCrc32 = computeCrc32(bytes);
		if (actualCrc32 != expectedCrc32) {
			throw new InvalidChecksumException(expectedCrc32, actualCrc32);
		}
		return bytes;
	}

	private static long computeCrc32(@NonNull final byte[] bytes) {
		final Checksum crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		return crc.getValue();
	}

	static String deflateToBase64(@NonNull final Path path) throws IOException, SizeLimitExceededException {
		final long inputFileSize = Files.size(path);
		final int sizeLimit = 0x200000;
		if (inputFileSize > sizeLimit) { // 2 MiB
			throw new SizeLimitExceededException("Input file exceeds the limit of " + sizeLimit + " bytes");
		}
		final double inverse = 1d / inputFileSize;
		final int bufferSize = (int) (inputFileSize + inputFileSize * 0.1 + inverse * 1000);
		final byte[] outputBytes = new byte[bufferSize];
		final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		try {
			deflater.setInput(Files.readAllBytes(path));
			deflater.finish();
			int compressedDataLength = deflater.deflate(outputBytes);
			if (compressedDataLength <= 0) {
				throw new IOException();
			}
			return Base64.getEncoder().encodeToString(Arrays.copyOf(outputBytes, compressedDataLength));
		}
		finally {
			deflater.end();
		}
	}

}
