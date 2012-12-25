package Main;

import java.io.IOException;

import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.remote.RemoteNXT;

/**
 * This class is used for accessing Motors and Sensors on
 * the slave brick. It uses RemoteNXT for simplifying the
 * communication.
 * 
 * @author FT
 *
 */
public class SlaveBrick {
	private String slaveName = "BigBlue";
	private RemoteNXT nxt;
	
	public boolean connected = false;
	
	public SlaveBrick(){
		NXTCommConnector connector = Bluetooth.getConnector();
		try{
			nxt = new RemoteNXT(slaveName, connector);
			
			//Setup references in RobotComponents
			RC.usLeft = new UltrasonicSensor(nxt.S1);
			RC.motorClaw = nxt.A;
			RC.motorRightRotator = nxt.B;
			
			connected = true;
		}catch(IOException e){
			System.out.println("Couldn't connect to slave!");
		}
	}
	
	public void waitForConnection(){
		while(!connected);
	}

}
