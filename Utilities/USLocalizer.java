package Utilities;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.navigation.Pose;
import Main.RC;
import Main.Robot;

/**
 * UltraSonic Localizer, code is mostly imported from
 * our lab codes, just modified to make it compatible
 * with the new OdometryPoseProvider from lejos.
 * @author FT
 *
 */
public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 30;
	
	private double WALL_DISTANCE_CONSTANT = 40;
	private double NOISE_MARGIN = 1;
	private double NOISE_MARGIN_START = WALL_DISTANCE_CONSTANT + NOISE_MARGIN;
	private double NOISE_MARGIN_END = WALL_DISTANCE_CONSTANT - NOISE_MARGIN;

	private Odometer odo;
	private Robot robot;
	private UltrasonicSensor us;
	
	//filter constants for getFilteredData
	private int filterCount;
	private int FILTER_LIMIT = 10;
	
	public USLocalizer(Robot r, Odometer odo) {
		this.odo = odo;
		this.robot = r;
		this.us = RC.usCenter;
		this.filterCount = 0;
		
		// switch off the ultrasonic sensor
		us.off();
	}
	
	public void doLocalization() {
		double angleA, angleB;
		robot.setRotateSpeed(30);
		odo.setPose(new Pose(0, 0, 0));
		// rotate the robot until it sees no wall
		robot.stop();
		robot.rotate(360, true);
		while(getFilteredData() < 250)Thread.yield();
		robot.stop();
		
		// keep rotating until the robot sees a wall, then latch the angle
		// let's start by trying to detect left wall first
		double angleStart = -1, angleEnd = -1;
		double dist = -1;
		while ((dist = getFilteredData()) > NOISE_MARGIN_END) {
			robot.rotate(-30, true);
			if(dist < NOISE_MARGIN_START && angleStart == -1){
				angleStart = odo.getPoseArnold().getHeading();
			}
		}
		robot.stop();
		angleEnd = odo.getPoseArnold().getHeading();
		angleB = angleEnd;
		
		//switch direction and wait until it sees no wall
		angleStart = -1;
		angleEnd = -1;
		robot.rotate(20);
		while(robot.isMoving())Thread.yield();
		robot.stop();
		robot.rotate(360, true);
		
		// keep rotating until the robot sees a wall, then latch the angle
		while((dist=getFilteredData()) > NOISE_MARGIN_END){
			if(dist<NOISE_MARGIN_START && angleStart == -1){
				angleStart = odo.getPoseArnold().getHeading();
			}
		}
		robot.stop();
		angleEnd = odo.getPoseArnold().getHeading();
		angleA = angleEnd;
		
		// angleA is clockwise from angleB, so assume the average of the
		// angles to the right of angleB is 45 degrees past 'north'
		double dA = 45 - (angleA+angleB)/2;
		
		// update the odometer position
		//we use -15 and -15 as the x and y initial positions, because we know approximately
		//that we will be in the center of the tile at the beginning, it worked correctly
		//during our tests so we kept it
		odo.setPose(new Pose((float)-15,(float)-15, (float)(odo.getPoseArnold().getHeading()+dA)));
		
	}
	
	private int getFilteredData() {
		int distance;
		
		// do a ping
		us.ping();
		
		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}
		
		// there will be a delay here
		distance = us.getDistance();
		
		if(distance > 60 && filterCount < FILTER_LIMIT){
			filterCount++;
			distance = 50;
		}else if(distance < 60){
			filterCount = 0;
		}
				
		return distance;
	}

}
