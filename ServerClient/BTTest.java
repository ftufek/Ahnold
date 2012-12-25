/*
* @author Sean Lawlor, Stepan Salenikovich
* @date November 3, 2011
* @class ECSE 211 - Design Principle and Methods
*/
import lejos.nxt.Button;
import lejos.nxt.LCD;
import bluetooth.*;

public class BTTest {
	// example call of the transmission protocol
	// The print function is just for debugging to make sure data is received correctly
	// make sure to import the bluetooth.BluetoothConnection library
	@SuppressWarnings("unused")
	public static void main(String [] args) {
		BluetoothConnection conn = new BluetoothConnection();
		// as of this point the bluetooth connection is closed again, and you can pair to another NXT (or PC) if you wish
		
		// example usage of Tranmission class
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			StartCorner corner = t.startingCorner;
			PlayerRole role = t.role;
			//defender will go here to get the flag:
			int fx = t.fx;	//flag pos x
			int fy = t.fy;	//flag pos y
			
			// attacker will drop the flag off here
			int dx = t.dx;	//destination pos x
			int dy = t.dy;	//destination pos y
			
			// print out the transmission information to the LCD
			conn.printTransmission();
		}
		// stall until user decides to end program
		Button.waitForAnyPress();
	}
}
