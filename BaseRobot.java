package befplayer;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class BaseRobot {
    RobotController rc;
    Movement movement;
    RobotType mytype;
    Team myteam;
    Team enemy;
    LinkedList<MapLocation> prev_points;
    PolyLine line;
    public BaseRobot(RobotController inrc){
        rc = inrc;
        mytype = rc.getType();
        myteam = rc.getTeam();
        enemy = myteam.opponent();
        prev_points = new LinkedList<MapLocation>();
        line = new PolyLine();
    }
    public void run() throws GameActionException {
        movement = new Movement(rc);
        donate_extra_bullets();
        space_robots();
        small_rand_pull();
        add_chase_val();
        line = new PolyLine();
    }
    void set_wander_movement(){
        //first calculates value
        for(MapLocation ploc : prev_points){
            movement.addLiniarPull(ploc,- Const.WANDER_MOVE_ON_VAL);
        }
        //then rearanges the queue.
        if(prev_points.size() >= Const.WANDER_MEMORY_LENGTH){
            prev_points.pop();
        }
        prev_points.add(rc.getLocation());
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
    boolean moveOpti()throws GameActionException{
        movement.calc_bullet_collision_values(rc.senseNearbyBullets());

        final Direction opt_dir = movement.bestDir();
        return tryMove(opt_dir);
    }

    void space_robots(){
        final MapLocation myloc = rc.getLocation();
        for(RobotInfo r : rc.senseNearbyRobots(-1,rc.getTeam())){
            float dis = myloc.distanceTo(r.location);
            if(dis < 3){
                movement.addLiniarPull(r.location,Const.TROOP_SPACE_VAL/dis);
            }
        }
    }
    void small_rand_pull(){
        MapLocation loc = rc.getLocation().add(randomDirection(),50);
        movement.addLiniarPull(loc,Const.SMALL_RAND_VAL);
    }

    boolean tryBuildRand(RobotType rty)throws GameActionException{
        if(!rc.hasRobotBuildRequirements(rty)){
            return false;
        }
        for(int i = 0; i < Const.RAND_BUILD_TRIES; i++){
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
        for(int i = 0; i < Const.RAND_BUILD_TRIES; i++){
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
        for(int i = 0; i < Const.RAND_BUILD_TRIES; i++){
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
    static float randInRange(float min, float max){
        final float dis = max - min;
        return (float)(Math.random()) * dis + min;
    }
    MapLocation[] movementPoints(){
        final int edge_points = 1;
        final int cen_points = 1;
        final int tot_points = edge_points + cen_points;
        final float movedis = rc.getType().strideRadius;

        MapLocation[] allps = new MapLocation[tot_points];

        MapLocation cen = rc.getLocation();

        //gets random points around edges
        Direction west = new Direction(1,0);
        for(int i = 0; i < edge_points; i++){
            Direction rdir = west.rotateLeftRads(randInRange(0,(float)(Math.PI)));
            allps[i] = cen.add(rdir,movedis);
        }
        //gets random points in middle
        for(int j = cen_points; j < tot_points; j++){
            while(true){
                Float x = randInRange(cen.x - movedis,cen.x + movedis);
                Float y = randInRange(cen.y - movedis,cen.y + movedis);
                MapLocation loc = new MapLocation(x,y);
                if(loc.distanceTo(cen) < movedis){
                    allps[j] = loc;
                    break;
                }
            }
        }
        return allps;
    }
    void add_chase_val()throws GameActionException{
        for(RobotInfo rob : rc.senseNearbyRobots(-1,enemy)){
            float chase_val = Const.chase_val(mytype,rc.getHealth(),rc.getLocation(),rob.type,(float)rob.health,rob.location);
            float chased_val = Const.chase_val(rob.type,(float)rob.health,rob.location,mytype,rc.getHealth(),rc.getLocation());
            movement.addLiniarPull(rob.location,chase_val - chased_val);
        }
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

}
