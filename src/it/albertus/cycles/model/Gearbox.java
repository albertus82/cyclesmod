package it.albertus.cycles.model;

import it.albertus.cycles.engine.PropertyException;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

public class Gearbox extends BikesInfElement {
	
	public static final int LENGTH = 20;
	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 65535;
	
	private final int[] ratios = new int[ LENGTH / 2 ]; // 22-41 (N, 1, 2, 3, 4, 5, 6, 7, 8, 9)
	
	public Gearbox( final int[] ratios ) {
		if ( ratios.length > LENGTH / 2 ) {
			throw new IllegalArgumentException( Messages.get( "err.gearbox", LENGTH / 2, ratios.length ) );
		}
		for ( int i = 0; i < ratios.length; i++ ) {
			this.ratios[i] = ratios[i];
		}
	}
	
	@Override
	public List<Byte> toByteList() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		for ( int ratio : ratios ) {
			byteList.addAll( ByteUtils.toByteList( ratio ) );
		}
		return byteList;
	}
	
	public static int parse( final String key, final String value ) {
		long newValue = Long.parseLong( value );
		if ( newValue < MIN_VALUE || newValue > MAX_VALUE ) {
			throw new PropertyException( Messages.get( "err.illegal.value", MIN_VALUE, MAX_VALUE, key, newValue ) );
		}
		return (int)newValue;
	}
	
	public int[] getRatios() {
		return ratios;
	}
	
}