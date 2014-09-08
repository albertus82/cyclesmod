package it.albertus.cycles.model;

import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

public class Gearbox extends BikesInfElement {
	
	protected static final int LENGTH = 20;
	
	private final int[] gearRatios = new int[ LENGTH / 2 ]; // 22-41 (N, 1, 2, 3, 4, 5, 6, 7, 8, 9)
	
	public Gearbox( int[] gearRatios ) {
		if ( gearRatios.length > LENGTH / 2 ) {
			throw new IllegalArgumentException( "Il cambio non puo' avere piu' di " + ( LENGTH / 2 ) + " rapporti, incluso folle. Numero rapporti forniti: " + gearRatios.length + '.' );
		}
		for ( int i = 0; i < gearRatios.length; i++ ) {
			this.gearRatios[i] = gearRatios[i];
		}
	}

	@Override
	public List<Byte> toByteList() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		for ( int ratio : gearRatios ) {
			byteList.addAll( ByteUtils.toByteList( ratio ) );
		}
		return byteList;
	}

	public int[] getGearRatios() {
		return gearRatios;
	}
	
}