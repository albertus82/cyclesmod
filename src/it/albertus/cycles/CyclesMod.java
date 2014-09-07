package it.albertus.cycles;

import it.albertus.cycles.model.Bike;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclesMod {
	
	private static final String DEFAULT_DESTINATION_PATH = "D:\\Documents\\Dropbox\\Personale\\DOS\\CYCLES\\";
	
	private void customizeBikes( FileBikesInf bikesInf ) {
		// Inserire qui le personalizzazioni...
	}
	
	
	private static final String FILE_NAME = "BIKES.INF";
	
	private static final Logger log = LoggerFactory.getLogger( CyclesMod.class );
	
	public static void main( String... args ) throws IOException {
		if ( args.length > 1 ) {
			throw new IllegalArgumentException( "Troppi parametri. Uso: CyclesMod [percorso di destinazione]" );
		}
		String destinationPath = args.length > 0 ? args[0] : DEFAULT_DESTINATION_PATH;
		
		CyclesMod mod = new CyclesMod();
		
		log.info( "Apertura file " + FILE_NAME + " originale..." );
		InputStream is = mod.getBikesInfInputStream();
		
		log.info( "Lettura del file " + FILE_NAME + " originale..." );
		FileBikesInf bikesInf = mod.parseBikesInfInputStream( is );
		
		log.info( "Applicazione delle personalizzazioni alla configurazione..." );
		mod.customizeBikes( bikesInf );
		log.info( "Personalizzazioni applicate." );
		
		// File in uscita
		log.info( "Preparazione nuovo file " + FILE_NAME + "..." );
		mod.writeCustomBikesInf( bikesInf, destinationPath );
	}
	
	private void writeCustomBikesInf( FileBikesInf bikesInf, String destinationPath ) throws IOException {
		FileOutputStream fos = new FileOutputStream( destinationPath + FILE_NAME );
		fos.write( bikesInf.toByteArray() );
		fos.flush();
		fos.close();
		log.info( "Nuovo file " + FILE_NAME + " scritto correttamente nel percorso \"" + destinationPath + "\"." );
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
	
	private InputStream getBikesInfInputStream() throws IOException {
		final int CRC = 0x28A33682;
		final short SIZE = 444;
		
		ZipInputStream zis = new ZipInputStream( CyclesMod.class.getResourceAsStream( "/bikes.zip" ) );
		ZipEntry ze = zis.getNextEntry();
		if ( ze.getCrc() != CRC ) {
			throw new StreamCorruptedException( "Il file " + FILE_NAME + " non corrisponde a quello originale; CRC atteso: " + String.format( "%X", CRC ) + ", CRC rilevato: " + String.format( "%X", ze.getCrc() ) + '.' );
		}
		if ( ze.getSize() != SIZE ) {
			throw new StreamCorruptedException( "Il file " + FILE_NAME + " non corrisponde a quello originale; dimensioni attese: " + SIZE + " byte, dimensioni rilevate: " + ze.getSize() + " byte." );
		}
		log.info( "File " + FILE_NAME + " originale aperto; CRC OK: " + String.format( "%X", ze.getCrc() ) + '.' );
		return zis;
	}
	
}