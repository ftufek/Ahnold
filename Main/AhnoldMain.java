package Main;

import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.util.Delay;
import Behaviors.AvoidObstacle;
import Behaviors.GrabBeacon;
import Behaviors.Navigate;
import Behaviors.ReleaseBeacon;
import Communication.CommandReceiver;
import Communication.PlayerRole;
import Communication.StartCorner;
import Communication.Transmission;
import Utilities.Constants;
import Utilities.LCDInfo;
import Utilities.LightLocalizer;
import Utilities.Navigation;
import Utilities.Odometer;
import Utilities.PathMaker;
import Utilities.SensorRotator;
import Utilities.USLocalizer;

/**
 * Robot's main Class.
 * 
 * Our robot name is Ahnold!
 * 
 * @author FT
 *
 */
public class AhnoldMain {
	
	/**
	 * Main program entry. 
	 * 
	 * This is where the program execution starts
	 * when the program it's launched on the NXT.
	 */
	public static void main(String[] args) {
		Transmission t = CommandReceiver.getTransmission();
		
		if(t.role == PlayerRole.ATTACKER){
			Delay.msDelay(5000*60);
		}
		
		initRobotComponents();
		
		//At one point, we had to change our odometer from
		//considering clockwise rotation positive to considering
		//anti-clockwise rotation positive(switch to lejos OdometryPoseProvider). 
		//Since our localization
		//code assumed the inverse, at the beginning, we tell our robot
		//to use the left motor as being the right one and vice-versa
		//but we restore them right after the localization finishes
		Robot r = new Robot(RC.motorRight, RC.motorLeft);
		Odometer odo = new Odometer(r);
		SlaveBrick slaveBrick = initSlaveBrick();
		Navigation navigation = new Navigation(r,odo);
		
		@SuppressWarnings("unused")
		LCDInfo lcd = new LCDInfo(odo);
		
		RC.liftClaw();
		localize(t,r,navigation,odo);
				
		//Than we restore the motors back
		r = new Robot(RC.motorLeft, RC.motorRight);
		odo = new Odometer(r);
		navigation = new Navigation(r, odo);
		lcd = new LCDInfo(odo);
		
		updateOdometerCorner(odo, t.startingCorner);
		
		RC.setUSContinous();
		
		//Go away from the wall and take the average
		//ambient light value so that we can compare
		//light values with it to know if we detect the Beacon
		r.setRotateSpeed(20);
		r.rotate(-45);
		r.setTravelSpeed(15);
		r.travel(25);
		RC.setAmbientLight(r);
		
		//We decide which logic to execute
		//Our logic is based on Behavior programming explained at:
		//   http://lejos.sourceforge.net/nxt/nxj/tutorial/Behaviors/BehaviorProgramming.htm
		if(t.role == PlayerRole.ATTACKER){
			executeAttacker(r, slaveBrick, navigation, odo, t.startingCorner, t.dx, t.dy);
		}else{
			executeDefender(r, slaveBrick, navigation, odo, t.startingCorner, t.fx, t.fy);
		}
		
		Sound.beep();
		Button.waitForAnyPress();
	}
	
	/**
	 * Initialize robot components.
	 * 
	 * Setup the static constants in RC (RobotComponents)
	 * to reference the motors and sensors on the master brick.
	 */
	private static void initRobotComponents(){	
		RC.motorRight = Motor.A;
		RC.motorLeft = Motor.B;
		RC.motorLift = Motor.C;
		RC.usCenter = new UltrasonicSensor(SensorPort.S2);
		RC.lsRight = new LightSensor(SensorPort.S3);
		RC.lsLocalization = new LightSensor(SensorPort.S4);
		RC.lsBack = new LightSensor(SensorPort.S1);
		RC.lsBack.setFloodlight(true);
		RC.lsRight.setFloodlight(false);
	}
	
	/**
	 * Start the connection with Slave Brick.
	 */
	private static SlaveBrick initSlaveBrick(){
		SlaveBrick b = new SlaveBrick();
		b.waitForConnection();
		return b;
	}
	
	/**
	 * Run UltraSonic localization and LightSensor (reading lines
	 * on the ground) localization.
	 * 
	 * @param t
	 * @param r
	 * @param n
	 * @param odo
	 */
	private static void localize(Transmission t, Robot r, Navigation n, Odometer odo){
		//Slow down for extra precision
		r.setTravelSpeed(10);
		r.setRotateSpeed(20);
		
		USLocalizer usl = new USLocalizer(r, odo);
		usl.doLocalization();
		
		n.rotateTo(45);
		r.travel(7);
		while(r.isMoving())Thread.yield();
		
		LightLocalizer ls = new LightLocalizer(r, odo);
		ls.doLocalization();	
		
		//Go to (0,0) and look to Y positive
		n.goTo(0, 0);
		while(n.isMoving())Thread.yield();
		n.rotateTo(0);
		while(n.isMoving())Thread.yield();
		n.stop();
	}
	
	/**
	 * Updates the odometer value based on the corner that the robot
	 * started.
	 * @param odo
	 * @param corner
	 */
	private static void updateOdometerCorner(Odometer odo, StartCorner corner){
		Pose p = new Pose();
		p.setLocation((float)(corner.getX()*Constants.TILE_SIDE_LENGTH),(float)(corner.getY()*Constants.TILE_SIDE_LENGTH));
		
		if(corner == StartCorner.BOTTOM_LEFT){
			p.setHeading(90);
		}else if(corner == StartCorner.BOTTOM_RIGHT){
			p.setHeading(180);
		}else if(corner == StartCorner.TOP_LEFT){
			p.setHeading(0);
		}else if(corner == StartCorner.TOP_RIGHT){
			p.setHeading(270);
		}		
		odo.setPose(p);
	}
	
	/**
	 * Contains the code for executing defender logic.
	 * 
	 * Manages the Arbitrators and Behaviors (explained in further detail
	 * in Behaviors classes)
	 * @param r
	 * @param slave
	 * @param n
	 * @param odo
	 * @param corner
	 * @param x beacon's initial x position
	 * @param y beacon's initial y position
	 */
	private static void executeDefender(Robot r, SlaveBrick slave, Navigation n, Odometer odo, StartCorner corner, int x, int y){
		//Set robot speeds
		r.setRotateSpeed(30);
		r.setTravelSpeed(30);
		
		//
		//Setup behaviors
		//
		
		//Navigate behavior (the least important Behavior)
		Waypoint beaconPos = new Waypoint((x*Constants.TILE_SIDE_LENGTH)-20, (y*Constants.TILE_SIDE_LENGTH)-20);
		Waypoint startPos = new Waypoint(odo.getPose().getX(), odo.getPose().getY());
		Path path = new Path();
		path.add(beaconPos);
		Navigate navigate = new Navigate(n, path);
		
		//Obstacle avoidance (more important than navigate behavior)
		SensorRotator rotator = new SensorRotator();
		AvoidObstacle avoidObstacle = new AvoidObstacle(r,n,false);
		
		//Grab Beacon (the most important behavior, executed after navigation to
		// beacon position is finished)
		GrabBeacon grab = new GrabBeacon(r, odo, n, rotator);
		
		r.setTravelSpeed(15);
		r.setRotateSpeed(20);
		
		//1st arbitrator's goal: go to the beacon 
		Arbitrator defender = new Arbitrator(new Behavior[]{navigate, avoidObstacle},true);
		defender.start();
		r.stop();
		Sound.twoBeeps();
		
		//Extra logic part (we had to bypass the arbitrator): grab the beacon
		grab.action();
		Sound.twoBeeps();
		
		//2nd arbitrator's goal: go to the hiding position and release it
		path.clear();
		path.add(hidingPosition(corner));
		navigate = new Navigate(n, path);
		defender = new Arbitrator(new Behavior[]{new ReleaseBeacon(r),navigate,avoidObstacle}, true);
		defender.start();
		Sound.twoBeeps();
		
		//3rd arbitrator's goal: return to the beginning position
		path.clear();
		path.add(startPos);
		navigate = new Navigate(n, path);
		defender = new Arbitrator(new Behavior[]{navigate, avoidObstacle},true);
		defender.start();
		Sound.twoBeeps();
	}
	
	/**
	 * Returns a hiding position for the defender logic.
	 * 
	 * We didn't had time for finding a hiding algorithm,
	 * so we just hardcoded it to go to the center of the maze.
	 * @param corner
	 * @return a Waypoint representing the hiding position
	 */
	private static Waypoint hidingPosition(StartCorner corner){
		
		return new Waypoint(150,150);
	}
	
	/**
	 * Manage attacker's logic using Arbitrators and Behaviors.
	 * 
	 * @param r
	 * @param slave
	 * @param n
	 * @param odo
	 * @param corner
	 * @param x final destination x of beacon
	 * @param y final destination y of beacon
	 */
	private static void executeAttacker(Robot r, SlaveBrick slave, Navigation n, Odometer odo, StartCorner corner, int x, int y){
		Waypoint beaconPos = new Waypoint(x,y);
		Waypoint startPos = new Waypoint(odo.getPose().getX(), odo.getPose().getY());
		
		Path path = PathMaker.lightScanPath();
		Navigate navigateBeacon = new Navigate(n, path);
		
		SensorRotator rotator = new SensorRotator();
		GrabBeacon grab = new GrabBeacon(r, odo, n, rotator);
		
		//1st arbitrator's goal: search the beacon and grab it
		Arbitrator attacker = new Arbitrator(new Behavior[]{navigateBeacon, grab},true);
		attacker.start();		
		Sound.twoBeeps();
				
		//2nd arbitrator's goal: go to the destination and release beacon
		path.clear();
		path.add(beaconPos);
		Navigate navigate = new Navigate(n, path);
		attacker = new Arbitrator(new Behavior[]{navigate}, true);
		attacker.start();
		Sound.twoBeeps();
		
		//Bring down the claw and open it
		RC.lift(false);
		RC.claw(false);
		
		//Go back and lift the claw so that we can navigate without
		//running into blocks
		r.travel(-20);		
		RC.claw(true);
		RC.lift(true);
		Sound.twoBeeps();
		
		//3rd arbitrator's goal: return to the beginning position
		path.clear();
		path.add(startPos);
		navigate = new Navigate(n, path);
		attacker = new Arbitrator(new Behavior[]{navigate},true);
		attacker.start();		
	}

}
