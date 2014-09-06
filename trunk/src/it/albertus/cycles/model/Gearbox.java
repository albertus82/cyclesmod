package it.albertus.cycles.model;

import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

public class Gearbox {
	
	protected static final int LENGTH = 14;

	private int neutral; // 22-23
	private int _1st;    // 24-25
	private int _2nd;    // 26-27
	private int _3rd;    // 28-29
	private int _4th;    // 30-31
	private int _5th;    // 32-33
	private int _6th;    // 34-35
	
	public Gearbox(int neutral, int _1st, int _2nd, int _3rd, int _4th,	int _5th, int _6th) {
		super();
		this.neutral = neutral;
		this._1st = _1st;
		this._2nd = _2nd;
		this._3rd = _3rd;
		this._4th = _4th;
		this._5th = _5th;
		this._6th = _6th;
	}
	
	public List<Byte> toByteList() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		byteList.addAll( ByteUtils.toByteList( neutral ) );
		byteList.addAll( ByteUtils.toByteList( _1st ) );
		byteList.addAll( ByteUtils.toByteList( _2nd ) );
		byteList.addAll( ByteUtils.toByteList( _3rd ) );
		byteList.addAll( ByteUtils.toByteList( _4th ) );
		byteList.addAll( ByteUtils.toByteList( _5th ) );
		byteList.addAll( ByteUtils.toByteList( _6th ) );
		return byteList;
	}
	
	public int getNeutral() {
		return neutral;
	}
	public void setNeutral(int neutral) {
		this.neutral = neutral;
	}
	public int get_1st() {
		return _1st;
	}
	public void set_1st(int _1st) {
		this._1st = _1st;
	}
	public int get_2nd() {
		return _2nd;
	}
	public void set_2nd(int _2nd) {
		this._2nd = _2nd;
	}
	public int get_3rd() {
		return _3rd;
	}
	public void set_3rd(int _3rd) {
		this._3rd = _3rd;
	}
	public int get_4th() {
		return _4th;
	}
	public void set_4th(int _4th) {
		this._4th = _4th;
	}
	public int get_5th() {
		return _5th;
	}
	public void set_5th(int _5th) {
		this._5th = _5th;
	}
	public int get_6th() {
		return _6th;
	}
	public void set_6th(int _6th) {
		this._6th = _6th;
	}

}