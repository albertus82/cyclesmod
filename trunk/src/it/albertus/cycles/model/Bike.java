package it.albertus.cycles.model;

import static it.albertus.util.ByteUtils.toInt;
import static it.albertus.util.ByteUtils.toIntArray;
import static it.albertus.util.ByteUtils.toShortArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bike extends BikesInfElement {
	
	public enum Type {
		_125( 125 ),
		_250( 250 ),
		_500( 500 );
		
		private int displacement;
		
		private Type( int displacement ) {
			this.displacement = displacement;
		}
		
		public int getDisplacement() {
			return displacement;
		}
	}
	
	public static final int LENGTH = Settings.LENGTH + Gearbox.LENGTH + Torque.LENGTH;
	
	private Settings settings; // 0-21
	private Gearbox gearbox;   // 22-41
	private Torque torque;     // 42-147
	
	public Bike( byte[] inf ) {
		this( new Settings( toInt( inf[0], inf[1] ), toInt( inf[2], inf[3] ), toInt( inf[4], inf[5] ), toInt( inf[6], inf[7] ), toInt( inf[8], inf[9] ), toInt( inf[10], inf[11] ), toInt( inf[12], inf[13] ), toInt( inf[14], inf[15] ), toInt( inf[16], inf[17] ), toInt( inf[18], inf[19] ), toInt( inf[20], inf[21] ) ), new Gearbox( toIntArray( Arrays.copyOfRange( inf, 22, 42 ) ) ), new Torque( toShortArray( Arrays.copyOfRange( inf, 42, 148 ) ) ) );
	}
	
	public Bike(Settings settings, Gearbox gearbox, Torque torque) {
		this.settings = settings;
		this.gearbox = gearbox;
		this.torque = torque;
	}
	
	@Override
	public List<Byte> toByteList() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		byteList.addAll( settings.toByteList() );
		byteList.addAll( gearbox.toByteList() );
		byteList.addAll( torque.toByteList() );
		return byteList;
	}
	
	public Gearbox getGearbox() {
		return gearbox;
	}
	
	public Torque getTorque() {
		return torque;
	}
	
	public Settings getSettings() {
		return settings;
	}
	
}