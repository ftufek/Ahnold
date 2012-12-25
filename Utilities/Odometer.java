package Utilities;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Pose;
import Main.Robot;

/**
 * Keeps track of the Robot's position and heading.
 * Extends OdometryPoseProvider from lejos.
 * 
 * @author FT
 *
 */
public class Odometer extends OdometryPoseProvider{
	public Odometer(Robot r) {
		super(r);
		setPose(new Pose(0,0,0));
	}
	
	/**
	 * Had to make a modified getPose, because our initial
	 * code assumed that all the angles are positive(going from
	 * 0 to 360) but the code inside lejos assumes angles going
	 * from -180 to 180.
	 * @return
	 */
	public synchronized Pose getPoseArnold(){
		Pose p = getPose();
		if(p.getHeading() >= 0){
			return p;
		}else{
			float newHeading = p.getHeading()+360;
			return new Pose(p.getX(), p.getY(), newHeading);
		}
	}
}
