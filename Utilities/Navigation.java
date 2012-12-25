package Utilities;

import lejos.robotics.navigation.Navigator;
import Main.Robot;

/**
 * Navigation is used for simplifying Robot navigation.
 * It extends the Navigator class in lejos.
 * 
 * For example we can set a Path using .setPath(Path p)
 * and than just say .followPath()
 * @author FT
 *
 */
public class Navigation extends Navigator{
	public Navigation(Robot r, Odometer odo) {
		super(r, odo);
	}
}
