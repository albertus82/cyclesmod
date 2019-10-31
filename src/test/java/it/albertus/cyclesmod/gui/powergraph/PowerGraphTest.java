package it.albertus.cyclesmod.gui.powergraph;

import static it.albertus.cyclesmod.gui.powergraph.PowerGraph.hpToNm;

import org.junit.Assert;
import org.junit.Test;

public class PowerGraphTest {

	@Test
	public void testHpToNm() {
		Assert.assertEquals(71.21, hpToNm(10, 1000), 0.01);
		Assert.assertEquals(35.60, hpToNm(10, 2000), 0.01);
		Assert.assertEquals(23.74, hpToNm(10, 3000), 0.01);
		Assert.assertEquals(89.01, hpToNm(50, 4000), 0.01);
		Assert.assertEquals(56.96, hpToNm(80, 10000), 0.01);
		Assert.assertEquals(70.95, hpToNm(123, 12345), 0.01);
		Assert.assertEquals(712054016d, hpToNm(100000d, 1), 0);
	}

}
