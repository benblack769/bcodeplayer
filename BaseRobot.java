package benplayer;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;

public class BaseRobot {
    static RobotController rc;
    static Movement movement;
    public BaseRobot(RobotController inrc){
        rc = inrc;
    }
    public void run() throws GameActionException {
        movement = new Movement();
        donate_extra_bullets();
    }

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,10,6);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        int currentCheck = 1;

        while(currentCheck <= checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    boolean tryBuildRand(RobotType rty)throws GameActionException{
        if(!rc.hasRobotBuildRequirements(rty)){
            return false;
        }
        int rand_checks = 10;
        for(int i = 0; i < rand_checks; i++){
            Direction build_dir = randomDirection();
            if(rc.canBuildRobot(rty,build_dir)){
                rc.buildRobot(rty,build_dir);
                return true;
            }
        }
        return false;
    }
    boolean tryHireGardenerRand()throws GameActionException{
        if(!rc.hasRobotBuildRequirements(RobotType.GARDENER)){
            return false;
        }
        int rand_checks = 10;
        for(int i = 0; i < rand_checks; i++){
            Direction build_dir = randomDirection();
            if(rc.canHireGardener(build_dir)){
                rc.hireGardener(build_dir);
                return true;
            }
        }
        return false;
    }
    MapLocation[] nearbyArchons(){
        final float archon_dis = 5;
        ArrayList<MapLocation> res = new ArrayList<MapLocation>();
        for(RobotInfo r :  rc.senseNearbyRobots(archon_dis,rc.getTeam())){
            if(r.type == RobotType.ARCHON){
                res.add(r.location);
            }
        }
        return res.toArray(new MapLocation[res.size()]);
    }
    boolean is_too_close(MapLocation loc, MapLocation[] locs,float close_dis){
        for(MapLocation aloc :  locs){
            if(aloc.distanceTo(loc) < close_dis){
                return true;
            }
        }
        return false;
    }
    boolean buildTreeRand() throws GameActionException{
        if(!rc.hasTreeBuildRequirements()) {
            return false;
        }
        //makes sure that trees are not planted too close to the archon, hopefully not locking the archon into a hole

        MapLocation[] archonlocs = nearbyArchons();
        final int tree_build_tries = 10;
        for(int i = 0; i < tree_build_tries; i++){
            Direction dir = randomDirection();
            MapLocation build_loc = rc.getLocation().add(dir);

            if(rc.canPlantTree(dir) &&
                    !is_too_close(build_loc,archonlocs,8.0f)){
                rc.plantTree(dir);
                return true;
            }
        }
        return false;
    }

    void donate_extra_bullets()throws GameActionException{
        final float max_store_bullets = 2000;
        float donate_bullets = rc.getTeamBullets() - max_store_bullets;
        if(donate_bullets > 0){
            rc.donate(donate_bullets);
        }
        if(GameConstants.VICTORY_POINTS_TO_WIN - rc.getTeamVictoryPoints() < rc.getTeamBullets() / 10){
            rc.donate(rc.getTeamBullets());
        }
    }
        /**
         * A slightly more complicated example function, this returns true if the given bullet is on a collision
         * course with the current robot. Doesn't take into account objects between the bullet and this robot.
         *
         * @param bullet The bullet in question
         * @return True if the line of the bullet's path intersects with this robot's current position.
         */
    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
}
