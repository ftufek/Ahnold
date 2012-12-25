package Utilities;
import lejos.nxt.LightSensor;
import lejos.nxt.Sound;
import lejos.robotics.navigation.Pose;
import Main.RC;
import Main.Robot;

/**
 * LightLocalizer based on the gridlines on the floor.
 * 
 * @author FT
 *
 */
public class LightLocalizer {
	private Odometer odo;
	private Robot robot;
	private LightSensor ls;
	
	private double DISTANCE_CENTER_LIGHTSENSOR = 14;
	
	public LightLocalizer(Robot r, Odometer odo) {
		this.odo = odo;
		this.robot = r;
		this.ls = RC.lsLocalization;
		
		// turn on the light
		ls.setFloodlight(true);
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		
		double[] angles = {0,0,0,0};
		robot.setRotateSpeed(30);
		robot.rotate(360, true);
		for(int i = 0; i < 4; i++){
			//we want to take 4 angles
			double angleStart = -1, angleEnd = -1;
			boolean lineDetected = false;
			while(true){
				if(!isDetectingLine() && lineDetected) break;
				if(isDetectingLine()){
					Sound.beep();
					lineDetected = true;
					if(angleStart == -1) angleStart = odo.getPoseArnold().getHeading();
					angleEnd = odo.getPoseArnold().getHeading();
				}
			}
			//we want to average the beginning angle and end angle
			//if they are close (they didn't go over 360 to overlap back to 0...)
			if(Math.abs(angleEnd - angleStart) < 10){
				angles[i] = (angleEnd + angleStart)/2;
			}else{
				//otherwise we can just return one of the angles
				angles[i] = angleEnd;
			}
		}
		
		double thetaXN = angles[0]; //x-
		double thetaYP = angles[1]; //y+
		double thetaXP = angles[2]; //x+
		double thetaYN = angles[3]; //y-
		
		double x = -(DISTANCE_CENTER_LIGHTSENSOR*Math.cos(Math.toRadians(angleDiffFromTo(thetaYP, thetaYN)/2)));
		double y = -(DISTANCE_CENTER_LIGHTSENSOR*Math.cos(Math.toRadians(angleDiffFromTo(thetaXN, thetaXP)/2)));
		
		double dTheta = 90 + angleDiffFromTo(thetaYP, thetaYN)/2-(thetaYN-180);
		double theta = odo.getPoseArnold().getHeading() + dTheta;
		
		odo.setPose(new Pose((float)x, (float)y, (float)theta));
	}
	
	private double angleDiffFromTo(double a, double b){
		//we know that b should be bigger than a
		//and also it's increasing clockwise
		if(b < a) return b + (360-a);
		return b-a;
	}
	
	protected boolean isDetectingLine(){
		if(ls.readNormalizedValue() < 450) return true;
		return false;
	}

}
