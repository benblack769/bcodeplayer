package befplayer;

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

        if(!move_into_gard_att_range()) {
            if(!move_no_bullets()) {
                if (!moveOpti()) {
                    // Move randomly
                    tryMove(randomDirection());
                }
            }
        }
        setBlocks();
        attack_body(first_scout_or_gard());
    }
    boolean can_move_no_bullet(Direction dir,BulletInfo[] buls) {
        MapLocation loc = rc.getLocation().add(dir,RobotType.SCOUT.strideRadius);
        return rc.canMove(loc) && !is_on_bullet(loc,buls);
    }
    boolean is_on_bullet(MapLocation loc,BulletInfo[] buls){
        for(BulletInfo bul : buls){
            if(loc.distanceTo(bul.location) < RobotType.SCOUT.bodyRadius){
                return true;
            }
        }
        return false;
    }
    boolean move_no_bullets() throws GameActionException {
        Direction dir = movement.bestDir();
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
        float rob_sense_range = RobotType.GARDENER.bodyRadius/2 + RobotType.SCOUT.strideRadius;
        for(RobotInfo rob : rc.senseNearbyRobots(rob_sense_range,enemy)){
            if(rob.type == RobotType.GARDENER){
                //Direction dir_to = rc.getLocation().directionTo(rob.location);
                //float dis_to = rc.getLocation().distanceTo(rob.location);
                //float dis_move = dis_to - (RobotType.GARDENER.bodyRadius + RobotType.SCOUT.bodyRadius + 0.001f);
                //rc.getLocation().add(dir_to,dis_move);
                return into_gardener_attack_range(rob.location);
            }
        }
        return null;
    }
    MapLocation into_gardener_attack_range(MapLocation gard_loc){
        for(int i = 0; i < 10; i++){
            Direction dir = randomDirection();
            MapLocation moveloc = gard_loc.add(dir,2.0001f);
            if(moveloc.distanceTo(gard_loc) < mytype.strideRadius &&  rc.canMove(moveloc)){
                return  moveloc;
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
