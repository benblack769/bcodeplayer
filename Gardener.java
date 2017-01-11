package benplayer;
import battlecode.common.*;

public class Gardener extends BaseRobot{
    public Gardener(RobotController rc) {
        super(rc);
    }

    boolean tree_built = false;
    int wander_timer = 20;
    @Override
    public void run() throws GameActionException {
        super.run();

        // Listen for home archon's location
        int xPos = rc.readBroadcast(0);
        int yPos = rc.readBroadcast(1);
        MapLocation archonLoc = new MapLocation(xPos, yPos);

        if (wander_timer > 0) {
            if (buildTreeRand()) {
                tree_built = true;
            }
        }else{
            wander_timer--;
        }
        water_tree();
        produce_soldiers();

        // Move randomly
        if(!tree_built){
            tryMove(randomDirection());
        }
    }
    boolean water_tree() throws GameActionException{
        if(!rc.canWater()){
            return false;
        }
        for(TreeInfo tinf : rc.senseNearbyTrees(2,rc.getTeam())){
            rc.setIndicatorLine(rc.getLocation(),tinf.getLocation(),0,0,0);
            if(tinf.health <= tinf.maxHealth - GameConstants.WATER_HEALTH_REGEN_RATE){
                rc.water(tinf.ID);
                return true;
            }
        }
        return false;
    }
    void produce_soldiers()throws GameActionException{
        if(rc.senseNearbyRobots(-1,rc.getTeam().opponent()).length > 0 ||
                (rc.getTeamBullets() > 1000 && Math.random() < 0.05)){
            tryBuildRand(RobotType.SOLDIER);
        }
    }
}
