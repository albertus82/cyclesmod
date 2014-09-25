package it.albertus.cycles.model;

import it.albertus.cycles.resources.Messages;

import java.util.ArrayList;
import java.util.List;

public class Torque extends BikesInfElement {
	
	public static final int LENGTH = 106;
	public static final short MIN_VALUE = 0;
	public static final short MAX_VALUE = 255;
	public static final short BASE_RPM = 768;
	public static final short POINT_WIDTH_RPM = 128;
	
	private short[] curve = new short[ LENGTH ]; // 42-147: curva di coppia (intervallo regime considerato: 768-14335 RPM).
	
	public Torque(short[] curve) {
		if ( curve.length > LENGTH ) {
			throw new IllegalArgumentException( Messages.get( "err.torque", LENGTH, curve.length ) );
		}
		for ( int i = 0; i < curve.length; i++ ) {
			this.curve[i] = curve[i];
		}
	}
	
	@Override
	public List<Byte> toByteList() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		for ( short point : curve ) {
			byteList.add( (byte)point );
		}
		return byteList;
	}
	
	public short[] getCurve() {
		return curve;
	}
	
	public static int getRpm( int index ) {
		return BASE_RPM + POINT_WIDTH_RPM * index;
	}
	
}