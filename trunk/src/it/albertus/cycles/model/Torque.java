package it.albertus.cycles.model;

import java.util.ArrayList;
import java.util.List;

public class Torque extends BikesInfElement {
	
	protected static final int LENGTH = 106;
	
	private short[] curve = new short[ LENGTH ]; // 42-147: curva di coppia (regime massimo considerato: ~14500 RPM).

	public Torque(short[] curve) {
		if ( curve.length > LENGTH ) {
			throw new IllegalArgumentException( "The torque curve cannot accept more than " + LENGTH + " points. Number of values provided: " + curve.length + '.' );
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

}