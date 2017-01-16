package benplayer;

import battlecode.common.*;

public class Scout extends  FightRobot {
    public Scout(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        //if(rc.canFireSingleShot()){
        //    rc.fireSingleShot(Direction.getWest());
        //}
    }
    void chase_other_scouts(){
        for(RobotInfo rob : rc.senseNearbyRobots(-1,enemy)){
            if(rob.type == RobotType.SCOUT){
                movement.addLiniarPull(rob.location,Const.SCOUT_CHASE_SCOUT_VAL);
            }
        }
    }
}
