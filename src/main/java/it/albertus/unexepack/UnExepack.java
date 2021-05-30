package it.albertus.unexepack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.java.Log;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Parameters;

// Converted from the C language version at https://github.com/w4kfu/unEXEPACK
// See also: https://www.bamsoftware.com/software/exepack/
@Log
@Command
public class UnExepack implements Callable<Integer> {

	private static final int DOS_SIGNATURE = 0x5A4D;
	private static final int EXEPACK_SIGNATURE = 0x4252;

	private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	private static final int MAX_INPUT_FILE_SIZE = 0x800000; // 8 MiB, based on the info available at https://w4kfu.github.io/unEXEPACK/files/exepack_list.html
	private static final String LOGGING_FORMAT_PROPERTY = "java.util.logging.SimpleFormatter.format";

	@Value
	@RequiredArgsConstructor
	private static class DosHeader {

		private static final int SIZE = 14 * Short.BYTES; // bytes

		int eMagic;
		int eCblp;
		int eCp;
		int eCrlc;
		int eCparhdr;
		int eMinAlloc;
		int eMaxAlloc;
		int eSs;
		int eSp;
		int eCsum;
		int eIp;
		int eCs;
		int eLfarlc;
		int eOvno;

		private DosHeader(@NonNull final byte[] bytes) {
			if (bytes.length != SIZE) {
				throw new IllegalArgumentException("Invalid byte array size");
			}
			final ByteBuffer buf = ByteBuffer.wrap(bytes);
			buf.order(BYTE_ORDER);
			eMagic = Short.toUnsignedInt(buf.getShort(0));
			eCblp = Short.toUnsignedInt(buf.getShort(2));
			eCp = Short.toUnsignedInt(buf.getShort(4));
			eCrlc = Short.toUnsignedInt(buf.getShort(6));
			eCparhdr = Short.toUnsignedInt(buf.getShort(8));
			eMinAlloc = Short.toUnsignedInt(buf.getShort(10));
			eMaxAlloc = Short.toUnsignedInt(buf.getShort(12));
			eSs = Short.toUnsignedInt(buf.getShort(14));
			eSp = Short.toUnsignedInt(buf.getShort(16));
			eCsum = Short.toUnsignedInt(buf.getShort(18));
			eIp = Short.toUnsignedInt(buf.getShort(20));
			eCs = Short.toUnsignedInt(buf.getShort(22));
			eLfarlc = Short.toUnsignedInt(buf.getShort(24));
			eOvno = Short.toUnsignedInt(buf.getShort(26));
			if (!test()) {
				throw new IllegalArgumentException("This is not a valid MS-DOS executable");
			}
		}

		private boolean test() {
			if (eMagic != DOS_SIGNATURE) {
				return false;
			}
			/* at least one page */
			if (eCp == 0) {
				return false;
			}
			/* last page must not hold 0 bytes */
			if (eCblp == 0) {
				return false;
			}
			/* not even number of paragraphs */
			if (eCparhdr % 2 != 0) { // NOSONAR Replace this if-then-else statement by a single return statement. Return of boolean expressions should not be wrapped into an "if-then-else" statement (java:S1126)
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return String.format("eMagic = 0x%04X, eCblp = 0x%04X, eCp = 0x%04X, eCrlc = 0x%04X, eCparhdr = 0x%04X, eMinAlloc = 0x%04X, eMaxAlloc = 0x%04X, eSs = 0x%04X, eSp = 0x%04X, eCsum = 0x%04X, eIp = 0x%04X, eCs = 0x%04X, eLfarlc = 0x%04X, eOvno = 0x%04X", eMagic, eCblp, eCp, eCrlc, eCparhdr, eMinAlloc, eMaxAlloc, eSs, eSp, eCsum, eIp, eCs, eLfarlc, eOvno);
		}

		private byte[] toByteArray() {
			final ByteBuffer buf = ByteBuffer.allocate(SIZE);
			buf.order(BYTE_ORDER);
			buf.putShort((short) eMagic);
			buf.putShort((short) eCblp);
			buf.putShort((short) eCp);
			buf.putShort((short) eCrlc);
			buf.putShort((short) eCparhdr);
			buf.putShort((short) eMinAlloc);
			buf.putShort((short) eMaxAlloc);
			buf.putShort((short) eSs);
			buf.putShort((short) eSp);
			buf.putShort((short) eCsum);
			buf.putShort((short) eIp);
			buf.putShort((short) eCs);
			buf.putShort((short) eLfarlc);
			buf.putShort((short) eOvno);
			return buf.array();
		}
	}

	@Value
	private static class ExepackHeader {

		private static final int SIZE = 9 * Short.BYTES; // bytes

		int realIp;
		int realCs;
		int memStart; // temporary storage used by the decompression stub
		int exepackSize; // size of the entire EXEPACK block: header, stub, and packed relocation table
		int realSp;
		int realSs;
		int destLen; // size (in 16-byte paragraphs) of the uncompressed data
		int skipLen;
		int signature;

		private ExepackHeader(@NonNull final byte[] bytes) {
			if (bytes.length != SIZE) {
				throw new IllegalArgumentException("Invalid byte array size");
			}
			final ByteBuffer buf = ByteBuffer.wrap(bytes);
			buf.order(BYTE_ORDER);
			realIp = Short.toUnsignedInt(buf.getShort(0));
			realCs = Short.toUnsignedInt(buf.getShort(2));
			memStart = Short.toUnsignedInt(buf.getShort(4));
			exepackSize = Short.toUnsignedInt(buf.getShort(6));
			realSp = Short.toUnsignedInt(buf.getShort(8));
			realSs = Short.toUnsignedInt(buf.getShort(10));
			destLen = Short.toUnsignedInt(buf.getShort(12));
			final int word8 = Short.toUnsignedInt(buf.getShort(14));
			final int word9 = Short.toUnsignedInt(buf.getShort(16));
			if ((word9 != EXEPACK_SIGNATURE && word8 != EXEPACK_SIGNATURE) || exepackSize == 0x00) {
				throw new IllegalArgumentException("This is not a valid EXEPACK executable");
			}
			if (word8 == EXEPACK_SIGNATURE && word9 != EXEPACK_SIGNATURE) {
				this.skipLen = 1;
				this.signature = word8;
			}
			else {
				this.skipLen = word8;
				this.signature = word9;
			}
		}

		@Override
		public String toString() {
			return String.format("realIp = 0x%04X, realCs = 0x%04X, memStart = 0x%04X, exepackSize = 0x%04X, realSp = 0x%04X, realSs = 0x%04X, destLen = 0x%04X, skipLen = 0x%04X, signature = 0x%04X", realIp, realCs, memStart, exepackSize, realSp, realSs, destLen, skipLen, signature);
		}
	}

	// @formatter:off
	@Parameters(index = "0", paramLabel = "<EXEPACK_file>")
	private Path inputFile;

	@Parameters(index = "1", paramLabel = "OUTPUT_FILE", defaultValue = "unpacked", showDefaultValue = Visibility.ALWAYS, arity = "0..1")
	private Path outputFile;
	// @formatter:on

	private static void reverse(@NonNull final byte[] array) {
		for (int i = 0, j = array.length - 1; i < j; i++, j--) {
			final byte c = array[i];
			array[i] = array[j];
			array[j] = c;
		}
	}

	/* buf is already reversed, because EXEPACK use backward processing */
	private static byte[] unpackData(@NonNull final byte[] packedData, final int unpackedDataSize) {
		final byte[] unpackedData = new byte[unpackedDataSize];
		int i = 0;
		int curUnpackedDataSize = 0;
		while (packedData[i] == (byte) 0xFF) { // skip all 0xFF bytes (they're just padding to make the packed exe's size a multiple of 16)
			i++;
		}
		while (true) {
			final int opcode = Byte.toUnsignedInt(packedData[i++]);
			final int count = Byte.toUnsignedInt(packedData[i]) * 0x100 + Byte.toUnsignedInt(packedData[i + 1]);
			i += 2;
			if ((opcode & 0xFE) == 0xB0) { // fill
				log.log(Level.FINE, () -> String.format("fill opcode: 0x%02X, count: %d", opcode, count));
				final byte fillbyte = packedData[i++];
				if (curUnpackedDataSize + count > unpackedDataSize) {
					throw new BufferOverflowException();
				}
				Arrays.fill(unpackedData, curUnpackedDataSize, curUnpackedDataSize + count, fillbyte);
				curUnpackedDataSize += count;
			}
			else if ((opcode & 0xFE) == 0xB2) { // copy
				log.log(Level.FINE, () -> String.format("copy opcode: 0x%02X, count: %d", opcode, count));
				if (curUnpackedDataSize + count > unpackedDataSize) {
					throw new BufferOverflowException();
				}
				System.arraycopy(packedData, i, unpackedData, curUnpackedDataSize, count);
				curUnpackedDataSize += count;
				i += count;
			}
			else {
				throw new IllegalStateException(String.format("Unknown opcode: 0x%02X", opcode));
			}
			if ((opcode & 1) == 1)
				break;
		}
		if (i != packedData.length) {
			if (packedData.length - i > unpackedDataSize - curUnpackedDataSize) {
				throw new IllegalStateException("Data left are too large");
			}
			System.arraycopy(packedData, i, unpackedData, curUnpackedDataSize, packedData.length - i);
		}
		return unpackedData;
	}

	private static byte[] createRelocTable(@NonNull final byte[] packedExec, @NonNull final DosHeader dh, @NonNull final ExepackHeader eh) {
		final int exepackOffset = (dh.eCparhdr + dh.eCs) * 16;
		final byte[] bytePattern = new byte[] { (byte) 0xcd, 0x21, (byte) 0xb8, (byte) 0xff, 0x4c, (byte) 0xcd, 0x21 }; // the byte pattern that precedes the error message, cd 21 b8 ff 4c cd 21, which encodes the instructions int 0x21; mov ax, 0x4cff; int 0x21
		final int reloc = exepackOffset + memmem(Arrays.copyOfRange(packedExec, exepackOffset, packedExec.length), bytePattern) + bytePattern.length;
		final int relocLength = eh.exepackSize - (reloc - exepackOffset) + "Packed file is corrupt".length();
		final int nbReloc = (relocLength - 16 * Short.BYTES) / 2;
		final int relocTableSize = nbReloc * 2 * Short.BYTES;
		final ByteBuffer buf = ByteBuffer.wrap(packedExec);
		buf.order(BYTE_ORDER);
		buf.position(reloc + "Packed file is corrupt".length());
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			for (int i = 0; i < 16; i++) {
				final int count = Short.toUnsignedInt(buf.getShort());
				if (log.isLoggable(Level.FINE)) {
					log.fine(String.format("i: %d, count: %d", i, count));
				}
				for (int j = 0; j < count; j++) {
					if (baos.size() >= relocTableSize) {
						throw new BufferOverflowException();
					}
					final int entry = Short.toUnsignedInt(buf.getShort());
					if (log.isLoggable(Level.FINE)) {
						log.fine(String.format("i: %d, j: %d, entry: 0x%04X", i, j, entry));
					}
					baos.write(new byte[] { (byte) entry, (byte) (entry >> 8) });
					if (baos.size() >= relocTableSize) {
						throw new BufferOverflowException();
					}
					final int segment = (i * 0x1000) & 0xFFFF;
					baos.write(new byte[] { (byte) segment, (byte) (segment >> 8) });
				}
			}
			return baos.toByteArray();
		}
		catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static byte[] writeExe(@NonNull final DosHeader dh, @NonNull final byte[] unpackedData, @NonNull final byte[] reloc, final int padding) {
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			baos.write(dh.toByteArray());
			baos.write(reloc);
			for (int i = 0; i < padding; i++) {
				baos.write(0);
			}
			baos.write(unpackedData);
			return baos.toByteArray();
		}
		catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static byte[] craftExec(@NonNull final DosHeader dh, @NonNull final ExepackHeader eh, @NonNull final byte[] unpackedData, @NonNull final byte[] reloc) {
		final int headerSize = DosHeader.SIZE + reloc.length;
		final int eMagic = DOS_SIGNATURE;
		int eCparhdr = (headerSize / 16) & 0xFFFF;
		eCparhdr = (eCparhdr / 32 + 1) * 32;
		final int paddingLength = eCparhdr * 16 - headerSize;
		final int totalLength = headerSize + paddingLength + unpackedData.length;
		final int eSs = eh.realSs;
		final int eSp = eh.realSp;
		final int eIp = eh.realIp;
		final int eCs = eh.realCs;
		final int eMinAlloc = dh.eMinAlloc;
		final int eMaxAlloc = 0xFFFF;
		final int eLfarlc = DosHeader.SIZE;
		final int eCrlc = (reloc.length / (2 * Short.BYTES)) & 0xFFFF;
		final int eCblp = totalLength % 512;
		final int eCp = (totalLength / 512 + 1) & 0xFFFF;
		final DosHeader dhead = new DosHeader(eMagic, eCblp, eCp, eCrlc, eCparhdr, eMinAlloc, eMaxAlloc, eSs, eSp, 0, eIp, eCs, eLfarlc, 0);
		log.log(Level.INFO, "DOS header: {0}", dhead);
		return writeExe(dhead, unpackedData, reloc, paddingLength);
	}

	public static byte[] unpack(@NonNull final byte[] packedExec) {
		final DosHeader dh = new DosHeader(Arrays.copyOf(packedExec, DosHeader.SIZE));
		log.log(Level.INFO, "DOS header: {0}", dh);

		final int exepackOffset = (dh.eCparhdr + dh.eCs) * 16;
		final ExepackHeader eh = new ExepackHeader(Arrays.copyOfRange(packedExec, exepackOffset, exepackOffset + ExepackHeader.SIZE));
		log.log(Level.INFO, () -> String.format("Exepack header @ 0x%X: %s", exepackOffset, eh));

		final int unpackedDataSize = eh.destLen * 16;
		final int packedDataStart = dh.eCparhdr * 16;
		final int packedDataEnd = exepackOffset;

		final byte[] packedData = Arrays.copyOfRange(packedExec, packedDataStart, packedDataEnd);
		reverse(packedData);
		final byte[] unpackedData = unpackData(packedData, unpackedDataSize);
		reverse(unpackedData);
		final byte[] reloc = createRelocTable(packedExec, dh, eh);
		return craftExec(dh, eh, unpackedData, reloc);
	}

	public static void main(final String... args) {
		if (System.getProperty(LOGGING_FORMAT_PROPERTY) == null) {
			System.setProperty(LOGGING_FORMAT_PROPERTY, "%5$s%6$s%n");
		}
		System.exit(new CommandLine(new UnExepack()).setCommandName(UnExepack.class.getSimpleName().toLowerCase(Locale.ROOT)).execute(args));
	}

	@Override
	public Integer call() throws IOException {
		if (Files.size(inputFile) > MAX_INPUT_FILE_SIZE) {
			throw new IOException("Input file is too large");
		}
		final byte[] packedExec = Files.readAllBytes(inputFile);
		final byte[] unpackedExec = unpack(packedExec);
		Files.write(outputFile, unpackedExec);
		return ExitCode.OK;
	}

	private static int memmem(@NonNull final byte[] haystack, @NonNull final byte[] needle) {
		for (int i = 0; i < haystack.length - needle.length + 1; ++i) {
			boolean found = true;
			for (int j = 0; j < needle.length; ++j) {
				if (haystack[i + j] != needle[j]) {
					found = false;
					break;
				}
			}
			if (found)
				return i;
		}
		return -1;
	}

}
