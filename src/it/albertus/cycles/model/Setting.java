package it.albertus.cycles.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

public enum Setting { // The order matters.
	
	GEARS_COUNT, // 0-1: numero di marce del cambio (solo lsB).
	RPM_RED_MARK, // 2-3: regime red mark min 8500 (stessa scala del regime limitatore, il motore si rompe quando regime >= regime red mark per piu' di qualche secondo; valori <8500 sono considerati =8500).
	RPM_LIMIT, // 4-5: regime limitatore, max 14335 (il contagiri si blocca ma la moto conserva le prestazioni di accelerazione a quel regime, quindi la coppia a quel regime deve essere azzerata se si vuole interrompere l'accelerazione della moto).
	OVERSPEED_GRACE_PERIOD, // 6-7: periodo di grazia su red mark (valore alto: il motore si rompe dopo piu' tempo. Per valori msB >=0x80 si rompe subito).
	GRIP, // 8-9: soglia di slittamento in sterzata (valore alto: slitta meno).
	UNKNOWN_1, // 10-11: ?
	BRAKING_SPEED, // 12-13: velocita' di frenata.
	UNKNOWN_2, // 14-15: ?
	SPIN_THRESHOLD, // 16-17: soglia di testacoda (valore basso: testacoda piu' probabile. Per valori msB >=0x80 testacoda sicuro).
	UNKNOWN_3, // 18-19: ?
	RPM_DOWNSHIFT; // 20-21: regime di scalata con cambio automatico (skill < 3).
	
	@Override
	public String toString() {
		return StringUtils.uncapitalize( WordUtils.capitalizeFully( this.name(), new char[] {'_'} ).replace( "_", "" ) );
	}
	
	public static Setting getSetting( String name ) {
		for ( Setting setting : Setting.values() ) {
			if ( setting.toString().equalsIgnoreCase( name ) ) {
				return setting;
			}
		}
		return null;
	}
	
}