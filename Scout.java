package benplayer;

import battlecode.common.*;

public class Scout extends  FightRobot {
    public Scout(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        move_towards_fight();

        set_wander_movement();

        if(!move_into_gard_att_range()) {
            if (!moveOpti()) {
                // Move randomly
                tryMove(randomDirection());
            }
        }
        attack_body(first_attack_obj());
    }

    void chase_other_scouts(){
        for(RobotInfo rob : rc.senseNearbyRobots(-1,enemy)){
            if(rob.type == RobotType.SCOUT){
                movement.addLiniarPull(rob.location,Const.SCOUT_CHASE_SCOUT_VAL);
            }
        }
    }
    MapLocation into_gardener_attack_range(){
        float rob_sense_range = RobotType.GARDENER.bodyRadius/2 + RobotType.SCOUT.strideRadius;
        for(RobotInfo rob : rc.senseNearbyRobots(rob_sense_range,enemy)){
            if(rob.type == RobotType.GARDENER){
                Direction dir_to = rc.getLocation().directionTo(rob.location);
                float dis_to = rc.getLocation().distanceTo(rob.location);
                float dis_move = dis_to - (RobotType.GARDENER.bodyRadius + RobotType.SCOUT.bodyRadius + 0.001f);
                return rc.getLocation().add(dir_to,dis_move);
            }
        }
        return null;
    }
    boolean move_into_gard_att_range() throws GameActionException {
        MapLocation loc = into_gardener_attack_range();
        if(loc == null){
            return false;
        }
        else{
            if(rc.canMove(loc)){
                rc.move(loc);
                return true;
            }
        }
        return false;
    }
}
