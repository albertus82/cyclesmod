package it.albertus.cycles;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.PropertyException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mod {

	private static final Logger log = LoggerFactory.getLogger( Mod.class );
	
	private static final String CFG_FILE_NAME = "BIKES.CFG";

	protected static final String DESTINATION_PATH = "D:\\Documents\\Dropbox\\Personale\\DOS\\CYCLES\\";
	
	protected static void customize( FileBikesInf bikesInf ) throws Exception {

		// Lettura del file di properties BIKES.CFG...
		log.info( "Lettura del file " + CFG_FILE_NAME + "..." );
		Properties properties = new Properties();
		BufferedReader br = null;
		try {
			br = new BufferedReader( new FileReader( DESTINATION_PATH + CFG_FILE_NAME ) );
		}
		catch ( FileNotFoundException fnfe ) {
			log.info( "File " + CFG_FILE_NAME + " non trovato. Creazione file di default..." );
			writeDefaultCfg( bikesInf );
			log.info( "Creazione file " + CFG_FILE_NAME + " di default completata." );
			br = new BufferedReader( new FileReader( DESTINATION_PATH + CFG_FILE_NAME ) );
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
		
//		bikesInf.getBike500().getGearbox().getRatios()[6] *= 1.08;
		
//		for ( int i = 1; i <= 6; i++ ) {
//			bikesInf.getBike500().getGearbox().getGearRatios()[i] = (int)(bikesInf.getBike500().getGearbox().getGearRatios()[i] * 0.89);
//		}
//		
//		for ( int i = 0; i < 106; i++ ) {
//			bikesInf.getBike500().getTorque().getCurve()[i] = (short)(bikesInf.getBike500().getTorque().getCurve()[i] * 2);	
//		}
		
	}

	private static void writeDefaultCfg( FileBikesInf bikesInf ) throws Exception {
		Map<String, String> properties = new LinkedHashMap<String, String>();
		String[] bikes = { "125", "250", "500" };
		
		for ( String prefix : bikes ) {
			Bike bike = null;
			for ( Method metodo : FileBikesInf.class.getMethods() ) {
				if ( metodo.getName().startsWith( "get" ) && metodo.getName().contains( prefix ) ) {
					bike = (Bike)metodo.invoke( bikesInf );
				}
			}
			
			if ( bike == null ) {
				throw new IllegalStateException();
			}
			
			// Settings
			for ( Method metodo : Settings.class.getMethods() ) {
				if ( metodo.getName().startsWith( "get" ) && metodo.getReturnType() != null && metodo.getReturnType().getName().equals( "int" ) ) {
					properties.put( prefix + '.' + Introspector.decapitalize( Settings.class.getSimpleName() ) + '.' + Introspector.decapitalize( StringUtils.substringAfter( metodo.getName(), "get" ) ), Integer.toString( (int) metodo.invoke( bike.getSettings() ) ) );
				}
			}
			
			// Gearbox
			for ( int i = 0; i < Gearbox.LENGTH / 2; i++ ) {
				properties.put( prefix + '.' + Introspector.decapitalize( Gearbox.class.getSimpleName() ) + '.' + i, Integer.toString( bike.getGearbox().getRatios()[i] ) );
			}
			
			// Torque
			for ( int i = 0; i < Torque.LENGTH; i++ ) {
				properties.put( prefix + '.' + Introspector.decapitalize( Torque.class.getSimpleName() ) + '.' + i, Short.toString( bike.getTorque().getCurve()[i] ) );
			}
		}

		// Salvataggio...
		BufferedWriter bw = new BufferedWriter( new FileWriter( DESTINATION_PATH + CFG_FILE_NAME ) );
		bw.write( "# Default BIKES.INF" );
		for ( String key : properties.keySet() ) {
			bw.newLine();
			bw.write( key + '=' + properties.get( key ) );
		}
		bw.flush();
		bw.close();
	}

	private static Bike getBike( Properties properties, String key, FileBikesInf bikesInf ) throws PropertyException {
		Bike bike;
		
		if ( key.startsWith( "125." ) ) {
			bike = bikesInf.getBike125();
		}
		else if ( key.startsWith( "250." ) ) {
			bike = bikesInf.getBike250();
		}
		else if ( key.startsWith( "500." ) ) {
			bike = bikesInf.getBike500();
		}
		else {
			throw new PropertyException( "Unsupported property: " + key + '=' + properties.getProperty( key ) );
		}
		
		return bike;
	}

	private static void parseTorqueProperty( Properties properties, String key, Bike bike ) throws PropertyException {
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

	private static void parseGearboxProperty( Properties properties, String key, Bike bike ) throws PropertyException {
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

	private static void parseSettingProperty( Properties properties, String key, Bike bike ) throws Exception {
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Settings.class.getSimpleName() ) + '.' );
		Method setter = null;
		Method getter = null;
		for ( Method metodo : Settings.class.getMethods() ) {
			if ( metodo.getName().equals( "set" + StringUtils.capitalize( suffix ) ) ) {
				setter = metodo;
			}
			if ( metodo.getName().equals( "get" + StringUtils.capitalize( suffix ) ) ) {
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