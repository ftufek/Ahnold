package Behaviors;

import lejos.geom.Point;
import lejos.nxt.Sound;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;
import Main.RC;
import Main.Robot;
import Utilities.Constants;
import Utilities.Navigation;

/**
 * It's a Behavior and is managed by the main Arbitrator (which is like
 * the brain who decides which behavior is more important and which one
 * should be executing).
 * 
 * The logic behind our obstacle avoidance is that when we detect
 * a wall we turn right or left by 100 degrees and travel forward
 * by 50 unless we detect a wall. Than we turn right or left by
 * 90 degrees and go forward 30. We decide to turn right or left
 * based on the direction that we can get to the center of the maze
 * more quickly (we are directing towards the center of the maze,
 * because in worst case, we are assuming that we would get to the
 * beacon more easily from the center...)
 * 
 * We recognize that this is a very basic algorithm, but since
 * we were told that there won't be too much obstacles on the maze,
 * we hope that it would work correctly. 
 * 
 * @author FT
 *
 */
public class AvoidObstacle implements Behavior{
	boolean suppressed;
	private Navigation n;
	private Robot r;
	
	private boolean isAttacker;

	private static int CENTER_POINT = (int) (5 * Constants.TILE_SIDE_LENGTH);
	
	private static int filterControl = 0;
	private static final int FILTER_OUT = 30;
	
	public AvoidObstacle(Robot r, Navigation n, boolean isAttacker) {
		this.n = n;
		this.r = r;
		this.isAttacker = isAttacker;
	}
	
	/**
	 * Decide that this behavior takes the control when the Robot detects a wall.
	 */
	@Override
	public boolean takeControl() {
		return RC.isDetectingWall();
	}
	
	/**
	 * Runs the obstacle avoidance logic.
	 */
	@Override
	public void action() {
		Sound.beep();
		suppressed = false;
		
		//Find out if we should turn left or turn right
		//in order to face towards the center of the maze
		Pose p = n.getPoseProvider().getPose();
		boolean turnLeft = false;
		double toAngle = n.getPoseProvider().getPose().angleTo(new Point(CENTER_POINT, CENTER_POINT));		
		float head = p.getHeading();
	    double diff = toAngle - head;
	    while(diff > 180) diff = diff - 360;
	    while(diff < -180) diff = diff + 360;	    
	    if(diff >= 0)turnLeft = true;
	    else turnLeft = false;
	    
	    // Positive rotation is anti-clockwise (left)
	    // Negative rotation is clockwise (right)
	    
	    if(turnLeft){
	    	r.rotate(100);
	    }else{
	    	r.rotate(-100);
	    }
	    
	    r.stop();
	    r.setTravelSpeed(10);
	    r.travel(50, true);
	    while(RC.usCenter() > 20 && r.isMoving()){
	    	Thread.yield();
	    }
	    
	    if(turnLeft){
	    	//if initially we turned left, turn right
	    	//to get back to (approximately) the angle before
	    	//wall detection
	    	r.rotate(-90);
	    }else{
	    	//else turn left
	    	r.rotate(90);
	    }
	    
	    //go forward little bit to make sure that we passed
	    //the block
	    r.travel(30,true);
	    while(RC.usCenter() > 20 && r.isMoving()){
	    	try{Thread.sleep(100);}catch(Exception e){};
	    }
	    
	    if(isAttacker){
	    	//remove the next point of search path when a light is detected
	    	n.getPath().remove(0);
	    }
	    
	    r.stop();
	    Sound.beep();
	}
	
	/**
	 * Basic UltraSonic Sensor filtering
	 * @return UltraSonic reading after filter was applied
	 */
	public double getFilteredData(){
		double distance = RC.usCenter();
		if (distance > 60 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
			distance = 1;
		} else if (distance > 60){
			// true 255, therefore set distance to 255
			distance = 255;
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
		}
		return distance;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}
