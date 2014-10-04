package it.albertus.cycles.engine;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.BeanUtils;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclesMod {
	
	private static final Logger log = LoggerFactory.getLogger( CyclesMod.class );
	
	private static final String VERSION_FILE_PATH = "/";
	private static final String VERSION_FILE_NAME = "version.properties";
	
	private static final String DEFAULT_DESTINATION_PATH = "";
	
	private BikesInf bikesInf;
	private BikesCfg bikesCfg;
	private final String path;
	private short changesCount = 0;
	
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
				log.error( e.getClass().getName() + ": " + ( StringUtils.isNotEmpty( e.getLocalizedMessage() ) ? e.getLocalizedMessage() : e.getMessage() ) );
			}
			else {
				throw e; // Le eccezioni prive di messaggio vengono semplicemente rilanciate.
			}
		}
	}

	private static String getWelcomeMessage() throws IOException {
		InputStream is = CyclesMod.class.getResourceAsStream( VERSION_FILE_PATH + VERSION_FILE_NAME );
		Properties version = new Properties();
		version.load( is );
		is.close();
		return Messages.get( "msg.welcome", version.get( "version.number" ), version.get( "version.date" ) );
	}
	
	private void execute() throws Exception {
		log.info( Messages.get( "msg.reading.original.file", BikesInf.FILE_NAME ) );
		bikesInf = new BikesInf( new BikesZip().getInputStream() );
		
		log.info( Messages.get( "msg.applying.customizations" ) );
		customize();
		
		log.info( Messages.get( "msg.preparing.new.file", BikesInf.FILE_NAME ) );
		bikesInf.write( path );
	}
	
	private void customize() throws Exception {
		// Lettura del file di properties BIKES.CFG...
		bikesCfg = new BikesCfg( bikesInf, path );
		
		// Elaborazione delle properties...
		for ( Object objectKey : bikesCfg.getProperties().keySet() ) {
			String key = (String)objectKey;
			String value = bikesCfg.getProperties().getProperty( key );
			
			if ( !StringUtils.isNumeric( value ) ) {
				throw new PropertyException( Messages.get( "err.unsupported.property", key, value ) );
			}
			
			// Settings
			if ( StringUtils.substringAfter( key, "." ).startsWith( Introspector.decapitalize( Settings.class.getSimpleName() ) ) ) {
				parseSettingProperty( key );
			}
			
			// Gearbox
			else if ( StringUtils.substringAfter( key, "." ).startsWith( Introspector.decapitalize( Gearbox.class.getSimpleName() ) ) ) {
				parseGearboxProperty( key );
			}
			
			// Torque
			else if ( StringUtils.substringAfter( key, "." ).startsWith( Introspector.decapitalize( Torque.class.getSimpleName() ) ) ) {
				parseTorqueProperty( key );
			}
			
			else {
				throw new PropertyException( Messages.get( "err.unsupported.property", key, value ) );
			}
		}
		log.info( Messages.get( "msg.customizations.applied", changesCount ) );
	}
	
	private Bike getBike( final String key ) throws Exception {
		Bike bike = bikesInf.getBike( Integer.parseInt( StringUtils.substringBefore( key, "." ) ) );
		
		if ( bike == null ) {
			Properties properties = bikesCfg.getProperties();
			throw new PropertyException( Messages.get( "err.unsupported.property", key, properties.getProperty( key ) ) );
		}
		
		return bike;
	}
	
	private void parseTorqueProperty( final String key ) throws Exception {
		Properties properties = bikesCfg.getProperties();
		short newValue = Torque.parse( key, properties.getProperty( key ) );
		
		Bike bike = getBike( key );
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Torque.class.getSimpleName() ) + '.' );
		if ( StringUtils.isNotEmpty( suffix ) && StringUtils.isNumeric( suffix ) && Integer.parseInt( suffix ) < bike.getTorque().getCurve().length ) {
			int index = Integer.parseInt( suffix );
			short defaultValue = bike.getTorque().getCurve()[ index ];
			if ( defaultValue != newValue ) {
				bike.getTorque().getCurve()[ index ] = newValue;
				logChange( key, defaultValue, newValue );
			}
		}
		else {
			throw new PropertyException( Messages.get( "err.unsupported.property", key, properties.getProperty( key ) ) );
		}
	}
	
	private void parseGearboxProperty( final String key ) throws Exception {
		Properties properties = bikesCfg.getProperties();
		int newValue = Gearbox.parse( key, properties.getProperty( key ) );
		
		Bike bike = getBike( key );
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Gearbox.class.getSimpleName() ) + '.' );
		if ( StringUtils.isNotEmpty( suffix ) && StringUtils.isNumeric( suffix ) && Integer.parseInt( suffix ) < bike.getGearbox().getRatios().length ) {
			int index = Integer.parseInt( suffix );
			int defaultValue = bike.getGearbox().getRatios()[ index ];
			if ( defaultValue != newValue ) {
				bike.getGearbox().getRatios()[ index ] = newValue;
				logChange( key, defaultValue, newValue );
			}
		}
		else {
			throw new PropertyException( Messages.get( "err.unsupported.property", key, properties.getProperty( key ) ) );
		}
	}
	
	private void parseSettingProperty( final String key ) throws Exception {
		Properties properties = bikesCfg.getProperties();
		int newValue = Settings.parse( key, properties.getProperty( key ) );
		
		Bike bike = getBike( key );
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Settings.class.getSimpleName() ) + '.' );
		Method setter = null;
		Method getter = null;
		for ( Method method : Settings.class.getMethods() ) {
			if ( method.getName().equals( BeanUtils.SETTER_PREFIX + StringUtils.capitalize( suffix ) ) ) {
				setter = method;
			}
			if ( method.getName().equals( BeanUtils.GETTER_PREFIX + StringUtils.capitalize( suffix ) ) ) {
				getter = method;
			}
		}
		if ( setter != null && getter != null ) {
			int defaultValue = (Integer)getter.invoke( bike.getSettings() );
			if ( newValue != defaultValue ) {
				setter.invoke( bike.getSettings(), newValue );
				logChange( key, defaultValue, newValue );
			}
		}
		else {
			throw new PropertyException( Messages.get( "err.unsupported.property", key, properties.getProperty( key ) ) );
		}
	}
	
	private void logChange( final String key, final int defaultValue, final int newValue ) {
		log.info( Messages.get( "msg.custom.value.detected", key, newValue, String.format( "%X", newValue ), defaultValue, String.format( "%X", defaultValue ) ) );
		changesCount++;
	}
	
}