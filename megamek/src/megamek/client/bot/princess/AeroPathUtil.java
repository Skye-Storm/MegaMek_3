package megamek.client.bot.princess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import megamek.common.IAero;
import megamek.common.MovePath;
import megamek.common.UnitType;
import megamek.common.MovePath.MoveStepType;

/**
 * Helper class that contains functionality relating exclusively to aero unit paths.
 * @author NickAragua
 *
 */
public class AeroPathUtil 
{	
	/**
	 * Determines if the aircraft undertaking the given path will stall at the end of the turn. 
	 * Only relevant for aerodyne units 
	 * @param movePath the path to check
	 * @return whether the aircraft will stall at the end of the path
	 */
	public static boolean willStall(MovePath movePath) {
		// Stalling only happens in atmospheres on ground maps
		if(!movePath.isOnAtmosphericGroundMap()) {
			return false;
		}
		
		// aircraft that are not vtols or spheroids will stall if the final velocity is zero after all acc/dec
		// aerodyne units can actually land or "vertical land" and it's ok to do so (even though you're unlikely to find the 20 clear spaces)
		// spheroids will stall if they don't move or land
		
		boolean isAirborne = movePath.getEntity().isAirborne();
		boolean isSpheroid = UnitType.isSpheroidDropship(movePath.getEntity());
		
        if ((movePath.getFinalVelocity() == 0) && isAirborne && !isSpheroid) {
            return true;
        }
        
        if (isSpheroid && (movePath.getFinalNDown() == 0) 
        		&& (movePath.getMpUsed() == 0) 
        		&& !movePath.contains(MoveStepType.VLAND)) {
            return true;
        }
        
        return false;
	}
	
	/**
     * Determines if the aircraft undertaking the given path will become a lawn dart
     * @param movePath the path to check
     * @return True or false
     */
	public static boolean willCrash(MovePath movePath) {
		return movePath.getEntity().isAero() && 
		        (movePath.getFinalAltitude() < 1) && 
		        !movePath.contains(MoveStepType.VLAND) && 
		        !movePath.contains(MoveStepType.LAND);
	}
	
	/**
	 * A quick determination that checks the given path for the most common causes of a PSR and whether it leads us off board.
	 * The idea being that a safe path off board should not include any PSRs.
	 * @param movePath The path to check
	 * @return True or false
	 */
	public static boolean isSafePathOffBoard(MovePath movePath)	{
	    // common causes of PSR include, but are not limited to:
	    // stalling your aircraft
	    // crashing your aircraft into the ground
	    // executing maneuvers
	    // thrusting too hard 
	    // see your doctor if you experience any of these symptoms as it may lead to your aircraft transforming into a lawn dart
		return !willStall(movePath) && !willCrash(movePath) && movePath.fliesOffBoard() && !movePath.contains(MoveStepType.MANEUVER) &&
		        (movePath.getMpUsed() <= movePath.getEntity().getWalkMP()) && 
		        (movePath.getEntity().isAero() && (movePath.getMpUsed() <= ((IAero) movePath.getEntity()).getSI()));
	}

	/**
     * Generates paths that begin with all valid acceleration sequences for this aircraft.
     * @param startingPath The initial path, hopefully empty.
     * @return The child paths with all the accelerations this unit possibly can undertake.
     */
    public static Collection<MovePath> generateValidAccelerations(MovePath startingPath, int lowerBound, int upperBound) {
        Collection<MovePath> paths = new ArrayList<MovePath>();
        
        // sanity check: if we've already done something else with the path, there's no acceleration to be done
        if(startingPath.length() > 0) {
            return paths;
        }
        
        int currentVelocity = startingPath.getFinalVelocity();
        
        // we go from the lower bound to the current velocity and generate paths with the required number of DECs to get to
        // the desired velocity
        for(int desiredVelocity = lowerBound; desiredVelocity < currentVelocity; desiredVelocity++) {
            MovePath path = startingPath.clone();
            for(int deltaVelocity = 0; deltaVelocity < currentVelocity - desiredVelocity; deltaVelocity++) {
                path.addStep(MoveStepType.DEC);
            }
            
            paths.add(path);
        }
        
        // If the unaltered starting path is within acceptable velocity bounds, it's also a valid "acceleration".
        if(startingPath.getFinalVelocity() <= upperBound &&
           startingPath.getFinalVelocity() >= lowerBound) {
            paths.add(startingPath.clone());
        }
        
        // we go from the current velocity to the upper bound and generate paths with the required number of DECs to get to
        // the desired velocity
        for(int desiredVelocity = currentVelocity; desiredVelocity < upperBound; desiredVelocity++) {
            MovePath path = startingPath.clone();
            for(int deltaVelocity = 0; deltaVelocity < upperBound - desiredVelocity; deltaVelocity++) {
                path.addStep(MoveStepType.ACC);
            }
            
            paths.add(path);
        }
        
        return paths;
    }
    
    /**
     * Helper function to calculate the maximum thrust we should use for a particular aircraft
     * We limit ourselves to the lowest of "safe thrust" and "structural integrity", as anything further is unsafe, meaning it requires a PSR.
     * @param aero The aero entity for which to calculate max thrust.
     * @return The max thrust.
     */
    public static int calculateMaxSafeThrust(IAero aero) {
        int maxThrust = Math.min(aero.getCurrentThrust(), aero.getSI());    // we should only thrust up to our SI
        return maxThrust;
    }
    
    /**
     * Given a move path, generate all possible increases and decreases in elevation.
     * @param path The move path to process.
     * @return Collection of generated paths.
     */
    public static List<MovePath> generateValidAltitudeChanges(MovePath path) {
        List<MovePath> paths = new ArrayList<MovePath>();
        
        // clone path add UP
        // if path uses more MP than entity has available or altitude higher than 10, stop
        for(int altChange = 0; ; altChange++) {
            MovePath childPath = path.clone();
            
            for(int numSteps = 0; numSteps < altChange; numSteps++) {
                childPath.addStep(MoveStepType.UP);
            }
            
            if((childPath.getFinalAltitude() > 10) ||
                    childPath.getMpUsed() > path.getEntity().getRunMP()) {
                break;
            }
            
            paths.add(childPath);
        }
        
        // clone path add DOWN
        // if path uses more MP than entity has available or altitude lower than 1, stop
        for(int altChange = 1; ; altChange++) {
            MovePath childPath = path.clone();
            
            for(int numSteps = 0; numSteps < altChange; numSteps++) {
                childPath.addStep(MoveStepType.DOWN);
            }
            
            if((childPath.getFinalAltitude() < 1) ||
                    childPath.getMpUsed() > path.getEntity().getRunMP()) {
                break;
            }
            
            paths.add(childPath);
        }
        
        return paths;
    }
}
