package it.albertus.cycles.model;

import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Settings extends BikesInfElement {
	
	public static final int LENGTH = 22;
	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 65535;
	
	private int gearsCount; // 0-1: numero di marce del cambio (solo lsB).
	private int rpmRedMark; // 2-3: regime red mark (stessa scala del regime limitatore, il motore si rompe quando regime >= regime red mark per piu' di qualche secondo).
	private int rpmLimit; // 4-5: regime limitatore, max 14334 (il contagiri si blocca ma la moto conserva le prestazioni di accelerazione a quel regime, quindi la coppia a quel regime deve essere azzerata se si vuole interrompere l'accelerazione della moto).
	private int rpmRedMarkGracePeriod; // 6-7: periodo di grazia su red mark (valore alto: il motore si rompe dopo piu' tempo. Per valori msB >=80 si rompe subito).
	private int skiddingThreshold; // 8-9: soglia di slittamento in sterzata (valore alto: slitta meno).
	private int unknown1; // 10-11: ?
	private int brakingSpeed; // 12-13: velocita' di frenata.
	private int unknown2; // 14-15: ?
	private int spinThreshold; // 16-17: soglia di testacoda (valore basso: testacoda piu' probabile. Per valori msB >=80 testacoda sicuro).
	private int unknown3; // 18-19: ?
	private int rpmDownshift; // 20-21: regime di scalata con cambio automatico (skill < 3).
	
	public Settings(int gearsCount, int rpmRedMark, int rpmLimit, int rpmRedMarkGracePeriod, int skiddingThreshold, int unknown1, int brakingSpeed, int unknown2, int spinThreshold, int unknown3, int rpmDownshift) {
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
		this.rpmDownshift = rpmDownshift;
	}
	
	@Override
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
		byteList.addAll( ByteUtils.toByteList( rpmDownshift ) );
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
	
	public int getRpmDownshift() {
		return rpmDownshift;
	}
	public void setRpmDownshift(int rpmDownshift) {
		this.rpmDownshift = rpmDownshift;
	}
	
	public int getGearsCount() {
		return gearsCount;
	}
	public void setGearsCount(int gearsCount) {
		this.gearsCount = gearsCount;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this );
	}

}