package benplayer;
import battlecode.common.*;

public class Soldier extends FightRobot{
    public Soldier(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        move_towards_fight();

        set_wander_movement();
        moveOpti();

        attack_body(first_attack_obj());
    }
}
