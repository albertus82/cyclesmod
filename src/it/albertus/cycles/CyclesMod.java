package it.albertus.cycles;

import it.albertus.cycles.model.Bike;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclesMod {
	
	private static final String FILE_NAME = "BIKES.INF";
	private static final int CRC = 0x28A33682;
	private static final short SIZE = 444;
	
	private static final Logger log = LoggerFactory.getLogger( CyclesMod.class );
	
	public static void main( String... args ) throws IOException {
		if ( args.length > 1 ) {
			throw new IllegalArgumentException( "Too many parameters. Usage: CyclesMod [destination]" );
		}
		String destinationPath = args.length > 0 ? args[0] : Mod.DESTINATION_PATH;
		
		CyclesMod mod = new CyclesMod();
		
		log.info( "Apertura file " + FILE_NAME + " originale..." );
		InputStream is = mod.getBikesInfInputStream();
		
		log.info( "Lettura del file " + FILE_NAME + " originale..." );
		FileBikesInf bikesInf = mod.parseBikesInfInputStream( is );
		
		log.info( "Applicazione delle personalizzazioni alla configurazione..." );
		Mod.customize( bikesInf );
		log.info( "Personalizzazioni applicate." );
		
		// File in uscita
		log.info( "Preparazione nuovo file " + FILE_NAME + "..." );
		mod.writeCustomBikesInf( bikesInf, destinationPath );
	}
	
	private void writeCustomBikesInf( FileBikesInf bikesInf, String destinationPath ) throws IOException {
		byte[] newBikesInf = bikesInf.toByteArray();
		Checksum crc = new CRC32();
		crc.update( newBikesInf, 0, newBikesInf.length );
		
		log.info( "La configurazione" + ( crc.getValue() == CRC ? " non " : ' ' ) + "e' stata modificata; CRC: " + String.format( "%X", crc.getValue() ) + '.' );

		BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( destinationPath + FILE_NAME ), SIZE );
		bos.write( newBikesInf );
		bos.flush();
		bos.close();
		log.info( "Nuovo file " + FILE_NAME + " scritto correttamente nel percorso \"" + destinationPath + "\"; CRC: " + String.format( "%X", crc.getValue() ) + '.' );
	}
	
	private FileBikesInf parseBikesInfInputStream( InputStream is ) throws IOException {
		byte[] inf125 = new byte[ Bike.LENGTH ];
		byte[] inf250 = new byte[ Bike.LENGTH ];
		byte[] inf500 = new byte[ Bike.LENGTH ];
		is.read( inf125 );
		is.read( inf250 );
		is.read( inf500 );
		is.close();
		log.info( "Lettura del file " + FILE_NAME + " originale completata. File chiuso." );
		
		Bike bike125 = new Bike( inf125 );
		Bike bike250 = new Bike( inf250 );
		Bike bike500 = new Bike( inf500 );
		FileBikesInf bikesInf = new FileBikesInf( bike125, bike250, bike500 );
		log.info( "File " + FILE_NAME + " originale elaborato." );
		return bikesInf;
	}
	
	private ZipInputStream getBikesInfInputStream() throws IOException {
		ZipInputStream zis = new ZipInputStream( getClass().getResourceAsStream( "data/bikes.zip" ) );
		ZipEntry ze = zis.getNextEntry();
		if ( ze.getCrc() != CRC ) {
			throw new StreamCorruptedException( "The original " + FILE_NAME + " file is corrupted; CRC miscompare, expected: " + String.format( "%X", CRC ) + ", actual: " + String.format( "%X", ze.getCrc() ) + '.' );
		}
		if ( ze.getSize() != SIZE ) {
			throw new StreamCorruptedException( "The original " + FILE_NAME + " file is corrupted; file size miscompare, expected: " + SIZE + " bytes, actual: " + ze.getSize() + " bytes." );
		}
		log.info( "File " + FILE_NAME + " originale aperto; CRC OK: " + String.format( "%X", ze.getCrc() ) + '.' );
		return zis;
	}
	
}