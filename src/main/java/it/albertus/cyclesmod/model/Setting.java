package it.albertus.cyclesmod.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Setting { // The order matters.

	GEARS_COUNT(0, "gearsCount"), // 0-1: numero di marce del cambio (solo lsB).
	RPM_REDLINE(1, "rpmRedline"), // 2-3: regime redline, min 8500 (stessa scala del regime limitatore, il motore si rompe quando regime >= regime red mark per piu' di qualche secondo; valori <8500 sono considerati =8500).
	RPM_LIMIT(2, "rpmLimit"), // 4-5: regime limitatore, max 14335 (il contagiri si blocca ma la moto conserva le prestazioni di accelerazione a quel regime, quindi la potenza a quel regime deve essere azzerata se si vuole interrompere l'accelerazione della moto).
	OVERSPEED_GRACE_PERIOD(3, "overspeedGracePeriod"), // 6-7: periodo di grazia su red mark (valore alto: il motore si rompe dopo piu' tempo. Per valori msB >=0x80 si rompe subito).
	GRIP(4, "grip"), // 8-9: soglia di slittamento in sterzata (valore alto: slitta meno).
	UNKNOWN_1(5, "unknown1"), // 10-11: ?
	BRAKING_SPEED(6, "brakingSpeed"), // 12-13: velocita' di frenata.
	UNKNOWN_2(7, "unknown2"), // 14-15: ?
	SPIN_THRESHOLD(8, "spinThreshold"), // 16-17: soglia di testacoda (valore basso: testacoda piu' probabile. Per valori msB >=0x80 testacoda sicuro).
	UNKNOWN_3(9, "unknown3"), // 18-19: ?
	RPM_DOWNSHIFT(10, "rpmDownshift"); // 20-21: regime di scalata con cambio automatico (skill < 3).

	private final int index;
	private final String key;

	public static Setting forKey(final String name) {
		for (final Setting setting : Setting.values()) {
			if (setting.getKey().equalsIgnoreCase(name)) { // Case insensitive.
				return setting;
			}
		}
		return null;
	}

	public static Setting forIndex(final int index) {
		for (final Setting setting : Setting.values()) {
			if (setting.getIndex() == index) {
				return setting;
			}
		}
		return null;
	}

}
