package it.albertus.unexepack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DosHeader {

	static final int SIGNATURE = 0x5A4D;
	static final int SIZE = 14 * Short.BYTES; // bytes

	private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

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

	DosHeader(@NonNull final byte[] bytes) throws InvalidDosHeaderException {
		if (bytes.length != SIZE) {
			throw new IllegalArgumentException("Invalid byte array size; expected: " + SIZE + " but was: " + bytes.length);
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
			throw new InvalidDosHeaderException(bytes);
		}
	}

	private boolean test() {
		if (eMagic != SIGNATURE) {
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

	byte[] toByteArray() {
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
