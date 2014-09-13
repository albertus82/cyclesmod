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
import java.util.LinkedHashMap;
import java.util.Map;
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

	public static void main( String... args ) throws Exception {
		if ( args.length > 1 ) {
			throw new IllegalArgumentException( "Too many parameters. Usage: CyclesMod [path]" );
		}
		String destinationPath = args.length > 0 ? args[0] : DEFAULT_DESTINATION_PATH;
		
		CyclesMod mod = new CyclesMod();
		
		log.info( "Apertura file " + INF_FILE_NAME + " originale..." );
		InputStream is = mod.getBikesInfInputStream();
		
		log.info( "Lettura del file " + INF_FILE_NAME + " originale..." );
		BikesInf bikesInf = mod.parseBikesInfInputStream( is );
		
		log.info( "Applicazione delle personalizzazioni alla configurazione..." );
		mod.customize( bikesInf, destinationPath );
		log.info( "Personalizzazioni applicate." );
		
		// File in uscita
		log.info( "Preparazione nuovo file " + INF_FILE_NAME + "..." );
		mod.writeCustomBikesInf( bikesInf, destinationPath );
	}
	
	private void writeCustomBikesInf( BikesInf bikesInf, String destinationPath ) throws IOException {
		byte[] newBikesInf = bikesInf.toByteArray();
		Checksum crc = new CRC32();
		crc.update( newBikesInf, 0, newBikesInf.length );
		
		log.info( "La configurazione" + ( crc.getValue() == INF_FILE_CRC ? " non " : ' ' ) + "e' stata modificata; CRC: " + String.format( "%X", crc.getValue() ) + '.' );

		BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( destinationPath + INF_FILE_NAME ), INF_FILE_SIZE );
		bos.write( newBikesInf );
		bos.flush();
		bos.close();
		log.info( "Nuovo file " + INF_FILE_NAME + " scritto correttamente nel percorso \"" + destinationPath + "\"; CRC: " + String.format( "%X", crc.getValue() ) + '.' );
	}
	
	private BikesInf parseBikesInfInputStream( InputStream is ) throws IOException {
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
		BikesInf bikesInf = new BikesInf( bike125, bike250, bike500 );
		log.info( "File " + INF_FILE_NAME + " originale elaborato." );
		return bikesInf;
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
	
	private void customize( BikesInf bikesInf, String destinationPath ) throws Exception {

		// Lettura del file di properties BIKES.CFG...
		log.info( "Lettura del file " + CFG_FILE_NAME + "..." );
		Properties properties = new Properties();
		BufferedReader br = null;
		try {
			br = new BufferedReader( new FileReader( destinationPath + CFG_FILE_NAME ) );
		}
		catch ( FileNotFoundException fnfe ) {
			log.info( "File " + CFG_FILE_NAME + " non trovato. Creazione file di default..." );
			writeDefaultCfg( bikesInf, destinationPath );
			log.info( "Creazione file " + CFG_FILE_NAME + " di default completata." );
			br = new BufferedReader( new FileReader( destinationPath + CFG_FILE_NAME ) );
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
			
			Bike bike = getBike(properties, key, bikesInf);
			
			// Settings
			if ( key.substring(4).startsWith( Introspector.decapitalize( Settings.class.getSimpleName() ) ) ) {
				parseSettingProperty(properties, key, bike);
			}
			
			// Gearbox
			else if ( key.substring(4).startsWith( Introspector.decapitalize( Gearbox.class.getSimpleName() ) ) ) {
				parseGearboxProperty(properties, key, bike);
			}
			
			// Torque
			else if ( key.substring(4).startsWith( Introspector.decapitalize( Torque.class.getSimpleName() ) ) ) {
				parseTorqueProperty(properties, key, bike);
			}
			
			else {
				throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
			}
		}
	}

	private void writeDefaultCfg( BikesInf bikesInf, String destinationPath ) throws Exception {
		Map<String, String> properties = new LinkedHashMap<String, String>();
		
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
			
			// Settings
			for ( Method metodo : Settings.class.getMethods() ) {
				if ( metodo.getName().startsWith( GETTER_PREFIX ) && metodo.getReturnType() != null && "int".equals( metodo.getReturnType().getName() ) ) {
					properties.put( prefix + '.' + Introspector.decapitalize( Settings.class.getSimpleName() ) + '.' + Introspector.decapitalize( StringUtils.substringAfter( metodo.getName(), GETTER_PREFIX ) ), Integer.toString( (int) metodo.invoke( bike.getSettings() ) ) );
				}
			}
			
			// Gearbox
			for ( int index = 0; index < bike.getGearbox().getRatios().length; index++ ) {
				properties.put( prefix + '.' + Introspector.decapitalize( Gearbox.class.getSimpleName() ) + '.' + index, Integer.toString( bike.getGearbox().getRatios()[ index ] ) );
			}
			
			// Torque
			for ( int index = 0; index < bike.getTorque().getCurve().length; index++ ) {
				properties.put( prefix + '.' + Introspector.decapitalize( Torque.class.getSimpleName() ) + '.' + index, Short.toString( bike.getTorque().getCurve()[ index ] ) );
			}
		}

		// Salvataggio...
		BufferedWriter bw = new BufferedWriter( new FileWriter( destinationPath + CFG_FILE_NAME ) );
		bw.write( "# Default BIKES.INF" );
		for ( String key : properties.keySet() ) {
			bw.newLine();
			bw.write( key + '=' + properties.get( key ) );
		}
		bw.flush();
		bw.close();
	}

	private Bike getBike( Properties properties, String key, BikesInf bikesInf ) throws Exception {
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

	private void parseTorqueProperty( Properties properties, String key, Bike bike ) throws PropertyException {
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Torque.class.getSimpleName() ) + '.' );
		if ( StringUtils.isNotEmpty( suffix ) && StringUtils.isNumeric( suffix ) && Integer.parseInt( suffix ) < Torque.LENGTH ) {
			if ( bike.getTorque().getCurve()[ Integer.parseInt( suffix ) ] != Short.parseShort( properties.getProperty( key ) ) ) {
				bike.getTorque().getCurve()[ Integer.parseInt( suffix ) ] = Short.parseShort( properties.getProperty( key ) );
				log.info( "Applicata modifica: " + key + '=' + properties.getProperty( key ) );
			}
		}
		else {
			throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
		}
	}

	private void parseGearboxProperty( Properties properties, String key, Bike bike ) throws PropertyException {
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Gearbox.class.getSimpleName() ) + '.' );
		if ( StringUtils.isNotEmpty( suffix ) && StringUtils.isNumeric( suffix ) && Integer.parseInt( suffix ) < Gearbox.LENGTH / 2 ) {
			if ( bike.getGearbox().getRatios()[ Integer.parseInt( suffix ) ] != Integer.parseInt( properties.getProperty( key ) ) ) { 
				bike.getGearbox().getRatios()[ Integer.parseInt( suffix ) ] = Integer.parseInt( properties.getProperty( key ) );
				log.info( "Applicata modifica: " + key + '=' + properties.getProperty( key ) );
			}
		}
		else {
			throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
		}
	}

	private void parseSettingProperty( Properties properties, String key, Bike bike ) throws Exception {
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
			if ( Integer.parseInt( properties.getProperty( key ) ) != (int)getter.invoke( bike.getSettings() ) ) {
				setter.invoke( bike.getSettings(), Integer.parseInt( properties.getProperty( key ) ) );
				log.info( "Applicata modifica: " + key + '=' + properties.getProperty( key ) );
			}
		}
		else {
			throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
		}
	}
	
}