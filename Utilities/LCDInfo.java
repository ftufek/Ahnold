package Utilities;
import lejos.nxt.LCD;
import lejos.robotics.navigation.Pose;
import lejos.util.Timer;
import lejos.util.TimerListener;
import Main.RC;

/**
 * Displays the Odometer and sensors data on the LCD.
 * 
 * @author FT
 *
 */
public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;
	
	// arrays for displaying data
	private Pose pos;
	
	public LCDInfo(Odometer odo) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		
		pos = new Pose();
		
		// start the timer
		lcdTimer.start();
	}
	
	public void timedOut() {
		pos = odo.getPoseArnold();
		LCD.clear();
		LCD.drawString("X: "+pos.getX(), 0, 0);
		LCD.drawString("Y: "+pos.getY(), 0, 1);
		LCD.drawString("H: "+pos.getHeading(), 0, 2);
		LCD.drawString("usC: "+RC.usCenter(), 0, 5);
		LCD.drawString("lsB: "+RC.lsBack(), 0, 6);
		LCD.drawString("lsR: "+RC.lsRight(), 0, 7);
	}
}
