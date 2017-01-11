package benplayer;

import battlecode.common.*;

public class Archon extends BaseRobot {
    int num_archons;
    int num_begin_gardeners = 0;
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
    void produce_gardener() throws GameActionException{
        final int disired_begin_gardeners = 3;
        if(num_begin_gardeners * num_archons <= disired_begin_gardeners ||
                Math.random() < 0.01){
            if(tryHireGardenerRand()){
                num_begin_gardeners++;
            }
        }
    }
}
