package it.albertus.cycles.model;

import it.albertus.cycles.resources.Messages;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BikesCfg {
	
	private static final Logger log = LoggerFactory.getLogger( BikesCfg.class );
	
	private static final String FILE_NAME = "BIKES.CFG";
	
	private final Properties properties = new Properties();
	
	public BikesCfg( final BikesInf bikesInf ) {
		StringReader reader = new StringReader( createProperties( bikesInf ) );
		try {
			populateProperties( reader );
		}
		catch ( IOException ioe ) {} // No exception possible with StringReader!
	}

	private void populateProperties(Reader reader) throws IOException {
		this.properties.load( reader );	
		reader.close();
	}

	public BikesCfg( final BikesInf originalBikesInf, final String path ) throws IOException {
		log.info( Messages.get( "msg.reading.file", FILE_NAME ) );
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader( path + FILE_NAME ) );
		}
		catch ( FileNotFoundException fnfe ) {
			log.info( Messages.get( "msg.file.not.found.creating.default", FILE_NAME ) );
			writeDefaultBikesCfg( originalBikesInf, path );
			log.info( Messages.get( "msg.default.file.created", FILE_NAME ) );
			reader = new BufferedReader( new FileReader( path + FILE_NAME ) );
		}
		populateProperties( reader );
		log.info( Messages.get( "msg.file.read", FILE_NAME ) );
	}
	
	private void writeDefaultBikesCfg( final BikesInf originalBikesInf, final String path ) throws IOException {
		final String properties = createProperties(originalBikesInf);

		// Salvataggio...
		BufferedWriter bw = new BufferedWriter( new FileWriter( path + FILE_NAME ) );
		bw.write( properties.toString() );
		bw.flush();
		bw.close();
	}

	private String createProperties( final BikesInf bikesInf ) {
		final String lineSeparator = java.security.AccessController.doPrivileged( new sun.security.action.GetPropertyAction( "line.separator" ) );
		final StringBuilder properties = new StringBuilder( Messages.get( "str.cfg.header" ) );
		
		for ( Bike bike : bikesInf.getBikes() ) {
			String prefix = Integer.toString( bike.getType().getDisplacement() );

			properties.append( lineSeparator ).append( lineSeparator );
			properties.append( "### ").append( bike.getType().getDisplacement() ).append( " cc - " + Messages.get( "str.cfg.begin" ) + "... ###");
			
			// Settings
			properties.append( lineSeparator );
			properties.append( "# " ).append( Settings.class.getSimpleName() ).append( " #" );
			properties.append( lineSeparator );
			for ( Setting setting : bike.getSettings().getValues().keySet() ) {
				properties.append( prefix ).append( '.' ).append( Introspector.decapitalize( Settings.class.getSimpleName() ) ).append( '.' ).append( setting.toString() );
				properties.append( '=' );
				properties.append( bike.getSettings().getValues().get( setting ).intValue() );
				properties.append( lineSeparator );
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
			properties.append( "# " ).append( Torque.class.getSimpleName() ).append( " (").append( Torque.getRpm( 0 ) ).append( '-' ).append( Torque.getRpm( Torque.LENGTH ) - 1 ).append( " RPM) #" );
			properties.append( lineSeparator );
			for ( int index = 0; index < bike.getTorque().getCurve().length; index++ ) {
				if ( index > 0 && index % 8 == 0 ) {
					properties.append( "# " + Torque.getRpm( index ) + " RPM");
					properties.append( lineSeparator );
				}
				properties.append( prefix ).append( '.' ).append( Introspector.decapitalize( Torque.class.getSimpleName() ) ).append( '.' ).append( index );
				properties.append( '=' );
				properties.append( bike.getTorque().getCurve()[ index ] );
				properties.append( lineSeparator );
			}
			
			properties.append( "### ").append( bike.getType().getDisplacement() ).append( " cc - " + Messages.get( "str.cfg.end" ) + ". ###");
		}
		
		properties.append( lineSeparator ).append( lineSeparator );
		properties.append( Messages.get( "str.cfg.footer" ) );
		return properties.toString();
	}
	
	public Properties getProperties() {
		return properties;
	}
	
}