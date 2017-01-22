package befplayer;

import battlecode.common.*;

public class Scout extends  FightRobot {
    public Scout(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        int start = Clock.getBytecodeNum();
        super.run();
        set_wander_movement();
        int end1 =  Clock.getBytecodeNum();
        chase_scouts_and_gardeners();
        int end2 =  Clock.getBytecodeNum();


        //setBlocks();

        into_gardener_attack_range();

        moveOpti();
        int end3 =  Clock.getBytecodeNum();

        attack_body(first_scout_or_gard());
        int end4 =  Clock.getBytecodeNum();
        System.out.print("here1: ");
        System.out.println(end1-start);
        System.out.print("here2: ");
        System.out.println(end2-end1);
        System.out.println(end3-end2);
        System.out.println(end4-end3);
    }
    RobotInfo first_scout_or_gard(){
        RobotInfo[] robots = rc.senseNearbyRobots(-1,enemy);
        for(RobotInfo rob : robots){
            if(rob.type == RobotType.SCOUT && rob.location.distanceTo(rc.getLocation()) < 3.0f && !is_blocked(rob)){
                return rob;
            }
        }
        for(RobotInfo rob : robots){
            if(rob.type == RobotType.GARDENER && !is_blocked(rob)){
                return rob;
            }
        }
        return null;
    }
    void chase_scouts_and_gardeners(){
        for(RobotInfo rob : rc.senseNearbyRobots(-1,enemy)){
            float val = (rob.type == RobotType.SCOUT) ?
                    Const.SCOUT_CHASE_SCOUT_VAL :
                    (rob.type == RobotType.GARDENER) ?
                            Const.SCOUT_CHASE_GARD_VAL : 0;

            movement.addLiniarPull(rob.location,val);
        }
    }
    void into_gardener_attack_range(){
        float rob_sense_range = RobotType.GARDENER.bodyRadius + RobotType.SCOUT.strideRadius;
        for(RobotInfo rob : rc.senseNearbyRobots(rob_sense_range,enemy)){
            if(rob.type == RobotType.GARDENER){
                movement.addConsiderPoint(directly_up_to(rob),Const.SCOUT_MOVE_GARD);
            }
        }
    }
}
