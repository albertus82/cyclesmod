package com.github.albertus82.cyclesmod.gui.powergraph;

import static com.github.albertus82.cyclesmod.gui.powergraph.PowerGraph.hpToNm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PowerGraphTest {

	@Test
	void testHpToNm() {
		Assertions.assertEquals(71.21, hpToNm(10, 1000), 0.01);
		Assertions.assertEquals(35.60, hpToNm(10, 2000), 0.01);
		Assertions.assertEquals(23.74, hpToNm(10, 3000), 0.01);
		Assertions.assertEquals(89.01, hpToNm(50, 4000), 0.01);
		Assertions.assertEquals(56.96, hpToNm(80, 10000), 0.01);
		Assertions.assertEquals(70.95, hpToNm(123, 12345), 0.01);
		Assertions.assertEquals(712054016d, hpToNm(100000d, 1), 0);
	}

}
