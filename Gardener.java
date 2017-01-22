package befplayer;
import battlecode.common.*;

import java.util.ArrayList;

public class Gardener extends BaseRobot{
    public Gardener(RobotController inrc) {
        super(inrc);
        prev_health = rc.getHealth();
    }

    boolean tree_built = false;
    int trees_built = 0;
    boolean built_lumberjack = false;
    boolean def_troop = false;
    float prev_health;
    boolean is_being_attacked;
    @Override
    public void run() throws GameActionException {
        super.run();
        handle_is_being_attacked();

        broadcast_scout_pestering();

        avoidArchons();
        set_wander_movement();
        move_towards_unhealed_trees();

        water_tree();
        handle_production();

        handle_move();
    }
    void handle_production() throws GameActionException {
        if(rc.getRoundNum() < 30) {
            buildTree();
        }
        else if(should_produce_scout()){
            produce_scout();
        }
        else if(should_produce_soldier()){
            produce_soldiers();
        }
        else if(should_produce_lumberjack()){
            produce_lumberjack();
        }
        else{
            produce_tree();
        }
    }
    void handle_move() throws GameActionException {
        if(!tree_built) {
            //try to move optimally
            moveOpti();
        }
    }
    void handle_is_being_attacked(){
        is_being_attacked = (rc.getHealth() < prev_health);
        prev_health = rc.getHealth();
    }
    void move_towards_unhealed_trees(){
        for(TreeInfo tree: rc.senseNearbyTrees(-1,myteam)){
            if(tree.getHealth() < tree.maxHealth - 10){
                movement.addLiniarPull(tree.location,Const.GARD_MOVE_TOWARDS_UNHEALED_TREE);
            }
            else{
                movement.addLiniarPull(tree.location,Const.GARD_MOVE_TOWARDS_UNHEALED_TREE/2);
            }
        }
    }
    void move_into_unhealed_trees(){
        ArrayList<TreeInfo> trees = new ArrayList<TreeInfo>();
        for(TreeInfo tree: rc.senseNearbyTrees(-1,myteam)){
            if(tree.getHealth() < tree.maxHealth - 10){
                trees.add(tree);
            }
        }
        TreeInfo t1 =null;
        TreeInfo t2;
        outloop:
        for(TreeInfo tree : trees){
            for(TreeInfo otree : trees){
                float dis = tree.location.distanceTo(otree.location);
                if(tree.ID != otree.ID && dis < 1.001){
                    t1 = tree;
                    t2 = otree;
                    break outloop;
                }
            }
        }
        if(t1 == null){
            return;
        }
        MapLocation ce;
        for(TreeInfo t3 : trees){

        }
    }
    boolean isCircleOccupied(MapLocation loc) throws GameActionException {
        return !rc.onTheMap(loc) || rc.isCircleOccupiedExceptByThisRobot(loc,1.0f);
    }
    boolean isLocationBlockedPermanently(MapLocation loc) throws GameActionException {
        return !rc.onTheMap(loc) || rc.isLocationOccupiedByTree(loc);
    }
    boolean location_blocks_two_paths() throws GameActionException{
        final MapLocation cen = rc.getLocation();
        Direction dir = new Direction(0);

        final float outer_rad = 4f;
        final float outer_circum = (float)Math.PI * 2 * outer_rad;
        final int check_locs = (int)outer_circum;
        final float rad_between =  ((float)Math.PI * 2) / check_locs;

        boolean some_chunk_blocked = false;
        boolean prev_blocked = false;
        int num_open = 0;

        boolean res = false;
        for(int i = 0; i < check_locs; i++){
            dir = dir.rotateLeftRads(rad_between);
            MapLocation loc = cen.add(dir,outer_rad);
            boolean this_blocked = isLocationBlockedPermanently(loc);
            //rc.setIndicatorDot(loc,255,255,255);
            //if(this_blocked){
            //    rc.setIndicatorDot(loc,255,0,0);
            //}
            if(this_blocked){
                num_open++;
            }
            else{
                num_open = 0;
            }
            if(num_open >= 2 && !prev_blocked){
                if(some_chunk_blocked){
                    res = true;
                }
                else{
                    some_chunk_blocked = true;
                }
            }
            prev_blocked = this_blocked;
        }
        return res;
    }
    int encircled_loc_count() throws GameActionException {
        final MapLocation cen = rc.getLocation();
        Direction dir = new Direction(0);

        final float outer_rad = 1.5f;
        final int check_locs = 6;
        final float rad_between =  (2 * (float)Math.PI) / check_locs;

        int blocked_count = 0;
        for(int i = 0; i < check_locs; i++) {
            dir = dir.rotateLeftRads(rad_between);
            MapLocation loc = cen.add(dir, outer_rad);
            //rc.setIndicatorDot(loc,255,255,255);
            if(isCircleOccupied(loc)){
                //rc.setIndicatorDot(loc,255,0,0);
                blocked_count++;
            }
        }
        return blocked_count;
    }
    boolean buildTree() throws GameActionException{
        Direction dir = new Direction(0);

        final int check_locs = 6;
        final float rad_between =  (2 * (float)Math.PI) / check_locs;

        for(int i = 0; i < check_locs; i++) {
            dir = dir.rotateLeftRads(rad_between);
            if(rc.canPlantTree(dir)){
                rc.plantTree(dir);
                tree_built = true;
                trees_built++;
                return true;
            }
        }
        return false;
    }

    void avoidArchons() throws GameActionException {
        for(MapLocation aloc : nearbyArchons()){
            movement.addLiniarPull(aloc,-Const.GAR_WAND_ARCHON_AVOID);
        }
    }
    boolean water_tree() throws GameActionException{
        if(!rc.canWater()){
            return false;
        }
        for(TreeInfo tinf : rc.senseNearbyTrees(2,rc.getTeam())){
            //rc.setIndicatorLine(rc.getLocation(),tinf.getLocation(),0,0,0);
            if(tinf.health <= tinf.maxHealth - GameConstants.WATER_HEALTH_REGEN_RATE){
                rc.water(tinf.ID);
                return true;
            }
        }
        return false;
    }
    boolean should_produce_scout()throws GameActionException{
        return (!fast_start && !def_troop && rc.getRoundNum() <= Const.SOLDIER_DEF_ROUND) ||
                (new Message(rc.readBroadcast(Const.SCOUTS_PESTERING)).nonEmpty()
                        && rc.readBroadcast(Const.SCOUTS_PESTERING_TURN) + Const.SCOUT_PESTER_LENGTH > rc.getRoundNum());
    }
    boolean should_produce_soldier()throws GameActionException{
        return((fast_start && !def_troop && rc.getRoundNum() > Const.SOLDIER_DEF_ROUND) ||
                rc.senseNearbyRobots(-1,enemy).length > 1 ||
                (rc.getTeamBullets() > 500 && Math.random() < 0.9));
    }
    boolean produce_scout()throws GameActionException{
        if(tryBuildRand(RobotType.SCOUT)){
            def_troop = true;
            return true;
        }
        return false;
    }
    boolean produce_soldiers()throws GameActionException{
        if(tryBuildRand(RobotType.SOLDIER)){
            def_troop = true;
            return true;
        }
        return false;
    }
    void produce_tank()throws GameActionException{
        if(rc.senseNearbyRobots(-1,enemy).length > 1 ||
                (rc.getTeamBullets() > 500 && Math.random() < 0.5)){
            tryBuildRand(RobotType.TANK);
        }
    }
    float neutral_tree_area(){
        float areasum = 0;
        for(TreeInfo tree : rc.senseNearbyTrees(-1,Team.NEUTRAL)){
            areasum += Const.area(tree.radius);
        }
        return areasum;
    }
    boolean should_produce_lumberjack(){
        float neut_tree_percent = neutral_tree_area() / Const.area(rc.getType().sensorRadius);
        return !built_lumberjack && neut_tree_percent > Const.LUMBER_TREE_AREA_HIRE_PERC;
    }
    boolean produce_lumberjack() throws GameActionException {
        if(should_produce_lumberjack()){
            if(tryBuildRand(RobotType.LUMBERJACK)){
                built_lumberjack = true;
                return true;
            }
        }
        return false;
    }
    boolean produce_tree() throws GameActionException {
        if (!location_blocks_two_paths()) {
            if ((6 - encircled_loc_count()) >= Const.MIN_TREE_OPENINGS) {
                return buildTree();
            }
        }
        if (tree_built && 6 - encircled_loc_count() > 1) {
            return buildTree();
        }
        return false;
    }
}
