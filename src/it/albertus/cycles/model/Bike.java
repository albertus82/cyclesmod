package it.albertus.cycles.model;

import static it.albertus.util.ByteUtils.toInt;
import static it.albertus.util.ByteUtils.toShortArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bike {
	
	public static final int LENGTH = Settings.LENGTH + Gearbox.LENGTH + Torque.LENGTH;
	
	private Settings settings; // 0-21
	private Gearbox gearbox;   // 22-35
	private Torque torque;     // 36-147
	
	public Bike( byte[] inf ) {
		this( new Settings( toInt( inf[0], inf[1] ), toInt( inf[2], inf[3] ), toInt( inf[4], inf[5] ), toInt( inf[6], inf[7] ), toInt( inf[8], inf[9] ), toInt( inf[10], inf[11] ), toInt( inf[12], inf[13] ), toInt( inf[14], inf[15] ), toInt( inf[16], inf[17] ), toInt( inf[18], inf[19] ), toInt( inf[20], inf[21] ) ), new Gearbox( toInt( inf[22], inf[23] ), toInt( inf[24], inf[25] ), toInt( inf[26], inf[27] ), toInt( inf[28], inf[29] ), toInt( inf[30], inf[31] ), toInt( inf[32], inf[33] ), toInt( inf[34], inf[35] ) ), new Torque( toShortArray( Arrays.copyOfRange( inf, 36, 148 ) ) ) );
	}
	
	public Bike(Settings settings, Gearbox gearbox, Torque torque) {
		super();
		this.settings = settings;
		this.gearbox = gearbox;
		this.torque = torque;
	}
	
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
	public void setGearbox(Gearbox gearbox) {
		this.gearbox = gearbox;
	}
	public Torque getTorque() {
		return torque;
	}
	public void setTorque(Torque torque) {
		this.torque = torque;
	}
	public Settings getSettings() {
		return settings;
	}
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
}