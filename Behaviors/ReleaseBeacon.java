package Behaviors;

import lejos.robotics.subsumption.Behavior;
import Main.RC;
import Main.Robot;

/**
 * Behavior that releases the Beacon.
 * 
 * @author FT
 *
 */
public class ReleaseBeacon implements Behavior{
	private Robot r;
	
	boolean released = false;

	public ReleaseBeacon(Robot r){
		this.r = r;
	}
	
	/**
	 * It takes control only if it hasn't already released
	 * the beacon.
	 */
	@Override
	public boolean takeControl() {
		return !released;
	}

	/**
	 * Runs the behavior to release the beacon.
	 */
	@Override
	public void action() {
		//Bring the claw down and open it
		RC.lift(false);
		RC.claw(false);
		
		//Go back to leave the Beacon there
		r.travel(-20);
		
		//Bring the claw up
		RC.lift(true);
		RC.claw(true);	

		released = true;
	}

	@Override
	public void suppress() {
		
	}

}
