package benplayer;
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
    ArrayList<MapLocation> cur_fights;
    boolean fast_start;
    public BaseRobot(RobotController inrc){
        rc = inrc;
        mytype = rc.getType();
        myteam = rc.getTeam();
        enemy = myteam.opponent();
        prev_points = new LinkedList<MapLocation>();
        line = new PolyLine();
        cur_fights = new ArrayList<MapLocation>();
        fast_start = is_fast_start();
    }
    boolean is_fast_start(){
        MapLocation[] mylocs = rc.getInitialArchonLocations(myteam);
        MapLocation[] enlocs = rc.getInitialArchonLocations(enemy);
        float max_dis = 0;
        for(MapLocation my : mylocs){
            for(MapLocation en : enlocs){
                max_dis = Math.max(max_dis, my.distanceTo(en));
            }
        }
        return max_dis < Const.FAST_START_DIS;
    }
    public void run() throws GameActionException {
        movement = new Movement(rc);
        donate_extra_bullets();
        space_robots();
        small_rand_pull();
        add_chase_val();
        towards_bullet_tree();
        shake_tree();
        handle_fights();
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
    void set_cur_fights() throws GameActionException {
        cur_fights.clear();
        for(int i = 0; i < Const.MAX_NUM_FIGHTS; i++){
            int broad_idx = i * 2 + Const.FIGHT_START_LOC;
            int turn = rc.readBroadcast(broad_idx+1);
            if(turn + Const.FIGHT_LENGTH > rc.getRoundNum()){
                MapLocation loc = new Message(rc.readBroadcast(broad_idx)).location();
                cur_fights.add(loc);
            }
        }
    }
    void broadcast_fight(int pos,MapLocation loc) throws GameActionException {
        rc.broadcast(pos,Message.EncodeMapLoc(loc));
        //System.out.println(Integer.toString(Message.EncodeMapLoc(loc)));
        //System.out.println(new Message(Message.EncodeMapLoc(loc)).location().toString());
        rc.broadcast(pos+1,rc.getRoundNum());
    }
    boolean add_new_fight(MapLocation loc) throws GameActionException {
        for(int i = 0; i < Const.MAX_NUM_FIGHTS; i++){
            int broad_idx = i * 2 + Const.FIGHT_START_LOC;
            int turn = rc.readBroadcast(broad_idx+1);
            if(turn + Const.FIGHT_LENGTH < rc.getRoundNum()){
                broadcast_fight(broad_idx,loc);
                return true;
            }
        }
        return false;
    }
    boolean should_add_new_right(){
        if(rc.senseNearbyRobots(-1,enemy).length >= Const.TROOPS_TO_FIGHT){
            for(MapLocation floc : cur_fights){
                if(floc.distanceTo(rc.getLocation()) < Const.FIGHT_RADIUS){
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }
    void draw_fights(){
        for(MapLocation loc : cur_fights){
            //System.out.println(loc.toString());
            rc.setIndicatorDot(loc,255,0,0);
            Direction dir = new Direction(1,0);
            for(int i = 0; i < 10; i++){
                MapLocation p = loc.add(dir,Const.FIGHT_RADIUS);
                rc.setIndicatorDot(p,255,255,255);
                dir.rotateLeftDegrees(36.0f);
            }
        }
    }
    void towards_bullet_tree(){
        for(TreeInfo tree : rc.senseNearbyTrees(-1,Team.NEUTRAL)){
            if(tree.containedBullets > 0){
                movement.addLiniarPull(tree.location,Const.BULLET_TREE_VAL);
            }
        }
    }
    void shake_tree() throws GameActionException{
        for(TreeInfo tree : rc.senseNearbyTrees(3,Team.NEUTRAL)){
            if(rc.canShake(tree.ID)){
                rc.shake(tree.ID);
            }
        }
    }
    void handle_fights() throws GameActionException {
        set_cur_fights();
        draw_fights();

        if(should_add_new_right()){
            if(add_new_fight(rc.getLocation())){
                rc.setIndicatorDot(rc.getLocation(),0,255,0);
            }
        }
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

        final Direction opt_dir = movement.bestLinDir();
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
        MapLocation loc = rc.getLocation().add(Const.randomDirection(),50);
        movement.addLiniarPull(loc,Const.SMALL_RAND_VAL);
    }

    boolean tryBuildRand(RobotType rty)throws GameActionException{
        if(!rc.hasRobotBuildRequirements(rty)){
            return false;
        }
        Direction dir = new Direction(0);

        final int check_locs = 6;
        final float rad_between =  (2 * (float)Math.PI) / check_locs;

        for(int i = 0; i < check_locs; i++) {
            dir = dir.rotateLeftRads(rad_between);
            if(rc.canBuildRobot(rty,dir)){
                rc.buildRobot(rty,dir);
                return true;
            }
        }
        return false;
    }
    boolean enemy_combat_troop_in_rad(float rad)throws GameActionException{
        for(RobotInfo rob : rc.senseNearbyRobots(rad,enemy)){
            if(rob.type.canAttack()){
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
            Direction build_dir = Const.randomDirection();
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
    void broadcast_scout_pestering() throws GameActionException{
        int scout_count = 0;
        for(RobotInfo rob : rc.senseNearbyRobots(2.5f,enemy)){
            if(rob.type == RobotType.SCOUT){
                scout_count++;
            }
        }
        if(scout_count > 0){
            rc.broadcast(Const.SCOUTS_PESTERING,Message.EncodeMapLoc(rc.getLocation()));
            rc.broadcast(Const.SCOUTS_PESTERING_TURN,rc.getRoundNum());
        }
    }
    void add_chase_val() throws GameActionException{
        for(RobotInfo rob : rc.senseNearbyRobots(-1,enemy)){
            float chase_val = Const.chase_val(mytype,rc.getHealth(),rc.getLocation(),rob.type,rob.health,rob.location);
            float chased_val = Const.chase_val(rob.type,rob.health,rob.location,mytype,rc.getHealth(),rc.getLocation());
            float dif_val = chase_val - chased_val;
            System.out.print(dif_val);
            System.out.print(' ');
            movement.addLiniarPull(rob.location,dif_val);
        }
        System.out.print('\n');
    }
    void donate_extra_bullets()throws GameActionException{
        if(GameConstants.VICTORY_POINTS_TO_WIN - rc.getTeamVictoryPoints() < rc.getTeamBullets() / 10){
            rc.donate(rc.getTeamBullets());
        }
        if(rc.getRoundNum() >= rc.getRoundLimit()-2){
            rc.donate(rc.getTeamBullets());
        }
    }
}
