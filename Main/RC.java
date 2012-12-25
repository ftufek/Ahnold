package Main;

import lejos.nxt.LightSensor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.remote.RemoteMotor;

/**
 * RC is shorthand for RobotComponents.
 * 
 * It's just holds static reference to the robot's
 * components (such as motors and sensors) so that
 * different parts of the code can access them easily.
 * 
 * @author FT
 *
 */
public class RC {
	public static NXTRegulatedMotor motorLeft;
	public static NXTRegulatedMotor motorRight;
	public static NXTRegulatedMotor motorLift;
	public static RemoteMotor motorClaw;
	public static UltrasonicSensor usLeft;
	public static UltrasonicSensor usCenter;
	public static LightSensor lsBack;
	public static LightSensor lsLocalization;
	public static LightSensor lsRight;
	public static RemoteMotor motorRightRotator;
	
	private static int LIFT_MOTOR_SPEED = 100;
	private static double AVERAGE_AMBIENT_LIGHT = 0;
	
	//beaconGrabbed is used to keep track if the GrabBeacon
	//behavior grabbed the beacon. We know that it's not good to
	//put it here, but we needed to do it quickly for the final demo.
	public static boolean beaconGrabbed;
	
	public static int lsBack(){
		return lsBack.getLightValue();
	}
	
	public static int lsRight(){
		return lsRight.getLightValue();
	}
	
	/**
	 * Take the average of the ambient light so that
	 * we can compare later with it's value to know if
	 * we're detecting the Beacon.
	 * @param r
	 */
	public static void setAmbientLight(Robot r){
		r.stop();
		r.setRotateSpeed(25);
		r.rotate(360, true);
		
		double total = 0;
		int n = 0;
		
		while(r.isMoving()){
			total += RC.lsBack();			
			n++;
		}
		AVERAGE_AMBIENT_LIGHT = total / n;
	}
	
	public static boolean isCloseToLight(){
		return (RC.lsBack() - AVERAGE_AMBIENT_LIGHT) > 7;
	}

	public static void setRotatorAngle(int angle){
		boolean result = false;
		while(!result){
			try{
				motorRightRotator.setSpeed(60);
				motorRightRotator.rotateTo(-angle);
				result = true;
			}catch(Exception e){
				result = false;
			}
		}
	}
	
	public static void liftClaw() {
		claw(true);
		lift(true);
	}	
	
	public static boolean isDetectingLight(){
		boolean detecting = false;
		try{
			detecting = lsRight() - AVERAGE_AMBIENT_LIGHT > 5 || lsBack() - AVERAGE_AMBIENT_LIGHT > 5;
		}catch(Exception e){
		}
		return detecting;
	}
	
	public static boolean isDetectingWall(){
		return usCenter() < 35;
	}
	
	public static int usCenter(){
		return usCenter.getDistance();
	}
	
	public static void setUSContinous(){
		usCenter.continuous();
		usLeft.continuous();
	}
	
	public static int usLeft() {
		return usLeft.getDistance();
	}
	
	public static boolean detectWallLeft() {
		return usLeft.getDistance() < 25;
	}
	
	public static void lift(boolean l){
		boolean result = false;
		while(!result){
			try{
				if(l){
					//lift
					motorLift.setSpeed(LIFT_MOTOR_SPEED);
					motorLift.rotateTo(680);
				}else{
					//delift
					motorLift.setSpeed(LIFT_MOTOR_SPEED);
					motorLift.rotateTo(0);
				}
				result = true;
			}catch(Exception e){
				result = false;
			}
			
		}
	}
	
	public static void claw(boolean c){
		boolean result = false;
		while(!result){
			try{
				motorClaw.setSpeed(LIFT_MOTOR_SPEED);
				if(c){
					motorClaw.rotateTo(-10);
				} else{ 
					motorClaw.rotateTo(35);
				}
				result = true;
			}catch(Exception e){
				result = false;
			}
		}
	}
}
