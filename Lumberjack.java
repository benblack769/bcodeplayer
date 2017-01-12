package benplayer;

import battlecode.common.*;

/**
 * Created by benblack on 1/11/2017.
 */
public class Lumberjack extends BaseRobot {
    public Lumberjack(RobotController inrc){
        super(inrc);
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

                tryMove(toEnemy);
            } else {
                move_to_tree();
                if(!moveOpti()) {
                    // Move Randomly
                    tryMove(randomDirection());
                }
            }
        }
    }
    public void move_to_tree() throws GameActionException {
        for(TreeInfo tree : rc.senseNearbyTrees(-1,Team.NEUTRAL)){
            if(rc.canChop(tree.ID)){
                rc.chop(tree.ID);
            }
            float dis_tree = rc.getLocation().distanceTo(tree.location);
            movement.addLiniarPull(tree.location,Const.LUMBER_TREE_LOC_BASE/dis_tree);
        }
    }
}
