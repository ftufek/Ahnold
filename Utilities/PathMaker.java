package Utilities;

import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;

/**
 * A helper class which generates different Path.
 * 
 * @author FT
 *
 */
public class PathMaker {
	
	/**
	 * Create path by using the tile numbers.
	 * 
	 * @param tiles
	 * @return a path representing the tile numbers in (x,y) coordinates
	 */
	public static Path createPath(double[][] tiles){
		Path p = new Path();
		for(double[] tile : tiles){
			p.add(new Waypoint(tile[0]*Constants.TILE_SIDE_LENGTH, tile[1]*Constants.TILE_SIDE_LENGTH));
		}
		return p;
	}
	
	//Used for testing
	public static Path dummyPath(){
		double[][] t = {
				{2,2},
				{3,5},
				{1,7},
				{4,6},
				{5,5},
				{2,3},
				{5,1},
				{6,3},
				{5,7},
				{7,7}
		};
		
		return createPath(t);
	}
	
	//For testing
	public static Path originalDummyPath(){
		double[][] t = {
				{2,2},
				{2,0},
				{0,2},
				{0,0}
		};
		
		return createPath(t);
	}
	
	/**
	 * Generate the path used for searching light in
	 * attacker mode.
	 * 
	 * @return search path
	 */
	public static Path lightScanPath(){
		double[][] p = {
				{1,1},
				{4,2},
				{2,4},
				{1,5},
				{2,7},
				{0.5,9},
				{6,9},
				{5,6},
				{6,4},
				{5,1},
				{8,2},
				{9,4},
				{8,6},
				{9,9}
		};
		return createPath(p);
	}
}
