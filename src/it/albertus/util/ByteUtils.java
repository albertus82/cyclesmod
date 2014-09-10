package it.albertus.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ByteUtils {
	
	public static byte[] toByteArray( List<Byte> byteList ) {
		byte[] byteArray = new byte[ byteList.size() ];
		for ( int i = 0; i < byteArray.length; i++ ) {
			byteArray[i] = byteList.get(i).byteValue();
		}
		return byteArray;
	}
	
	public static byte[] toByteArray( int intValue ) {
		byte[] byteArray = new byte[2];
		byteArray[0] = (byte)intValue;
		byteArray[1] = (byte)(intValue >>> 8);
		return byteArray;
	}
	
	public static List<Byte> toByteList( int intValue ) {
		List<Byte> byteList = new ArrayList<Byte>( 2 );
		byteList.add( (byte)intValue );
		byteList.add( (byte)(intValue >>> 8) );
		return byteList;
	}

	public static short[] toShortArray( byte[] byteArray ) {
		short[] shortArray = new short[ byteArray.length ];
		for ( int i = 0; i < byteArray.length; i++ ) {
			shortArray[i] = toShort( byteArray[i] );
		}
		return shortArray;
	}

	public static int[] toIntArray( byte[] byteArray ) {
		if ( byteArray.length % 2 != 0 ) {
			throw new IllegalArgumentException( "Cannot convert an odd sized byte array into an int array." );
		}
		int[] intArray = new int[ byteArray.length / 2 ];
		for ( int i = 0; i < byteArray.length; i += 2 ) {
			intArray[ i / 2 ] = toInt( byteArray[i], byteArray[i + 1] );
		}
		return intArray;
	}
	
	public static short toShort( byte byteValue ) {
		return toShort( byteValue, ByteOrder.LITTLE_ENDIAN );
	}
	
	public static short toShort( byte byteValue, ByteOrder byteOrder ) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2);
		byteBuffer.order( byteOrder );
	    byteBuffer.put( byteValue );
	    byteBuffer.put( (byte)0x00 );
	    byteBuffer.flip();
	    return byteBuffer.getShort();
	}

	public static int toInt( byte lowOrderByte, byte highOrderByte ) {
		return toInt( lowOrderByte, highOrderByte, ByteOrder.LITTLE_ENDIAN );
	}
	
	public static int toInt( byte lowOrderByte, byte highOrderByte, ByteOrder byteOrder ) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
		byteBuffer.order( byteOrder );
	    byteBuffer.put( lowOrderByte );
	    byteBuffer.put( highOrderByte );
	    byteBuffer.put( (byte)0x00 );
	    byteBuffer.put( (byte)0x00 );
	    byteBuffer.flip();
	    return byteBuffer.getInt();
	}

}