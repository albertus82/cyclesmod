package it.albertus.cycles.model;

import it.albertus.cycles.resources.Messages;
import it.albertus.util.ByteUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	
	private final Bike[] bikes = new Bike[3];
	
	public BikesInf( final InputStream bikesInfInputStream ) throws IOException {
		read( bikesInfInputStream );
	}
	
	public BikesInf( String fileName ) throws IOException {
		read( new BufferedInputStream( new FileInputStream( fileName ) ) );
	}
	
	private void read( final InputStream inf ) throws IOException {
		byte[] inf125 = new byte[ Bike.LENGTH ];
		byte[] inf250 = new byte[ Bike.LENGTH ];
		byte[] inf500 = new byte[ Bike.LENGTH ];
		
		if ( inf.read( inf125 ) != Bike.LENGTH || inf.read( inf250 ) != Bike.LENGTH || inf.read( inf500 ) != Bike.LENGTH || inf.read() != -1 ) {
			inf.close();
			throw new IllegalStateException( Messages.get( "err.wrong.file.size" ) );
		}
		inf.close();
		log.info( Messages.get( "msg.original.file.read", FILE_NAME ) );
		
		bikes[0] = new Bike( Bike.Type.CLASS_125, inf125 );
		bikes[1] = new Bike( Bike.Type.CLASS_250, inf250 );
		bikes[2] = new Bike( Bike.Type.CLASS_500, inf500 );
		log.info( Messages.get( "msg.original.file.parsed", FILE_NAME ) );
	}
	
	public void write( final String fileName ) throws IOException {
		byte[] newBikesInf = this.toByteArray();
		Checksum crc = new CRC32();
		crc.update( newBikesInf, 0, newBikesInf.length );
		log.info( Messages.get( "msg.configuration.changed", ( crc.getValue() == FILE_CRC ? ' ' + Messages.get( "msg.not" ) + ' ' : ' ' ), String.format( "%X", crc.getValue() ) ) );
	
		BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( fileName ), FILE_SIZE );
		write( bos, newBikesInf );
		log.info( Messages.get( "msg.new.file.written.into.path", FILE_NAME, "".equals( fileName ) ? '.' : fileName, String.format( "%X", crc.getValue() ) ) );
	}

	private void write(OutputStream outputStream, byte[] bikesInf) throws IOException {
		if (bikesInf == null || outputStream == null) {
			throw new IllegalArgumentException();
		}
		outputStream.write(bikesInf);
		outputStream.flush();
		outputStream.close();
	}
	
	/**
	 * Ricostruisce il file BIKES.INF a partire dalle 3 configurazioni contenute nell'oggetto (125, 250, 500).
	 * 
	 * @return L'array di byte corrispondente al file BIKES.INF.
	 */
	private byte[] toByteArray() {
		List<Byte> byteList = new ArrayList<Byte>( FILE_SIZE );
		for ( Bike bike : bikes ) {
			byteList.addAll( bike.toByteList() );
		}
		if ( byteList.size() != FILE_SIZE ) {
			throw new IllegalStateException( Messages.get( "err.wrong.file.size.detailed", FILE_NAME, FILE_SIZE, byteList.size() ) );
		}
		return ByteUtils.toByteArray( byteList );
	}

	public Bike getBike( int displacement ) {
		for ( Bike bike : bikes ) {
			if ( bike.getType().getDisplacement() == displacement ) {
				return bike;
			}
		}
		return null;
	}
	
	public Bike[] getBikes() {
		return bikes;
	}
	
}