package it.albertus.cycles.data;

import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Messages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BikesZip {
	
	private static final Logger log = LoggerFactory.getLogger( BikesZip.class );
	
	public static final String FILE_PATH = '/' + BikesZip.class.getPackage().getName().replace( '.', '/' ) + '/';
	public static final String FILE_NAME = "bikes.zip";
	
	private final ZipInputStream inputStream;
	
	public ZipInputStream getInputStream() {
		return inputStream;
	}

	public BikesZip() throws IOException {
		this.inputStream = openBikesInfInputStream();
	}
	
	private ZipInputStream openBikesInfInputStream() throws IOException {
		log.info( Messages.get( "msg.opening.file", BikesInf.FILE_NAME ) );
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream( getClass().getResourceAsStream( FILE_PATH + FILE_NAME ) );
		}
		catch ( Exception e ) {
			throw new FileNotFoundException( Messages.get( "msg.file.not.found", FILE_PATH + FILE_NAME ) );
		}
		ZipEntry ze = zis.getNextEntry();
		if ( ze.getCrc() != BikesInf.FILE_CRC ) {
			throw new StreamCorruptedException( Messages.get( "err.original.file.corrupted.crc", BikesInf.FILE_NAME, String.format( "%X", BikesInf.FILE_CRC ), String.format( "%X", ze.getCrc() ) ) );
		}
		if ( ze.getSize() != BikesInf.FILE_SIZE ) {
			throw new StreamCorruptedException( Messages.get( "err.original.file.corrupted.size", BikesInf.FILE_NAME, BikesInf.FILE_SIZE, ze.getSize() ) );
		}
		log.info( Messages.get( "msg.original.file.opened", BikesInf.FILE_NAME, String.format( "%X", ze.getCrc() ) ) );
		return zis;
	}
	
}