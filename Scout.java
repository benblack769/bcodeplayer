package benplayer;

import battlecode.common.*;

import java.awt.*;
import java.util.Map;

public class Scout extends  FightRobot {
    public Scout(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
        chase_scouts_and_gardeners();

        set_wander_movement();

        setBlocks();
        handle_move();
        attack_body(first_scout_or_gard());
    }
    void handle_move() throws GameActionException {
        if(!move_into_gard_att_range()) {
            if(!move_no_bullets()) {
                if (!moveOpti()) {
                    // Move randomly
                    tryMove(Const.randomDirection());
                }
            }
        }
    }
    boolean move_no_bullets() throws GameActionException {
        Direction dir = movement.bestLinDir();
        BulletInfo[] bullets = rc.senseNearbyBullets(RobotType.SCOUT.bodyRadius + RobotType.SCOUT.strideRadius);

        final int checksPerSide = 6;
        final float degreeOffset = 10;
        // First, try intended direction
        if (can_move_no_bullet(dir,bullets)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        int currentCheck = 1;

        while(currentCheck <= checksPerSide) {
            // Try the offset of the left side
            if(can_move_no_bullet(dir.rotateLeftDegrees(degreeOffset*currentCheck),bullets)) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(can_move_no_bullet(dir.rotateRightDegrees(degreeOffset*currentCheck),bullets)) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
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
    MapLocation into_gardener_attack_range(){
        BulletInfo[] bullets = rc.senseNearbyBullets(RobotType.SCOUT.bodyRadius + RobotType.SCOUT.strideRadius);
        float rob_sense_range = RobotType.GARDENER.bodyRadius/2 + RobotType.SCOUT.strideRadius;
        for(RobotInfo rob : rc.senseNearbyRobots(rob_sense_range,enemy)){
            if(rob.type == RobotType.GARDENER){
                return directly_up_to(rob);
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
