package it.albertus.cycles.engine;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;

import java.beans.Introspector;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
	
	private static final String CFG_FILE_NAME = "BIKES.CFG";
	private static final String INF_FILE_NAME = "BIKES.INF";
	private static final int INF_FILE_CRC = 0x28A33682;
	private static final short INF_FILE_SIZE = 444;
	private static final String DEFAULT_DESTINATION_PATH = "";
	private static final String GETTER_PREFIX = "get";
	
	private BikesInf bikesInf;
	private final Properties properties = new Properties();
	private final String path;
	private short changesCount = 0;

	public CyclesMod( String path ) {
		this.path = path;
	}

	public static void main( String... args ) throws Exception {
		if ( args.length > 1 ) {
			throw new IllegalArgumentException( "Too many parameters. Usage: CyclesMod [path]" );
		}
		String path = args.length > 0 ? args[0] : DEFAULT_DESTINATION_PATH;
		
		new CyclesMod( path ).execute();
	}
	
	private void execute() throws Exception {
		log.info( "Lettura del file " + INF_FILE_NAME + " originale..." );
		readOriginalBikesInf();
		
		log.info( "Applicazione delle personalizzazioni alla configurazione..." );
		customize();
		
		log.info( "Preparazione nuovo file " + INF_FILE_NAME + "..." );
		writeCustomBikesInf();
	}
	
	private void writeCustomBikesInf() throws IOException {
		byte[] newBikesInf = bikesInf.toByteArray();
		Checksum crc = new CRC32();
		crc.update( newBikesInf, 0, newBikesInf.length );
		
		log.info( "La configurazione" + ( crc.getValue() == INF_FILE_CRC ? " non " : ' ' ) + "e' stata modificata; CRC: " + String.format( "%X", crc.getValue() ) + '.' );

		BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( path + INF_FILE_NAME ), INF_FILE_SIZE );
		bos.write( newBikesInf );
		bos.flush();
		bos.close();
		log.info( "Nuovo file " + INF_FILE_NAME + " scritto correttamente nel percorso \"" + path + "\"; CRC: " + String.format( "%X", crc.getValue() ) + '.' );
	}
	
	private void readOriginalBikesInf() throws IOException {
		log.info( "Apertura file " + INF_FILE_NAME + "..." );
		InputStream is = getBikesInfInputStream();
		
		byte[] inf125 = new byte[ Bike.LENGTH ];
		byte[] inf250 = new byte[ Bike.LENGTH ];
		byte[] inf500 = new byte[ Bike.LENGTH ];
		is.read( inf125 );
		is.read( inf250 );
		is.read( inf500 );
		is.close();
		log.info( "Lettura del file " + INF_FILE_NAME + " originale completata. File chiuso." );
		
		Bike bike125 = new Bike( inf125 );
		Bike bike250 = new Bike( inf250 );
		Bike bike500 = new Bike( inf500 );
		bikesInf = new BikesInf( bike125, bike250, bike500 );
		log.info( "File " + INF_FILE_NAME + " originale elaborato." );
	}
	
	private ZipInputStream getBikesInfInputStream() throws IOException {
		ZipInputStream zis = new ZipInputStream( getClass().getResourceAsStream( "../data/bikes.zip" ) );
		ZipEntry ze = zis.getNextEntry();
		if ( ze.getCrc() != INF_FILE_CRC ) {
			throw new StreamCorruptedException( "The original " + INF_FILE_NAME + " file is corrupted; CRC miscompare, expected: " + String.format( "%X", INF_FILE_CRC ) + ", actual: " + String.format( "%X", ze.getCrc() ) + '.' );
		}
		if ( ze.getSize() != INF_FILE_SIZE ) {
			throw new StreamCorruptedException( "The original " + INF_FILE_NAME + " file is corrupted; file size miscompare, expected: " + INF_FILE_SIZE + " bytes, actual: " + ze.getSize() + " bytes." );
		}
		log.info( "File " + INF_FILE_NAME + " originale aperto; CRC OK: " + String.format( "%X", ze.getCrc() ) + '.' );
		return zis;
	}
	
	private void customize() throws Exception {
		// Lettura del file di properties BIKES.CFG...
		log.info( "Lettura del file " + CFG_FILE_NAME + "..." );
		BufferedReader br = null;
		try {
			br = new BufferedReader( new FileReader( path + CFG_FILE_NAME ) );
		}
		catch ( FileNotFoundException fnfe ) {
			log.info( "File " + CFG_FILE_NAME + " non trovato. Creazione file di default..." );
			writeDefaultBikesCfg();
			log.info( "Creazione file " + CFG_FILE_NAME + " di default completata." );
			br = new BufferedReader( new FileReader( path + CFG_FILE_NAME ) );
		}
		properties.load( br );
		br.close();
		log.info( "Lettura del file " + CFG_FILE_NAME + " completata. File chiuso." );
		
		// Elaborazione delle properties...
		for ( Object objectKey : properties.keySet() ) {
			String key = (String)objectKey;
			if ( !StringUtils.isNumeric( properties.getProperty( key ) ) ) {
				throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
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
				throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
			}
		}
		log.info( "Personalizzazioni applicate, " + changesCount + " valori modificati." );
	}

	private void writeDefaultBikesCfg() throws Exception {
        final String lineSeparator = java.security.AccessController.doPrivileged( new sun.security.action.GetPropertyAction( "line.separator" ) );
		final StringBuilder properties = new StringBuilder( "##### BIKES.INF demystified! #####" );
		
		for ( Bike.Type type : Bike.Type.values() ) {
			String prefix = Integer.toString( type.getDisplacement() );
			Bike bike = null;
			for ( Method metodo : BikesInf.class.getMethods() ) {
				if ( metodo.getName().startsWith( GETTER_PREFIX ) && metodo.getName().contains( prefix ) ) {
					bike = (Bike)metodo.invoke( bikesInf );
				}
			}
			
			if ( bike == null ) {
				throw new IllegalStateException();
			}
			
			properties.append( lineSeparator ).append( lineSeparator );
			properties.append( "### ").append( type.getDisplacement() ).append( " cc - Begin... ###");
			
			// Settings
			properties.append( lineSeparator );
			properties.append( "# " ).append( Settings.class.getSimpleName() ).append( " #" );
			properties.append( lineSeparator );
			for ( Method metodo : Settings.class.getMethods() ) {
				if ( metodo.getName().startsWith( GETTER_PREFIX ) && metodo.getReturnType() != null && "int".equals( metodo.getReturnType().getName() ) ) {
					properties.append( prefix ).append( '.' ).append( Introspector.decapitalize( Settings.class.getSimpleName() ) ).append( '.' ).append( Introspector.decapitalize( StringUtils.substringAfter( metodo.getName(), GETTER_PREFIX ) ) );
					properties.append( '=' );
					properties.append( (int)metodo.invoke( bike.getSettings() ) );
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
			
			properties.append( "### ").append( type.getDisplacement() ).append( " cc - End. ###");
		}
		
		properties.append( lineSeparator ).append( lineSeparator );
		properties.append( "##### EOF #####");

		// Salvataggio...
		BufferedWriter bw = new BufferedWriter( new FileWriter( path + CFG_FILE_NAME ) );
		bw.write( properties.toString() );
		bw.flush();
		bw.close();
	}

	private Bike getBike( String key ) throws Exception {
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
			throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
		}
		
		return bike;
	}

	private void parseTorqueProperty( String key ) throws Exception {
		short newValue = Short.parseShort( properties.getProperty( key ) );
		if ( newValue < Torque.MIN_VALUE || newValue > Torque.MAX_VALUE ) {
			throw new PropertyException( "Value must be between " + Torque.MIN_VALUE + " and " + Torque.MAX_VALUE + ": " + key + '=' + newValue );
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
			throw new PropertyException( "Unsupported property: " + key + '=' + newValue );
		}
	}

	private void parseGearboxProperty( String key ) throws Exception {
		int newValue = Integer.parseInt( properties.getProperty( key ) );
		if ( newValue < Gearbox.MIN_VALUE || newValue > Gearbox.MAX_VALUE ) {
			throw new PropertyException( "Value must be between " + Gearbox.MIN_VALUE + " and " + Gearbox.MAX_VALUE + ": " + key + '=' + newValue );
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
			throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
		}
	}

	private void parseSettingProperty( String key ) throws Exception {
		int newValue = Integer.parseInt( properties.getProperty( key ) );
		if ( newValue < Settings.MIN_VALUE || newValue > Settings.MAX_VALUE ) {
			throw new PropertyException( "Value must be between " + Settings.MIN_VALUE + " and " + Settings.MAX_VALUE + ": " + key + '=' + newValue );
		}
		
		Bike bike = getBike( key );
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Settings.class.getSimpleName() ) + '.' );
		Method setter = null;
		Method getter = null;
		for ( Method metodo : Settings.class.getMethods() ) {
			if ( metodo.getName().equals( "set" + StringUtils.capitalize( suffix ) ) ) {
				setter = metodo;
			}
			if ( metodo.getName().equals( GETTER_PREFIX + StringUtils.capitalize( suffix ) ) ) {
				getter = metodo;
			}
		}
		if ( setter != null && getter != null ) {
			int defaultValue = (int)getter.invoke( bike.getSettings() );
			if ( newValue != defaultValue ) {
				setter.invoke( bike.getSettings(), newValue );
				logChange( key, defaultValue, newValue );
			}
		}
		else {
			throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
		}
	}

	private void logChange( String key, int defaultValue, int newValue ) {
		log.info( "Applicata modifica: " + key + '=' + newValue + " (default: " + defaultValue + ")." );
		changesCount++;
	}
	
}