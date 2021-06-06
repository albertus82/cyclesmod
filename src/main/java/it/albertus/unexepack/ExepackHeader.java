package it.albertus.unexepack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.NonNull;
import lombok.Value;

@Value
class ExepackHeader {

	static final int SIGNATURE = 0x4252; // RB
	static final int SIZE = 9 * Short.BYTES; // bytes

	int realIp;
	int realCs;
	int memStart; // temporary storage used by the decompression stub
	int exepackSize; // size of the entire EXEPACK block: header, stub, and packed relocation table
	int realSp;
	int realSs;
	int destLen; // size (in 16-byte paragraphs) of the uncompressed data
	int skipLen;
	int signature; // NOSONAR Rename field "signature" to prevent any misunderstanding/clash with field "SIGNATURE" defined on line 12. Methods and field names should not be the same or differ only by capitalization (java:S1845)

	ExepackHeader(@NonNull final byte[] bytes) throws InvalidExepackHeaderException {
		if (bytes.length != SIZE) {
			throw new IllegalArgumentException("Invalid byte array size; expected: " + SIZE + " but was: " + bytes.length);
		}
		final ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		realIp = Short.toUnsignedInt(buf.getShort(0));
		realCs = Short.toUnsignedInt(buf.getShort(2));
		memStart = Short.toUnsignedInt(buf.getShort(4));
		exepackSize = Short.toUnsignedInt(buf.getShort(6));
		realSp = Short.toUnsignedInt(buf.getShort(8));
		realSs = Short.toUnsignedInt(buf.getShort(10));
		destLen = Short.toUnsignedInt(buf.getShort(12));
		final int word8 = Short.toUnsignedInt(buf.getShort(14));
		final int word9 = Short.toUnsignedInt(buf.getShort(16));
		if ((word9 != SIGNATURE && word8 != SIGNATURE) || exepackSize == 0x00) {
			throw new InvalidExepackHeaderException(bytes);
		}
		if (word8 == SIGNATURE && word9 != SIGNATURE) {
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
