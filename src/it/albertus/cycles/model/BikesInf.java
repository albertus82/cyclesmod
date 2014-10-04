package it.albertus.cycles.model;

import it.albertus.cycles.resources.Messages;
import it.albertus.util.ByteUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BikesInf {

	private static final Logger log = LoggerFactory.getLogger( BikesInf.class );
	
	public static final String FILE_NAME = "BIKES.INF";
	public static final int FILE_CRC = 0x28A33682;
	public static final short FILE_SIZE = 444;
	
	private Bike bike125;
	private Bike bike250;
	private Bike bike500;
	
	public BikesInf( final InputStream originalBikesInfInputStream ) throws IOException {
		read( originalBikesInfInputStream );
	}
	
	private void read( final InputStream inf ) throws IOException {
		byte[] inf125 = new byte[ Bike.LENGTH ];
		byte[] inf250 = new byte[ Bike.LENGTH ];
		byte[] inf500 = new byte[ Bike.LENGTH ];
		inf.read( inf125 );
		inf.read( inf250 );
		inf.read( inf500 );
		inf.close();
		log.info( Messages.get( "msg.original.file.read", FILE_NAME ) );
		
		this.bike125 = new Bike( inf125 );
		this.bike250 = new Bike( inf250 );
		this.bike500 = new Bike( inf500 );
		log.info( Messages.get( "msg.original.file.parsed", FILE_NAME ) );
	}
	
	public void write( final String destinationPath ) throws IOException {
		byte[] newBikesInf = this.toByteArray();
		Checksum crc = new CRC32();
		crc.update( newBikesInf, 0, newBikesInf.length );
		log.info( Messages.get( "msg.configuration.changed", ( crc.getValue() == FILE_CRC ? ' ' + Messages.get( "msg.not" ) + ' ' : ' ' ), String.format( "%X", crc.getValue() ) ) );
	
		BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( destinationPath + FILE_NAME ), FILE_SIZE );
		bos.write( newBikesInf );
		bos.flush();
		bos.close();
		log.info( Messages.get( "msg.new.file.written.into.path", FILE_NAME, "".equals( destinationPath ) ? '.' : destinationPath, String.format( "%X", crc.getValue() ) ) );
	}
	
	/**
	 * Ricostruisce il file BIKES.INF a partire dalle 3 configurazioni contenute nell'oggetto (125, 250, 500).
	 * 
	 * @return L'array di byte corrispondente al file BIKES.INF.
	 */
	private byte[] toByteArray() {
		List<Byte> byteList = new ArrayList<Byte>( FILE_SIZE );
		byteList.addAll( bike125.toByteList() );
		byteList.addAll( bike250.toByteList() );
		byteList.addAll( bike500.toByteList() );
		if ( byteList.size() != FILE_SIZE ) {
			throw new IllegalStateException( Messages.get( "err.wrong.file.size", FILE_NAME, FILE_SIZE, byteList.size() ) );
		}
		return ByteUtils.toByteArray( byteList );
	}
	
	public Bike getBike125() {
		return bike125;
	}
	public Bike getBike250() {
		return bike250;
	}
	public Bike getBike500() {
		return bike500;
	}
	
}