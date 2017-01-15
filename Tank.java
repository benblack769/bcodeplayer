package befplayer;

import battlecode.common.*;

public class Tank extends  FightRobot {
    public Tank(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        //if(rc.canFireSingleShot()){
        //    rc.fireSingleShot(Direction.getWest());
        //}
    }
}
