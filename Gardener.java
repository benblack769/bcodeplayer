package benplayer;
import battlecode.common.*;

public class Gardener extends BaseRobot{
    public Gardener(RobotController inrc) {
        super(inrc);
    }

    boolean tree_built = false;
    int wander_timer = 30;
    boolean built_lumberjack = false;
    @Override
    public void run() throws GameActionException {
        super.run();

        // Listen for home archon's location
        int xPos = rc.readBroadcast(0);
        int yPos = rc.readBroadcast(1);
        MapLocation archonLoc = new MapLocation(xPos, yPos);

        avoid_directions_blocked();
        avoidArchons();
        set_wander_movement();
        //builds tree if not wandering
        if (wander_timer > 0) {
            if (buildTreeRand()) {
                tree_built = true;
            }
        }else{
            wander_timer--;
        }
        water_tree();
        produce_soldiers();
        produce_lumberjack();

        //if not sticking to tree, then move
        if(!tree_built){
            //try to move optimally
            if(!moveOpti()) {
                // try to move randomly
                tryMove(randomDirection());
            }
        }
    }
    void avoid_directions_blocked()throws GameActionException{
        final int dir_cnt = 10;
        final MapLocation cen = rc.getLocation();
        Direction dir = new Direction(0);
        for(int i = 0; i < dir_cnt; i++){
            dir = dir.rotateLeftRads((float)(Math.PI * 2 / dir_cnt));
            MapLocation loc = cen.add(dir);
            if(!rc.isLocationOccupiedByTree(loc)){
                movement.addLiniarPull(loc,Const.GAR_WAND_BLOCKED_VAL);
            }
            //visualize signt radii
            //rc.setIndicatorDot(cen.add(dir,RobotType.SOLDIER.bulletSightRadius),255,255,255);
            //rc.setIndicatorDot(cen.add(dir,RobotType.SOLDIER.sensorRadius),0,0,255);
        }
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
        if(rc.senseNearbyRobots(-1,rc.getTeam().opponent()).length > 1 ||
                (rc.getTeamBullets() > 1000 && Math.random() < 0.003)){
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
    void produce_lumberjack() throws GameActionException {
        float neut_tree_percent = neutral_tree_area() / Const.area(rc.getType().sensorRadius);
        if(!built_lumberjack && neut_tree_percent > Const.LUMBER_TREE_AREA_HIRE_PERC){
            if(tryBuildRand(RobotType.LUMBERJACK)){
                built_lumberjack = true;
            }
        }
    }
}
