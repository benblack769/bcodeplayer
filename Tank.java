package befplayer;

import battlecode.common.*;

public class Tank extends  FightRobot {
    public Tank(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        move_towards_fight();

        set_wander_movement();
        if(!moveOpti()){
            // Move randomly
            tryMove(randomDirection());
        }
        attack_body(first_attack_obj());
    }
}
