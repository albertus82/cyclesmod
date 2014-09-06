package it.albertus.cycles.model;

import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

public class Settings {
	
	protected static final int LENGTH = 22;

	private int gearsCount; // 0-1
	private int rpmRedMark; // 2-3
	private int rpmLimit; // 4-5
	private int rpmRedMarkGracePeriod; // 6-7
	private int skiddingThreshold; // 8-9
	private int unknown1; // 10-11
	private int brakingSpeed; // 12-13
	private int unknown2; // 14-15
	private int spinThreshold; // 16-17
	private int unknown3; // 18-19
	private int unknown4; // 20-21
	
	public Settings(int gearsCount, int rpmRedMark, int rpmLimit,
			int rpmRedMarkGracePeriod, int skiddingThreshold, int unknown1,
			int brakingSpeed, int unknown2, int spinThreshold, int unknown3,
			int unknown4) {
		super();
		this.gearsCount = gearsCount;
		this.rpmRedMark = rpmRedMark;
		this.rpmLimit = rpmLimit;
		this.rpmRedMarkGracePeriod = rpmRedMarkGracePeriod;
		this.skiddingThreshold = skiddingThreshold;
		this.unknown1 = unknown1;
		this.brakingSpeed = brakingSpeed;
		this.unknown2 = unknown2;
		this.spinThreshold = spinThreshold;
		this.unknown3 = unknown3;
		this.unknown4 = unknown4;
	}
	
	public List<Byte> toByteList() {
		List<Byte> byteList = new ArrayList<Byte>( LENGTH );
		byteList.addAll( ByteUtils.toByteList( gearsCount ) );
		byteList.addAll( ByteUtils.toByteList( rpmRedMark ) );
		byteList.addAll( ByteUtils.toByteList( rpmLimit ) );
		byteList.addAll( ByteUtils.toByteList( rpmRedMarkGracePeriod ) );
		byteList.addAll( ByteUtils.toByteList( skiddingThreshold ) );
		byteList.addAll( ByteUtils.toByteList( unknown1 ) );
		byteList.addAll( ByteUtils.toByteList( brakingSpeed ) );
		byteList.addAll( ByteUtils.toByteList( unknown2 ) );
		byteList.addAll( ByteUtils.toByteList( spinThreshold ) );
		byteList.addAll( ByteUtils.toByteList( unknown3 ) );
		byteList.addAll( ByteUtils.toByteList( unknown4 ) );
		return byteList;
	}
	
	
	public int getRpmRedMark() {
		return rpmRedMark;
	}
	public void setRpmRedMark(int rpmRedMark) {
		this.rpmRedMark = rpmRedMark;
	}
	public int getRpmLimit() {
		return rpmLimit;
	}
	public void setRpmLimit(int rpmLimit) {
		this.rpmLimit = rpmLimit;
	}
	public int getRpmRedMarkGracePeriod() {
		return rpmRedMarkGracePeriod;
	}
	public void setRpmRedMarkGracePeriod(int rpmRedMarkGracePeriod) {
		this.rpmRedMarkGracePeriod = rpmRedMarkGracePeriod;
	}
	public int getSkiddingThreshold() {
		return skiddingThreshold;
	}
	public void setSkiddingThreshold(int skiddingThreshold) {
		this.skiddingThreshold = skiddingThreshold;
	}
	public int getUnknown1() {
		return unknown1;
	}
	public void setUnknown1(int unknown1) {
		this.unknown1 = unknown1;
	}
	public int getBrakingSpeed() {
		return brakingSpeed;
	}
	public void setBrakingSpeed(int brakingSpeed) {
		this.brakingSpeed = brakingSpeed;
	}
	public int getUnknown2() {
		return unknown2;
	}
	public void setUnknown2(int unknown2) {
		this.unknown2 = unknown2;
	}
	public int getSpinThreshold() {
		return spinThreshold;
	}
	public void setSpinThreshold(int spinThreshold) {
		this.spinThreshold = spinThreshold;
	}
	public int getUnknown3() {
		return unknown3;
	}
	public void setUnknown3(int unknown3) {
		this.unknown3 = unknown3;
	}
	public int getUnknown4() {
		return unknown4;
	}
	public void setUnknown4(int unknown4) {
		this.unknown4 = unknown4;
	}
	public int getGearsCount() {
		return gearsCount;
	}
	public void setGearsCount(int gearsCount) {
		this.gearsCount = gearsCount;
	}
	
}