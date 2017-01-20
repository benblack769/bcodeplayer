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
        if(!moveOpti()){
            // Move randomly
            tryMove(Const.randomDirection());
        }
        attack_body(first_attack_obj());
    }
    void move_up_to_scout(){
    }
}
