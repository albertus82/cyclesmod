package it.albertus.cycles.model;

import it.albertus.cycles.engine.PropertyException;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Settings extends BikesInfElement {
	
	public static final int LENGTH = 22;
	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 65535;
	
	private final Map<Setting, Integer> values = new TreeMap<Setting, Integer>();
	
	public Settings( int gearsCount, int rpmRedMark, int rpmLimit, int overspeedGracePeriod, int grip, int unknown1, int brakingSpeed, int unknown2, int spinThreshold, int unknown3, int rpmDownshift ) {
		values.put( Setting.GEARS_COUNT, gearsCount );
		values.put( Setting.RPM_RED_MARK, rpmRedMark );
		values.put( Setting.RPM_LIMIT, rpmLimit );
		values.put( Setting.OVERSPEED_GRACE_PERIOD, overspeedGracePeriod );
		values.put( Setting.GRIP, grip );
		values.put( Setting.UNKNOWN_1, unknown1 );
		values.put( Setting.BRAKING_SPEED, brakingSpeed );
		values.put( Setting.UNKNOWN_2, unknown2 );
		values.put( Setting.SPIN_THRESHOLD, spinThreshold );
		values.put( Setting.UNKNOWN_3, unknown3 );
		values.put( Setting.RPM_DOWNSHIFT, rpmDownshift );
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
			throw new PropertyException( Messages.get( "err.illegal.value", MIN_VALUE, MAX_VALUE, key, newValue ) );
		}
		return (int) newValue;
	}
	
	public Map<Setting, Integer> getValues() {
		return values;
	}
	
}