package it.albertus.cycles.model;

import java.util.ArrayList;
import java.util.List;

public class Torque {
	
	protected static final int LENGTH = 112;
	
	private short[] curve; // 36-147

	public Torque(short[] curve) {
		super();
		this.setCurve(curve);
	}
	
	public List<Byte> toByteList() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		for ( short pt : curve ) {
			byteList.add( (byte)pt );
		}
		return byteList;
	}

	public short[] getCurve() {
		return curve;
	}
	public void setCurve(short[] curve) {
		this.curve = curve;
	}

}