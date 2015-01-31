package it.albertus.cycles.engine;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Messages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclesMod extends PropertyParser {
	
	private static final Logger log = LoggerFactory.getLogger( CyclesMod.class );
	
	private static final String VERSION_FILE_PATH = "/";
	private static final String VERSION_FILE_NAME = "version.properties";
	
	private static final String DEFAULT_DESTINATION_PATH = "";
	
	private BikesCfg bikesCfg;
	private final String path;
	
	public CyclesMod( String path ) {
		this.path = path;
	}
	
	public static void main( final String... args ) throws Exception {
		try {
			log.info( getWelcomeMessage() );
			
			// Gestione parametri da riga di comando...
			if ( args.length > 1 ) {
				throw new IllegalArgumentException( Messages.get( "err.too.many.parameters" ) + ' ' + Messages.get( "msg.command.line.help", CyclesMod.class.getSimpleName() ) );
			}
			String path = args.length == 1 ? args[0] : DEFAULT_DESTINATION_PATH;
			
			if ( path.contains( "?" ) || StringUtils.startsWithIgnoreCase( path, "-help" ) || StringUtils.startsWithIgnoreCase( path, "/help" ) ) {
				log.info( Messages.get( "msg.command.line.help", CyclesMod.class.getSimpleName() ) );
				return;
			}
			if ( !"".equals( path ) && !path.endsWith( "/" ) && !path.endsWith( "\\" ) && !path.endsWith( File.separator ) ) {
				path += File.separator;
			}
			
			new CyclesMod( path ).execute();
		}
		catch ( Exception e ) {
			if ( StringUtils.isNotEmpty( e.getLocalizedMessage() ) || StringUtils.isNotEmpty( e.getMessage() ) ) {
				log.error( e.getClass().getSimpleName() + ": " + ( StringUtils.isNotEmpty( e.getLocalizedMessage() ) ? e.getLocalizedMessage() : e.getMessage() ) );
			}
			else {
				throw e; // Le eccezioni prive di messaggio vengono semplicemente rilanciate.
			}
		}
	}

	private static String getWelcomeMessage() throws IOException {
		Properties version = new Properties();
		InputStream is = CyclesMod.class.getResourceAsStream( VERSION_FILE_PATH + VERSION_FILE_NAME );
		version.load( is );
		is.close();
		return Messages.get( "msg.welcome", version.get( "version.number" ), version.get( "version.date" ) ) + "\r\n";
	}
	
	private void execute() throws IOException {
		log.info( Messages.get( "msg.reading.original.file", BikesInf.FILE_NAME ) );
		bikesInf = new BikesInf( new BikesZip().getInputStream() );
		
		log.info( Messages.get( "msg.applying.customizations" ) );
		customize();
		
		log.info( Messages.get( "msg.preparing.new.file", BikesInf.FILE_NAME ) );
		bikesInf.write( path + BikesInf.FILE_NAME );
	}
	
	private void customize() throws IOException {
		// Lettura del file di properties BIKES.CFG...
		bikesCfg = new BikesCfg( bikesInf, path );
		
		// Elaborazione delle properties...
		short changesCount = 0;
		for ( Object objectKey : bikesCfg.getProperties().keySet() ) {
			String key = (String) objectKey;
			if ( applyProperty( key, bikesCfg.getProperties().getProperty( key ) ) ) {
				changesCount++;
			}
		}
		log.info( Messages.get( "msg.customizations.applied", changesCount ) );
	}
	
}