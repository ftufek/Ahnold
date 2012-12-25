package Behaviors;

import lejos.robotics.pathfinding.Path;
import lejos.robotics.subsumption.Behavior;
import Utilities.Navigation;

/**
 * Navigation behavior.
 * 
 * Receives a Path as input and follows it until
 * the Path finishes.
 * 
 * It's usually the least important Behavior, so
 * it gets executed only if none of the more important
 * behaviors don't take control. An example of more important
 * behavior would be AvoidObstacle, so when an obstacle is
 * detected, the Navigate behavior is supressed, AvoidObstacle
 * takes control, runs it's actions than once it finishes we fall
 * back to Navigate behavior which continues it's path if it's
 * not already finished. If it's finished, we fallback to an
 * even less important behavior and if there's none left the
 * Arbitrator just finishes executing.
 * 
 * @author FT
 *
 */
public class Navigate implements Behavior{
	private Navigation n;
	private boolean supressed;

	public Navigate(Navigation n, Path p){
		this.n = n;
		n.setPath(p);
		supressed = false;
	}
	
	/**
	 * Decide that this behavior takes control only if we
	 * didn't already travel all the path.
	 */
	@Override
	public boolean takeControl() {
		return !n.pathCompleted();
	}

	/**
	 * Runs the behavior which follows the path unless supressed or finished.
	 */
	@Override
	public void action() {
		supressed = false;
		n.getMoveController().setTravelSpeed(20);
		n.followPath();
		while(!supressed && !n.pathCompleted()) {
			Thread.yield();
		}
		n.stop();
	}

	@Override
	public void suppress() {
		supressed = true;
	}

}
