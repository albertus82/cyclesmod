package io.github.albertus82.cyclesmod.gui.torquegraph;

import static io.github.albertus82.cyclesmod.gui.torquegraph.BasicTorqueGraph.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TorqueGraphTest {

	@Test
	void testTorqueToPower() {
		Assertions.assertEquals(0, torqueToPower(255, 0), 0.01);
		Assertions.assertEquals(63.75, torqueToPower(255, 3552), 0.01);
		Assertions.assertEquals(127.5, torqueToPower(255, 7104), 0.01);
		Assertions.assertEquals(191.25, torqueToPower(255, 10656), 0.01);
		Assertions.assertEquals(255, torqueToPower(255, 14208), 0.01);

		Assertions.assertEquals(0, torqueToPower(128, 0), 0.01);
		Assertions.assertEquals(32, torqueToPower(128, 3552), 0.01);
		Assertions.assertEquals(64, torqueToPower(128, 7104), 0.01);
		Assertions.assertEquals(96, torqueToPower(128, 10656), 0.01);
		Assertions.assertEquals(128, torqueToPower(128, 14208), 0.01);
	}

}
