package befplayer;
import battlecode.common.*;

public class Gardener extends BaseRobot{
    public Gardener(RobotController inrc) {
        super(inrc);
    }

    boolean tree_built = false;
    boolean built_lumberjack = false;
    @Override
    public void run() throws GameActionException {
        super.run();

        avoidArchons();
        set_wander_movement();

        water_tree();
        produce_lumberjack();
        //builds tree if not wandering make sure this happens after troop production
        if(!should_produce_lumberjack()) {
            if (!location_blocks_two_paths()) {
                if ((7 - encircled_loc_count()) >= Const.MIN_TREE_OPENINGS) {
                    buildTree();
                    tree_built = true;
                }
            }
            if (tree_built && 7 - encircled_loc_count() > 1) {
                buildTree();
            }
        }
        produce_soldiers();

        //if not sticking to tree, then move
        if(!tree_built){
            //try to move optimally
            if(!moveOpti()) {
                // try to move randomly
                tryMove(randomDirection());
            }
        }
    }
    boolean isCircleOccupiedByTreeRoughly(MapLocation loc,float radius) throws GameActionException {
        return rc.onTheMap(loc) && rc.isCircleOccupiedExceptByThisRobot(loc,radius) && !rc.isLocationOccupiedByRobot(loc);
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

        boolean res = false;
        for(int i = 0; i < check_locs; i++){
            dir = dir.rotateLeftRads(rad_between);
            MapLocation loc = cen.add(dir,outer_rad);
            boolean this_blocked = isCircleOccupiedByTreeRoughly(loc,1.0f);
            //rc.setIndicatorDot(loc,255,255,255);
            //if(this_blocked){
            //    rc.setIndicatorDot(loc,255,0,0);
            //}
            if(this_blocked && !prev_blocked){
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
            if(isCircleOccupiedByTreeRoughly(loc,0.99f)){
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
    void produce_soldiers()throws GameActionException{
        if(rc.senseNearbyRobots(-1,enemy).length > 1 ||
                (rc.getTeamBullets() > 1000 && Math.random() < 0.9)){
            tryBuildRand(RobotType.SOLDIER);
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
}
