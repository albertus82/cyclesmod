package it.albertus.cycles.engine;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

import java.beans.Introspector;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PropertyParser {
	
	private static final Logger log = LoggerFactory.getLogger( PropertyParser.class );
	
	protected BikesInf bikesInf;
	
	protected boolean applyProperty(String key, String value) {
		if ( StringUtils.isBlank( value ) || !StringUtils.isNumeric( value ) ) {
			throw new InvalidPropertyException( Messages.get( "err.unsupported.property", key, value ) );
		}
		
		boolean applied = false;
		
		// Settings
		if ( isSettingsProperty(key) ) {
			applied = applySettingProperty( key, value );
		}
		
		// Gearbox
		else if ( isGearboxProperty(key) ) {
			applied = applyGearboxProperty( key, value );
		}
		
		// Torque
		else if ( isTorqueProperty(key) ) {
			applied = applyTorqueProperty( key, value );
		}
		
		else {
			throw new InvalidPropertyException( Messages.get( "err.unsupported.property", key, value ) );
		}
		
		return applied;
	}

	protected boolean isTorqueProperty(String key) {
		return StringUtils.substringAfter( key, "." ).startsWith( Introspector.decapitalize( Torque.class.getSimpleName() ) );
	}

	protected boolean isGearboxProperty(String key) {
		return StringUtils.substringAfter( key, "." ).startsWith( Introspector.decapitalize( Gearbox.class.getSimpleName() ) );
	}

	protected boolean isSettingsProperty(String key) {
		return StringUtils.substringAfter( key, "." ).startsWith( Introspector.decapitalize( Settings.class.getSimpleName() ) );
	}
	
	private boolean applyTorqueProperty( final String key, final String value ) {
		boolean applied = false;
		short newValue = Torque.parse( key, value );
		
		Bike bike = getBike( key, value );
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Torque.class.getSimpleName() ) + '.' );
		if ( StringUtils.isNotEmpty( suffix ) && StringUtils.isNumeric( suffix ) && Integer.parseInt( suffix ) < bike.getTorque().getCurve().length ) {
			int index = Integer.parseInt( suffix );
			short defaultValue = bike.getTorque().getCurve()[ index ];
			if ( defaultValue != newValue ) {
				bike.getTorque().getCurve()[ index ] = newValue;
				applied = true;
				logChange( key, defaultValue, newValue );
			}
		}
		else {
			throw new InvalidPropertyException( Messages.get( "err.unsupported.property", key, value ) );
		}
		return applied;
	}
	
	private boolean applyGearboxProperty( final String key, final String value ) {
		boolean applied = false;
		int newValue = Gearbox.parse( key, value );
		
		Bike bike = getBike( key, value );
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Gearbox.class.getSimpleName() ) + '.' );
		if ( StringUtils.isNotEmpty( suffix ) && StringUtils.isNumeric( suffix ) && Integer.parseInt( suffix ) < bike.getGearbox().getRatios().length ) {
			int index = Integer.parseInt( suffix );
			int defaultValue = bike.getGearbox().getRatios()[ index ];
			if ( defaultValue != newValue ) {
				bike.getGearbox().getRatios()[ index ] = newValue;
				applied = true;
				logChange( key, defaultValue, newValue );
			}
		}
		else {
			throw new InvalidPropertyException( Messages.get( "err.unsupported.property", key, value ) );
		}
		return applied;
	}
	
	private boolean applySettingProperty( final String key, final String value ) {
		boolean applied = false;
		int newValue = Settings.parse( key, value );
		
		Bike bike = getBike( key, value );
		String suffix = StringUtils.substringAfter( key, Introspector.decapitalize( Settings.class.getSimpleName() ) + '.' );
		Setting setting = Setting.getSetting( suffix );
		if ( setting != null ) {
			int defaultValue = bike.getSettings().getValues().get( setting );
			if ( newValue != defaultValue ) {
				bike.getSettings().getValues().put( setting, newValue );
				applied = true;
				logChange( key, defaultValue, newValue );
			}
		}
		else {
			throw new InvalidPropertyException( Messages.get( "err.unsupported.property", key, value ) );
		}
		return applied;
	}
	
	private void logChange( final String key, final int defaultValue, final int newValue ) {
		log.info( Messages.get( "msg.custom.value.detected", key, newValue, String.format( "%X", newValue ), defaultValue, String.format( "%X", defaultValue ) ) );
	}
	
	protected Bike getBike( final String key, final String value ) {
		Bike bike = bikesInf.getBike( Integer.parseInt( StringUtils.substringBefore( key, "." ) ) );
		if ( bike == null ) {
			throw new InvalidPropertyException( Messages.get( "err.unsupported.property", key, value ) );
		}
		return bike;
	}
}
