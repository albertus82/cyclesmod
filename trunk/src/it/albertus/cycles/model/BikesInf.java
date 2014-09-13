package it.albertus.cycles.model;

import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

public class BikesInf {

	private static final int LENGTH = 3 * Bike.LENGTH;
	
	private Bike bike125;
	private Bike bike250;
	private Bike bike500;
	
	public BikesInf(Bike bike125, Bike bike250, Bike bike500) {
		super();
		this.bike125 = bike125;
		this.bike250 = bike250;
		this.bike500 = bike500;
	}
	
	/**
	 * Ricostruisce il file BIKES.INF a partire dalle 3 configurazioni contenute nell'oggetto (125, 250, 500).
	 * 
	 * @return L'array di byte corrispondente al file BIKES.INF.
	 */
	public byte[] toByteArray() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		byteList.addAll( bike125.toByteList() );
		byteList.addAll( bike250.toByteList() );
		byteList.addAll( bike500.toByteList() );
		if ( byteList.size() != LENGTH ) {
			throw new IllegalStateException( "Wrong BIKES.INF file size, actual: " + byteList.size() + " bytes, expected: " + LENGTH + " bytes." );
		}
		return ByteUtils.toByteArray( byteList );
	}
	
	public Bike getBike125() {
		return bike125;
	}
	public void setBike125(Bike bike125) {
		this.bike125 = bike125;
	}
	public Bike getBike250() {
		return bike250;
	}
	public void setBike250(Bike bike250) {
		this.bike250 = bike250;
	}
	public Bike getBike500() {
		return bike500;
	}
	public void setBike500(Bike bike500) {
		this.bike500 = bike500;
	}
	
}