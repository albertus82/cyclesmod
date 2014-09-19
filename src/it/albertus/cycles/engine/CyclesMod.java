package it.albertus.cycles.engine;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

import java.beans.Introspector;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclesMod {
	
	private static final Logger log = LoggerFactory.getLogger( CyclesMod.class );
	
	private static final String VERSION_NUMBER = "1.0.0";
	private static final String BUILD_DATE = "2014-09-19";
	
	private static final String CFG_FILE_NAME = "BIKES.CFG";
	
	private static final String ZIP_FILE_PATH = '/' + StringUtils.substringBeforeLast( CyclesMod.class.getPackage().getName(), "." ).replace( '.', '/' ) + "/data/";
	private static final String ZIP_FILE_NAME = "bikes.zip";
	
	private static final String DEFAULT_DESTINATION_PATH = "";
	private static final String GETTER_PREFIX = "get";
	
	private BikesInf bikesInf;
	private final Properties properties = new Properties();
	private final String path;
	private short changesCount = 0;

	public CyclesMod( String path ) {
		this.path = path;
	}

	public static void main( final String... args ) throws Exception {
		try {
			log.info( Messages.get( "msg.welcome", VERSION_NUMBER, BUILD_DATE ) );
			if ( args.length > 1 ) {
				throw new IllegalArgumentException( Messages.get( "err.command.line.help" ) );
			}
			String path = args.length > 0 ? args[0] : DEFAULT_DESTINATION_PATH;
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
				throw e;
			}
		}
	}
	
	private void execute() throws Exception {
		log.info( Messages.get( "msg.reading.original.file" , BikesInf.FILE_NAME ) );
		readOriginalBikesInf();
		
		log.info( Messages.get( "msg.applying.customizations" ) );
		customize();
		
		log.info( Messages.get( "msg.preparing.new.file", BikesInf.FILE_NAME ) );
		writeCustomBikesInf();
	}
	
	private void writeCustomBikesInf() throws IOException {
		byte[] newBikesInf = bikesInf.toByteArray();
		Checksum crc = new CRC32();
		crc.update( newBikesInf, 0, newBikesInf.length );
		
		log.info( Messages.get( "msg.configuration.changed", ( crc.getValue() == BikesInf.FILE_CRC ? ' ' + Messages.get( "msg.not" ) + ' ' : ' ' ), String.format( "%X", crc.getValue() ) ) );

		BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( path + BikesInf.FILE_NAME ), BikesInf.FILE_SIZE );
		bos.write( newBikesInf );
		bos.flush();
		bos.close();
		log.info( Messages.get( "msg.new.file.written.into.path", BikesInf.FILE_NAME, "".equals( path ) ? '.' : path, String.format( "%X", crc.getValue() ) ) );
	}
	
	private void readOriginalBikesInf() throws IOException {
		log.info( Messages.get( "msg.opening.file", BikesInf.FILE_NAME ) );
		InputStream is = getBikesInfInputStream();
		
		byte[] inf125 = new byte[ Bike.LENGTH ];
		byte[] inf250 = new byte[ Bike.LENGTH ];
		byte[] inf500 = new byte[ Bike.LENGTH ];
		is.read( inf125 );
		is.read( inf250 );
		is.read( inf500 );
		is.close();
		log.info( Messages.get( "msg.original.file.read", BikesInf.FILE_NAME ) );
		
		Bike bike125 = new Bike( inf125 );
		Bike bike250 = new Bike( inf250 );
		Bike bike500 = new Bike( inf500 );
		bikesInf = new BikesInf( bike125, bike250, bike500 );
		log.info( Messages.get( "msg.original.file.parsed", BikesInf.FILE_NAME ) );
	}
	
	private ZipInputStream getBikesInfInputStream() throws IOException {
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream( getClass().getResourceAsStream( ZIP_FILE_PATH + ZIP_FILE_NAME ) );
		}
		catch ( Exception e ) {
			throw new FileNotFoundException( Messages.get( "msg.file.not.found", ZIP_FILE_PATH + ZIP_FILE_NAME ) );
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
	
	private void customize() throws Exception {
		// Lettura del file di properties BIKES.CFG...
		log.info( Messages.get( "msg.reading.file", CFG_FILE_NAME ) );
		BufferedReader br = null;
		try {
			br = new BufferedReader( new FileReader( path + CFG_FILE_NAME ) );
		}
		catch ( FileNotFoundException fnfe ) {
			log.info( Messages.get( "msg.file.not.found.creating.default", CFG_FILE_NAME ) );
			writeDefaultBikesCfg();
			log.info( Messages.get( "msg.default.file.created", CFG_FILE_NAME ) );
			br = new BufferedReader( new FileReader( path + CFG_FILE_NAME ) );
		}
		properties.load( br );
		br.close();
		log.info( Messages.get( "msg.file.read", CFG_FILE_NAME ) );
		
		// Elaborazione delle properties...
		for ( Object objectKey : properties.keySet() ) {
			String key = (String)objectKey;
			if ( !StringUtils.isNumeric( properties.getProperty( key ) ) ) {
				throw new PropertyException( Messages.get( "err.unsupported.property", key, properties.getProperty( key ) ) );
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
				throw new PropertyException( Messages.get( "err.unsupported.property", key, properties.getProperty( key ) ) );
			}
		}
		log.info( Messages.get( "msg.customizations.applied", changesCount ) );
	}

	private void writeDefaultBikesCfg() throws Exception {
        final String lineSeparator = java.security.AccessController.doPrivileged( new sun.security.action.GetPropertyAction( "line.separator" ) );
		final StringBuilder properties = new StringBuilder( Messages.get( "str.cfg.header" ) );
		
		for ( Bike.Type type : Bike.Type.values() ) {
			String prefix = Integer.toString( type.getDisplacement() );
			Bike bike = null;
			for ( Method method : BikesInf.class.getMethods() ) {
				if ( method.getName().startsWith( GETTER_PREFIX ) && method.getName().contains( prefix ) ) {
					bike = (Bike)method.invoke( bikesInf );
				}
			}
			
			if ( bike == null ) {
				throw new IllegalStateException();
			}
			
			properties.append( lineSeparator ).append( lineSeparator );
			properties.append( "### ").append( type.getDisplacement() ).append( " cc - " + Messages.get( "str.cfg.begin" ) + "... ###");
			
			// Settings
			properties.append( lineSeparator );
			properties.append( "# " ).append( Settings.class.getSimpleName() ).append( " #" );
			properties.append( lineSeparator );
			for ( Method method : Settings.class.getMethods() ) {
				if ( method.getName().startsWith( GETTER_PREFIX ) && method.getReturnType() != null && "int".equals( method.getReturnType().getName() ) ) {
					properties.append( prefix ).append( '.' ).append( Introspector.decapitalize( Settings.class.getSimpleName() ) ).append( '.' ).append( Introspector.decapitalize( StringUtils.substringAfter( method.getName(), GETTER_PREFIX ) ) );
					properties.append( '=' );
					properties.append( (Integer)method.invoke( bike.getSettings() ) );
					properties.append( lineSeparator );
				}
			}
			
			// Gearbox
			properties.append( lineSeparator );
			properties.append( "# " ).append( Gearbox.class.getSimpleName() ).append( " #" );
			properties.append( lineSeparator );
			for ( int index = 0; index < bike.getGearbox().getRatios().length; index++ ) {
				properties.append( prefix ).append( '.' ).append( Introspector.decapitalize( Gearbox.class.getSimpleName() ) ).append( '.' ).append( index );
				properties.append( '=' );
				properties.append( bike.getGearbox().getRatios()[ index ] );
				properties.append( lineSeparator );
			}
			
			// Torque
			properties.append( lineSeparator );
			properties.append( "# " ).append( Torque.class.getSimpleName() ).append( " #" );
			properties.append( lineSeparator );
			for ( int index = 0; index < bike.getTorque().getCurve().length; index++ ) {
				properties.append( prefix ).append( '.' ).append( Introspector.decapitalize( Torque.class.getSimpleName() ) ).append( '.' ).append( index );
				properties.append( '=' );
				properties.append( bike.getTorque().getCurve()[ index ] );
				properties.append( lineSeparator );
			}
			
			properties.append( "### ").append( type.getDisplacement() ).append( " cc - " + Messages.get( "str.cfg.end" ) + ". ###");
		}
		
		properties.append( lineSeparator ).append( lineSeparator );
		properties.append( Messages.get( "str.cfg.footer" ) );

		// Salvataggio...
		BufferedWriter bw = new BufferedWriter( new FileWriter( path + CFG_FILE_NAME ) );
		bw.write( properties.toString() );
		bw.flush();
		bw.close();
	}

	private Bike getBike( final String key ) throws Exception {
		Bike bike = null;
		
		for ( Bike.Type type : Bike.Type.values() ) {
			if ( key.startsWith( type.getDisplacement() + "." ) ) {
				for ( Method method : BikesInf.class.getMethods() ) {
					if ( method.getName().startsWith( GETTER_PREFIX ) && method.getName().contains( Integer.toString( type.getDisplacement() ) ) ) {
						bike = (Bike)method.invoke( bikesInf );
						break;
					}
				}
			}
		}
		
		if ( bike == null ) {
			throw new PropertyException( Messages.get( "err.unsupported.property", key, properties.getProperty( key ) ) );
		}
		
		return bike;
	}

	private void parseTorqueProperty( final String key ) throws Exception {
		short newValue = Short.parseShort( properties.getProperty( key ) );
		if ( newValue < Torque.MIN_VALUE || newValue > Torque.MAX_VALUE ) {
			throw new PropertyException( Messages.get( "err.illegal.value", Torque.MIN_VALUE, Torque.MAX_VALUE, key, newValue ) );
		}
		
		Bike bike = getBike( key );
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Torque.class.getSimpleName() ) + '.' );
		if ( StringUtils.isNotEmpty( suffix ) && StringUtils.isNumeric( suffix ) && Integer.parseInt( suffix ) < bike.getTorque().getCurve().length ) {
			int index = Integer.parseInt( suffix );
			int defaultValue = bike.getTorque().getCurve()[ index ];
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
		int newValue = Integer.parseInt( properties.getProperty( key ) );
		if ( newValue < Gearbox.MIN_VALUE || newValue > Gearbox.MAX_VALUE ) {
			throw new PropertyException( Messages.get( "err.illegal.value", Gearbox.MIN_VALUE, Gearbox.MAX_VALUE, key, newValue ) );
		}
		
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
		int newValue = Integer.parseInt( properties.getProperty( key ) );
		if ( newValue < Settings.MIN_VALUE || newValue > Settings.MAX_VALUE ) {
			throw new PropertyException( Messages.get( "err.illegal.value", Settings.MIN_VALUE, Settings.MAX_VALUE, key, newValue ) );
		}
		
		Bike bike = getBike( key );
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Settings.class.getSimpleName() ) + '.' );
		Method setter = null;
		Method getter = null;
		for ( Method method : Settings.class.getMethods() ) {
			if ( method.getName().equals( "set" + StringUtils.capitalize( suffix ) ) ) {
				setter = method;
			}
			if ( method.getName().equals( GETTER_PREFIX + StringUtils.capitalize( suffix ) ) ) {
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
		log.info( Messages.get( "msg.custom.value.detected", key, newValue, defaultValue ) );
		changesCount++;
	}
	
}