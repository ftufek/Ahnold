ECSE 211 dpm - Fall 2012 Final Project BT Server & Client doc

Definitions:

[A]ttacker and [D]efender
	* roles as defined in the project spec.

Starting Corner:
	* values - 1, 2, 3, or 4
	* 1 - bottom left (0,0) - "X-1" in the spec
	* 2 - top left (0,10) - "X-2" in the spec
	* 3 - top right (10,10) - "X-3" in the spec
	* 4 - bottom right (10,0) - "X-4" in the spec
	
D flag X (fx):
	* starting X tile location of the beacon.

D flag Y (fx):
	* starting Y tile location of the beacon.

A destination X (dx):
	* X tile location of the destination where the A must bring the beacon.

A destination Y (dy):
	* Y tile location of the destination where the A must bring the beacon.

-------------------------
        BT Server
-------------------------
Simply run the "ProjectF12Server.jar" to start the BT server.
Make sure BT is enabled on your computer.  If you have not paired your NXT
to the computer you are running the server on, it will prompt you to do so.
Make sure you know the pin of the NXT.

Connect to:
Select if you want it to send to both Defender and Attacker, or just one of them.
If you select both, it will first try to pair with both, before sending the data,
so both Defender and Attacker must be running the BT client.

NOTE: The server currently crashes on Mac - OSX for me, this will be fixed, if possible

-------------------------
        BT Client
-------------------------
Simply add the "ProjectF12Client.jar" to your Eclipse project (or your build script, if not using Eclipse) and import "bluetooth.*".

Tested with both lejos 9.0 and 9.1

See "BTTest.java" for an example of how to use the BluetoothConnection and Transmission classes.

Also see the javadoc for some more info.