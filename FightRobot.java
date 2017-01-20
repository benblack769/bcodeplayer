package benplayer;
import battlecode.common.*;

public class FightRobot extends  BaseRobot{
    PolyLine blocks;
    public FightRobot(RobotController rc) {
        super(rc);
    }


    static final float OCT_DIS = (float)(Math.PI*2)/8;
    static final int eight = 8;
    static final Direction[] octants = {
            new Direction(0.5f*OCT_DIS),
            new Direction(1.5f*OCT_DIS),
            new Direction(2.5f*OCT_DIS),
            new Direction(3.5f*OCT_DIS),
            new Direction(4.5f*OCT_DIS),
            new Direction(5.5f*OCT_DIS),
            new Direction(6.5f*OCT_DIS),
            new Direction(7.5f*OCT_DIS)
    };
    float[] octantvals = new float[eight];

    void setBlocks() {
        blocks = new PolyLine();
        for (TreeInfo tree : rc.senseNearbyTrees(6)) {
            if (tree.radius < 1.0) {
                blocks.addSmallBody(tree, 0);
            } else {
                blocks.addBody(tree, 0);
            }
        }
    }
    MapLocation move_to_scout_loc(RobotInfo scout){
        MapLocation my_loc = rc.getLocation();
        if(scout.location.distanceTo(my_loc) > mytype.strideRadius){
            return null;
        }
        return  null;
    }
    boolean is_blocked(RobotInfo rob){
        return blocks.intersects(new LinSeg(rob.location,rc.getLocation()));
    }
    int getOctant(Direction dir){
        int deg = (int)(360f + dir.getAngleDegrees()) % 360;
        return (deg * 8) / 360;
    }
    void setOctantVals(){
        octantvals = new float[eight];
        for(int i = 0; i < eight; i++){
            octantvals[i] = 0;
        }
        for(RobotInfo rob : rc.senseNearbyRobots()){
            int sign = rob.team == myteam ? -1 : 1;
            float value = Const.damageValue(rob.type,rob.health);
            int oct = getOctant(rc.getLocation().directionTo(rob.location));
            octantvals[oct] += value * sign;
        }
        //printOctVals();
    }
    void printOctVals(){
        float maxval = 0;
        for(float val : octantvals){
            maxval = Math.max(maxval,Math.abs(val));
        }
        if(maxval <= 0){
            return;
        }
        for(int i = 0; i < eight; i++){
            boolean isred = octantvals[i] > 0;
            int absval = (int)Math.abs(254f * octantvals[i] / maxval);
            int redval =  isred ? absval : 0;
            int blueval =  !isred ? absval : 0;
            rc.setIndicatorDot(rc.getLocation().add(octants[i],3f),redval,0,blueval);
        }
    }
    BodyInfo best_attack_obj(){
        setOctantVals();

        RobotInfo bestrob = null;
        float bestval = -10e10f;
        for(RobotInfo rob : rc.senseNearbyRobots(-1, enemy)){
            float oct_val = Const.FRIENDLY_FIRE_VAL *
                    octantvals[getOctant(rc.getLocation().directionTo(rob.location))];
            float rob_val = Const.damageValue(rob.type,rob.health);
            float dis_val = 1.0f / rob.location.distanceSquaredTo(rc.getLocation());
            rc.setIndicatorDot(rob.location,255,255,255);
            float tot_val = oct_val + dis_val * rob_val;
            if(bestval < tot_val){
                bestval = tot_val;
                bestrob = rob;
            }
        }
        return bestrob;
    }
    void move_towards_fight() throws GameActionException {
        for(MapLocation floc : cur_fights){
            movement.addLiniarPull(floc,Const.MOVE_TO_FIGHT_VAL);
        }
    }
    BodyInfo first_attack_obj(){
        RobotInfo[] robs = rc.senseNearbyRobots(-1, enemy);
        return robs.length > 0 ? robs[0] : null;
    }
    void attack_body(BodyInfo body) throws GameActionException {
        if(body == null){
            return;
        }
        // And we have enough bullets, and haven't attacked yet this turn...
        if (rc.canFirePentadShot()) {
            // ...Then fire a bullet in the direction of the enemy.
            rc.firePentadShot(rc.getLocation().directionTo(body.getLocation()));
        }
        if(rc.canFireSingleShot()){
            rc.fireSingleShot(rc.getLocation().directionTo(body.getLocation()));
        }
    }
    boolean in_bullet_path(BulletInfo bul,MapLocation loc,int turns){
        float bul_dis = bul.speed * turns;
        if(bul.dir.getDeltaX(0) == 0){
            return true;
        }
        float y = bul.dir.getDeltaY(0) / bul.dir.getDeltaX(0);
        return y < 0;
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
    MapLocation directly_up_to(RobotInfo rob){
        float tot_body_rad = rob.type.bodyRadius + mytype.bodyRadius + 0.0001f;
        for(int i = 0; i < 20; i++){
            Direction dir = Const.randomDirection();
            MapLocation moveloc = rob.location.add(dir,tot_body_rad);
            if(moveloc.distanceTo(rob.location) < mytype.strideRadius &&  rc.canMove(moveloc)){
                return  moveloc;
            }
        }
        return null;
    }
}
