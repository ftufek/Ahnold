package Main;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

/**
 * Robot is used for accessing the robot's navigation
 * capabilities. It extends the DifferentialPilot class
 * which is in lejos and which already has all the
 * functionality we actually need.
 * @author FT
 *
 */
public class Robot extends DifferentialPilot{
	public static double WHEEL_DIAMETER = 5.25;
	public static double WHEEL_TO_WHEEL_DISTANCE = 18.84;
	
	public Robot(NXTRegulatedMotor mL, NXTRegulatedMotor mR){
		super(WHEEL_DIAMETER, WHEEL_TO_WHEEL_DISTANCE, mL, mR);
	}
}
