package Utilities;

import lejos.util.Timer;
import lejos.util.TimerListener;
import Main.RC;

/**
 * SensorRotator rotates our sensors that are mounted
 * on the right rotator motor.
 * 
 * @author FT
 *
 */
public class SensorRotator implements TimerListener{
	private boolean enRotate;
	private Timer t = new Timer(100, this);
	
	private int mode = 0;
	
	public SensorRotator(){
		enRotate = true;
		t.start();
	}
	
	//the outer and inner angles that both motors should be alternating between  
	private int outAngle = 45;
	private int inAngle = -45;
	
	@Override
	public void timedOut() {
		if(enRotate){
			if (mode == 0){
				RC.setRotatorAngle(outAngle);
				mode = 1;
			}
			if (mode == 1){
				RC.setRotatorAngle(inAngle);
				mode = 0;
			}
		}
	}
	
	/**
	 * Enables the motor rotator
	 */
	public void enable(){
		enRotate = true;
	}
	
	/**
	 * Disable the motor rotator
	 */
	public void disable(){
		t.stop();
		enRotate = false;
		RC.setRotatorAngle(0);
		while(RC.motorRightRotator.isMoving())Thread.yield();
	}
}
