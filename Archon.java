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
        broadcast_scout_pestering();
        donate_extra_bullets();

        produce_gardener();
        set_wander_movement();

        //try to move optimally
        moveOpti();
    }
    void produce_gardener() throws GameActionException {
        final int disired_begin_gardeners = 6;
        boolean desires_begin_gards = num_prod_garden * num_archons <= disired_begin_gardeners;
        boolean rand_gard = Math.random() < 0.02;
        boolean in_danger = enemy_combat_troop_in_rad(10);
        boolean very_begin_turn = rc.getRoundNum() < 100;
        boolean no_gard_produced = num_prod_garden == 0;

        if(!in_danger && (
                 very_begin_turn && no_gard_produced ||
                        (!very_begin_turn && (desires_begin_gards || rand_gard)))){
            if(tryHireGardenerRand()){
                num_prod_garden++;
            }
        }
    }
    boolean enemy_combat_troop_in_rad(float rad)throws GameActionException{
        for(RobotInfo rob : rc.senseNearbyRobots(rad,enemy)){
            if(rob.type.canAttack()){
                return true;
            }
        }
        return false;
    }
}
