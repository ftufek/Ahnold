package Behaviors;

import lejos.nxt.Sound;
import lejos.robotics.subsumption.Behavior;
import Main.RC;
import Main.Robot;
import Utilities.Navigation;
import Utilities.Odometer;
import Utilities.SensorRotator;

/**
 * Behavior that's responsible for grabbing the
 * beacon when a light is detected.
 * 
 * It's used by the attacker logic.
 * 
 * @author FT
 *
 */
public class GrabBeacon implements Behavior {
	private Robot r;
	private Odometer odo;
	private Navigation n;
	private SensorRotator rotator;
	
	public GrabBeacon(Robot r, Odometer odo, Navigation n, SensorRotator rotator){
		this.r = r;
		this.odo = odo;
		this.n = n;
		this.rotator = rotator;
	}

	/**
	 * Decide that the behavior takes control when a light
	 * is detected.
	 */
	@Override
	public boolean takeControl() {
		return RC.isDetectingLight();
	}

	/**
	 * Execute GrabBeacon logic.
	 */
	@Override
	public void action() {
		Sound.beep();
		RC.lsBack.setFloodlight(false); //helps when detecting the beacon
		r.stop();
		rotator.disable();
		RC.setRotatorAngle(45); //make sure that the rotator is out of the way

		r.setRotateSpeed(20);
		r.setTravelSpeed(15);
		
		double angle = findBrightestAngle(360);
		n.rotateTo(angle);		
		
		while(!RC.isCloseToLight()) {
			//need to travel back because we're localizing with LightSensor
			//on the back of the Robot
			r.travel(-15); 
			r.rotate(-30);
			angle = findBrightestAngle(70);
			n.rotateTo(angle);
			r.stop();
			
			//If initially the robot thinks that it detected
			//the light, but after searching for it, it stopped
			//detecting it, just quit the behavior
			if(!RC.isDetectingLight())return;
		}
		Sound.buzz();
		
		//Rotate to bring the claw in direction of the beacon
		//bring down and open the claw
		r.rotate(170);
		RC.lift(false);
		RC.claw(false);
		
		//The robot has approximately found the light, it must now approach it slowly and precisely
		r.travel(20);
		while(RC.usCenter() > 10){
			r.forward();
			r.setTravelSpeed(5);
		}
		
		//Just shake left and right little bit 
		//to improve the position of the beacon inside the claw
		r.rotate(20);
		r.rotate(-20);
		r.travel(10);
		
		//Close the claw and lift
		RC.claw(true);
		RC.lift(true);
		
		//we also need to keep track of grabbing outside
		//so that other behaviors can take it into account
		RC.beaconGrabbed = true;
		Sound.beep();
	}

	@Override
	public void suppress() {
	}
	
	/**
	 * Turns by rotateAngle degrees and returns the angle
	 * at which the light was the brightest.
	 * 
	 * @param rotateAngle
	 * @return angle that has the brightest light
	 */
	public double findBrightestAngle(int rotateAngle){
		r.stop();
		r.setRotateSpeed(35);
		double angle = odo.getPoseArnold().getHeading();
		double oldSpeed = r.getRotateSpeed();
		r.setRotateSpeed(10);
		r.rotate(rotateAngle, true);
		double maxLightValue = -1;
		while(RC.motorLeft.isMoving()){
			double l;
			if((l = RC.lsBack()) > maxLightValue){
				maxLightValue = l;
				angle = odo.getPoseArnold().getHeading();
			}
		}
		r.setRotateSpeed(oldSpeed);
		return angle;
	}

}
