package befplayer;
import battlecode.common.*;

public class FightRobot extends  BaseRobot{
    public FightRobot(RobotController rc) {
        super(rc);
    }
    @Override
    public void run() throws GameActionException {
        super.run();

        Team enemy = rc.getTeam().opponent();

        MapLocation myLocation = rc.getLocation();

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        set_wander_movement();
        if(!moveOpti()){
            // Move randomly
            tryMove(randomDirection());
        }
        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFirePentadShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.firePentadShot(rc.getLocation().directionTo(robots[0].location));
            }
        }
    }
    void go_to_scout_pester(){

    }
}
