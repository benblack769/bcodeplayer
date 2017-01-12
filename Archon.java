package benplayer;

import battlecode.common.*;

public class Archon extends BaseRobot {
    int num_archons;
    int num_prod_garden = 0;
    public Archon(RobotController inrc) {
        super(inrc);
        num_archons = rc.getInitialArchonLocations(rc.getTeam()).length;
    }

    @Override
    public void run()throws GameActionException  {
        super.run();
        donate_extra_bullets();

        produce_gardener();

        // Move randomly
        tryMove(randomDirection());

        // Broadcast archon's location for other robots on the team to know
        MapLocation myLocation = rc.getLocation();
        rc.broadcast(0,(int)myLocation.x);
        rc.broadcast(1,(int)myLocation.y);
    }
    void produce_gardener() throws GameActionException {
        final int disired_begin_gardeners = 6;
        final int desired_middle_gardener = 15;
        if(num_prod_garden * num_archons <= disired_begin_gardeners ||
                Math.random() < 0.03 ||
                (num_prod_garden * num_archons <= desired_middle_gardener
                        && rc.getTeamBullets() > 500)){
            if(tryHireGardenerRand()){
                num_prod_garden++;
            }
        }
    }
}
