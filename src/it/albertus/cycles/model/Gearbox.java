package it.albertus.cycles.model;

import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

public class Gearbox extends BikesInfElement {
	
	public static final int LENGTH = 20;
	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 65535;
	
	private final int[] ratios = new int[ LENGTH / 2 ]; // 22-41 (N, 1, 2, 3, 4, 5, 6, 7, 8, 9)
	
	public Gearbox( int[] ratios ) {
		if ( ratios.length > LENGTH / 2 ) {
			throw new IllegalArgumentException( "Il cambio non puo' avere piu' di " + ( LENGTH / 2 ) + " rapporti, incluso folle. Numero rapporti forniti: " + ratios.length + '.' );
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

	public int[] getRatios() {
		return ratios;
	}
	
}