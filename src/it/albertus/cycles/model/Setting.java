package it.albertus.cycles.model;

public enum Setting { // The order matters.
	
	gearsCount			 (0), // 0-1: numero di marce del cambio (solo lsB).
	rpmRedline			 (1), // 2-3: regime redline, min 8500 (stessa scala del regime limitatore, il motore si rompe quando regime >= regime red mark per piu' di qualche secondo; valori <8500 sono considerati =8500).
	rpmLimit			 (2), // 4-5: regime limitatore, max 14335 (il contagiri si blocca ma la moto conserva le prestazioni di accelerazione a quel regime, quindi la coppia a quel regime deve essere azzerata se si vuole interrompere l'accelerazione della moto).
	overspeedGracePeriod (3), // 6-7: periodo di grazia su red mark (valore alto: il motore si rompe dopo piu' tempo. Per valori msB >=0x80 si rompe subito).
	grip				 (4), // 8-9: soglia di slittamento in sterzata (valore alto: slitta meno).
	unknown1			 (5), // 10-11: ?
	brakingSpeed		 (6), // 12-13: velocita' di frenata.
	unknown2			 (7), // 14-15: ?
	spinThreshold		 (8), // 16-17: soglia di testacoda (valore basso: testacoda piu' probabile. Per valori msB >=0x80 testacoda sicuro).
	unknown3			 (9), // 18-19: ?
	rpmDownshift		(10); // 20-21: regime di scalata con cambio automatico (skill < 3).
	
	private int index;

	private Setting(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public static Setting getSetting(String name) {
		for (Setting setting : Setting.values()) {
			if (setting.toString().equalsIgnoreCase(name)) { // Case insensitive.
				return setting;
			}
		}
		return null;
	}

	public static Setting getSetting(int index) {
		for (Setting setting : Setting.values()) {
			if (setting.getIndex() == index) {
				return setting;
			}
		}
		return null;
	}

}