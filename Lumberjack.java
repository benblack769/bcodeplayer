package benplayer;

import battlecode.common.*;
public class Lumberjack extends BaseRobot {
    MapLocation origin;

    public Lumberjack(RobotController inrc){
        super(inrc);
        origin = rc.getLocation();
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        Team enemy = rc.getTeam().opponent();
        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

        if(false && robots.length > 0 && !rc.hasAttacked()) {
            // Use strike() to hit all nearby robots!
            rc.strike();
        } else {
            // No close robots, so search for robots within sight radius
            robots = rc.senseNearbyRobots(-1,enemy);

            // If there is a robot, move towards it
            if(false && robots.length > 0) {
                MapLocation myLocation = rc.getLocation();
                MapLocation enemyLocation = robots[0].getLocation();
                Direction toEnemy = myLocation.directionTo(enemyLocation);
            } else {
                move_to_tree();

                moveOpti();
            }
        }
        chop_best_tree();
    }
    float tree_chop_val(TreeInfo tree){
        //prioritizes unheathy trees over healthy ones
        float tree_healt_val = tree.maxHealth / tree.health;
        //prioritizes large trees over small ones
        float tree_size_val = Const.area(tree.radius);
        float base_val = Const.LUMBER_TREE_LOC_BASE;
        //prioritizes close trees over far ones from gardener which produced it
        float closeness_to_origin = 1 / tree.location.distanceTo(origin);
        return tree_healt_val * tree_size_val * base_val * closeness_to_origin;
    }
    void chop_best_tree() throws GameActionException {
        TreeInfo besttree = null;
        float bestval = -10e10f;
        for(TreeInfo tree : rc.senseNearbyTrees(-1,Team.NEUTRAL)) {
            if(rc.canChop(tree.ID)) {
                float tval = tree_chop_val(tree);
                if (tval > bestval) {
                    bestval = tval;
                    besttree = tree;
                }
            }
        }
        if (besttree != null && rc.canChop(besttree.ID)) {
            rc.chop(besttree.ID);
        }
    }
    void move_to_tree() throws GameActionException {
        for(TreeInfo tree : rc.senseNearbyTrees(-1,Team.NEUTRAL)){
            movement.addLiniarPull(tree.location,tree_chop_val(tree));
        }
    }
}
