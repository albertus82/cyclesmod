package io.github.albertus82.unexepack;

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
import lombok.extern.java.Log;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Parameters;

// Translated from the C code @ https://github.com/w4kfu/unEXEPACK
// See also: https://www.bamsoftware.com/software/exepack/
@Log
@Command(description = "Unpacker for Microsoft EXEPACK utility compressor.")
public class UnExepack implements Callable<Integer> {

	private static final int MAX_INPUT_FILE_SIZE = 0x800000; // 8 MiB, based on the info available at https://w4kfu.github.io/unEXEPACK/files/exepack_list.html
	private static final String LOGGING_FORMAT_PROPERTY = "java.util.logging.SimpleFormatter.format";

	@Parameters(index = "0", paramLabel = "<EXEPACK_file>")
	private Path inputFile;

	@Parameters(index = "1", paramLabel = "OUTPUT_FILE", defaultValue = "unpacked", showDefaultValue = Visibility.ALWAYS, arity = "0..1")
	private Path outputFile;

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
		final int exepackOffset = (dh.getECparhdr() + dh.getECs()) * 16;
		final byte[] bytePattern = new byte[] { (byte) 0xcd, 0x21, (byte) 0xb8, (byte) 0xff, 0x4c, (byte) 0xcd, 0x21 }; // the byte pattern that precedes the error message, cd 21 b8 ff 4c cd 21, which encodes the instructions int 0x21; mov ax, 0x4cff; int 0x21
		final int reloc = exepackOffset + memmem(Arrays.copyOfRange(packedExec, exepackOffset, packedExec.length), bytePattern) + bytePattern.length;
		final int relocLength = eh.getExepackSize() - (reloc - exepackOffset) + "Packed file is corrupt".length();
		final int nbReloc = (relocLength - 16 * Short.BYTES) / 2;
		final int relocTableSize = nbReloc * 2 * Short.BYTES;
		final ByteBuffer buf = ByteBuffer.wrap(packedExec);
		buf.order(ByteOrder.LITTLE_ENDIAN);
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
		final byte[] header = dh.toByteArray();
		final ByteBuffer buf = ByteBuffer.allocate(header.length + reloc.length + padding + unpackedData.length);
		buf.put(header);
		buf.put(reloc);
		buf.position(buf.position() + padding);
		buf.put(unpackedData);
		return buf.array();
	}

	private static byte[] craftExec(@NonNull final DosHeader dh, @NonNull final ExepackHeader eh, @NonNull final byte[] unpackedData, @NonNull final byte[] reloc) {
		final int headerSize = DosHeader.SIZE + reloc.length;
		final int eMagic = DosHeader.SIGNATURE;
		int eCparhdr = (headerSize / 16) & 0xFFFF;
		eCparhdr = (eCparhdr / 32 + 1) * 32;
		final int paddingLength = eCparhdr * 16 - headerSize;
		final int totalLength = headerSize + paddingLength + unpackedData.length;
		final int eSs = eh.getRealSs();
		final int eSp = eh.getRealSp();
		final int eIp = eh.getRealIp();
		final int eCs = eh.getRealCs();
		final int eMinAlloc = dh.getEMinAlloc();
		final int eMaxAlloc = 0xFFFF;
		final int eLfarlc = DosHeader.SIZE;
		final int eCrlc = (reloc.length / (2 * Short.BYTES)) & 0xFFFF;
		final int eCblp = totalLength % 512;
		final int eCp = (totalLength / 512 + 1) & 0xFFFF;
		final DosHeader dhead = new DosHeader(eMagic, eCblp, eCp, eCrlc, eCparhdr, eMinAlloc, eMaxAlloc, eSs, eSp, 0, eIp, eCs, eLfarlc, 0);
		log.log(Level.INFO, "DOS header: {0}", dhead);
		return writeExe(dhead, unpackedData, reloc, paddingLength);
	}

	public static byte[] unpack(@NonNull final byte[] packedExec) throws InvalidDosHeaderException, InvalidExepackHeaderException {
		final DosHeader dh = new DosHeader(Arrays.copyOf(packedExec, DosHeader.SIZE));
		log.log(Level.INFO, "DOS header: {0}", dh);

		final int exeLen = decodeExeLen(dh.getECblp(), dh.getECp());
		if (exeLen < packedExec.length) {
			log.log(Level.WARNING, "EXE file size is {0,number,#}; ignoring {1,number,#} trailing bytes.", new Integer[] { exeLen, packedExec.length - exeLen });
		}

		final int exepackOffset = (dh.getECparhdr() + dh.getECs()) * 16;
		final ExepackHeader eh = new ExepackHeader(Arrays.copyOfRange(packedExec, exepackOffset, exepackOffset + ExepackHeader.SIZE));
		log.log(Level.INFO, () -> String.format("Exepack header @ 0x%X: %s", exepackOffset, eh));

		final int unpackedDataSize = eh.getDestLen() * 16;
		final int packedDataStart = dh.getECparhdr() * 16;
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
			System.setProperty(LOGGING_FORMAT_PROPERTY, "%4$s: %5$s%6$s%n");
		}
		System.exit(new CommandLine(new UnExepack()).setCommandName(UnExepack.class.getSimpleName().toLowerCase(Locale.ROOT)).execute(args));
	}

	@Override
	public Integer call() throws IOException {
		if (!inputFile.toFile().exists() || Files.isDirectory(inputFile)) {
			log.severe("The input file does not exist.");
			return ExitCode.SOFTWARE;
		}
		if (Files.size(inputFile) > MAX_INPUT_FILE_SIZE) {
			log.severe("The input file is too large.");
			return ExitCode.SOFTWARE;
		}
		final byte[] packedExec = Files.readAllBytes(inputFile);
		final byte[] unpackedExec;
		try {
			unpackedExec = unpack(packedExec);
		}
		catch (final InvalidDosHeaderException e) {
			log.severe("The input file is not a valid MS-DOS executable.");
			return ExitCode.SOFTWARE;
		}
		catch (final InvalidExepackHeaderException e) {
			log.severe("The input file is not a valid EXEPACK executable.");
			return ExitCode.SOFTWARE;
		}
		Files.write(outputFile, unpackedExec);
		return ExitCode.OK;
	}

	public static int memmem(@NonNull final byte[] haystack, @NonNull final byte[] needle) {
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

	static int decodeExeLen(final int eCblp, final int eCp) {
		if (eCblp == 0) {
			return eCp * 512;
		}
		else if (eCp == 0) {
			return -1;
		}
		else if (eCblp >= 1 && eCblp <= 511) {
			return (eCp - 1) * 512 + eCblp;
		}
		else {
			return -1;
		}
	}

}
