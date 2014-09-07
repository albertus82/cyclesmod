package it.albertus.cycles.model;

import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

public class Gearbox {
	
	protected static final int LENGTH = 14;

	private int neutral; // 22-23
	private int gear1st; // 24-25
	private int gear2nd; // 26-27
	private int gear3rd; // 28-29
	private int gear4th; // 30-31
	private int gear5th; // 32-33
	private int gear6th; // 34-35
	
	public Gearbox(int neutral, int gear1st, int gear2nd, int gear3rd, int gear4th, int gear5th, int gear6th) {
		super();
		this.neutral = neutral;
		this.gear1st = gear1st;
		this.gear2nd = gear2nd;
		this.gear3rd = gear3rd;
		this.gear4th = gear4th;
		this.gear5th = gear5th;
		this.gear6th = gear6th;
	}
	
	public List<Byte> toByteList() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		byteList.addAll( ByteUtils.toByteList( neutral ) );
		byteList.addAll( ByteUtils.toByteList( gear1st ) );
		byteList.addAll( ByteUtils.toByteList( gear2nd ) );
		byteList.addAll( ByteUtils.toByteList( gear3rd ) );
		byteList.addAll( ByteUtils.toByteList( gear4th ) );
		byteList.addAll( ByteUtils.toByteList( gear5th ) );
		byteList.addAll( ByteUtils.toByteList( gear6th ) );
		return byteList;
	}
	
	public int getNeutral() {
		return neutral;
	}
	public void setNeutral(int neutral) {
		this.neutral = neutral;
	}
	public int getGear1st() {
		return gear1st;
	}
	public void setGear1st(int gear1st) {
		this.gear1st = gear1st;
	}
	public int getGear2nd() {
		return gear2nd;
	}
	public void setGear2nd(int gear2nd) {
		this.gear2nd = gear2nd;
	}
	public int getGear3rd() {
		return gear3rd;
	}
	public void setGear3rd(int gear3rd) {
		this.gear3rd = gear3rd;
	}
	public int getGear4th() {
		return gear4th;
	}
	public void setGear4th(int gear4th) {
		this.gear4th = gear4th;
	}
	public int getGear5th() {
		return gear5th;
	}
	public void setGear5th(int gear5th) {
		this.gear5th = gear5th;
	}
	public int getGear6th() {
		return gear6th;
	}
	public void setGear6th(int gear6th) {
		this.gear6th = gear6th;
	}
	
}