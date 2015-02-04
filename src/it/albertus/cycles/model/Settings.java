package it.albertus.cycles.model;

import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.resources.Resources;
import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Settings extends BikesInfElement {
	
	public static final int LENGTH = 22;
	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 65535;
	
	private final Map<Setting, Integer> values = new TreeMap<Setting, Integer>();
	
	public Settings( int gearsCount, int rpmRedline, int rpmLimit, int overspeedGracePeriod, int grip, int unknown1, int brakingSpeed, int unknown2, int spinThreshold, int unknown3, int rpmDownshift ) {
		values.put( Setting.gearsCount, gearsCount );
		values.put( Setting.rpmRedline, rpmRedline );
		values.put( Setting.rpmLimit, rpmLimit );
		values.put( Setting.overspeedGracePeriod, overspeedGracePeriod );
		values.put( Setting.grip, grip );
		values.put( Setting.unknown1, unknown1 );
		values.put( Setting.brakingSpeed, brakingSpeed );
		values.put( Setting.unknown2, unknown2 );
		values.put( Setting.spinThreshold, spinThreshold );
		values.put( Setting.unknown3, unknown3 );
		values.put( Setting.rpmDownshift, rpmDownshift );
	}
	
	@Override
	public List<Byte> toByteList() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		for ( int value : values.values() ) {
			byteList.addAll( ByteUtils.toByteList( value ) );
		}
		return byteList;
	}
	
	public static int parse( final String key, final String value ) {
		long newValue = Long.parseLong( value );
		if ( newValue < MIN_VALUE || newValue > MAX_VALUE ) {
			throw new InvalidPropertyException( Resources.get( "err.illegal.value", MIN_VALUE, MAX_VALUE, key, newValue ) );
		}
		return (int) newValue;
	}
	
	public Map<Setting, Integer> getValues() {
		return values;
	}
	
}