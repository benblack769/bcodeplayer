package befplayer;

import battlecode.common.*;

import java.util.ArrayList;

final public class Movement {
    final float speed;
    final float body_rad;
    final MapLocation cen;
    final float damage_value;
    final RobotType type;

    TreeInfo[] trees;
    BodyInfo[] blocking_objs;
    BulletInfo[] close_bullets;

    MapLocation liniar_pull;
    RobotController rc;
    ArrayList<Float> consider_values;
    ArrayList<MapLocation> consider_points;

    public Movement(RobotController inrc) throws GameActionException {
        rc = inrc;
        type = rc.getType();
        speed = type.strideRadius;
        cen = rc.getLocation();
        body_rad = type.bodyRadius;
        damage_value = Const.damageValue(type,rc.getHealth());

        trees = rc.senseNearbyTrees();
        RobotInfo[] robots = rc.senseNearbyRobots();
        close_bullets = rc.senseNearbyBullets(speed+body_rad);

        blocking_objs = new BodyInfo[trees.length + robots.length];
        liniar_pull = new MapLocation(0,0);

        consider_values = new  ArrayList<Float>();
        consider_points = new  ArrayList<MapLocation>();

        System.arraycopy(trees, 0, blocking_objs, 0, trees.length);
        System.arraycopy(robots, 0, blocking_objs, trees.length, robots.length);

        //calc_avoid_trees();
    }
    public void addConsiderPoint(MapLocation loc,float bonus_val){
        if(loc == null){
            System.out.println("local null added, not good.");
            return;
        }
        if(!rc.canMove(loc)){
            System.out.println("not valid move, not good.");
            return;
        }
        consider_points.add(loc);
        consider_values.add(bonus_val);
    }
    public void addConsiderPoint(Direction dir,float bonus_val){
        addConsiderPoint(cen.add(dir,speed),bonus_val);
    }
    float linVal(MapLocation loc){
        float difx = loc.x - cen.x;
        float dify = loc.y - cen.y;
        return ((difx * liniar_pull.x) + (dify * liniar_pull.y));
    }
    float bulVal(MapLocation loc,BulletInfo bul){
        if(willCollideWithMe(bul,loc)) {
            return - Const.damageValue(type, rc.getHealth()) * bul.damage;
        }
        else{
            return 0;
        }
    }
    float bulletsVal(MapLocation loc){
        float tot_val = 0;
        for(BulletInfo bul : close_bullets){
            tot_val += bulVal(loc,bul);
        }
        return tot_val;
    }
    public MapLocation bestLoc(){
        int start = Clock.getBytecodeNum();
        addLinPoints();
        addArbPoints();

        //first stage, consider best possible points
        int size = consider_values.size();
        Float[] values = consider_values.toArray(new Float[consider_values.size()]);
        MapLocation[] points = consider_points.toArray(new MapLocation[consider_points.size()]);

        for(int i = 0; i < size; i++){
            values[i] += linVal(points[i]);
            if(Clock.getBytecodesLeft() > 2000) {
                //values[i] += bulletsVal(points[i]);
            }
            else{
                //System.out.println("bullet calculations truncated due to bytecode limit");
            }
        }
        MapLocation max_p = null;
        float smallval = Float.NEGATIVE_INFINITY;
        float bestv = smallval;
        for(int i = 0; i < size; i++){
            float val = values[i];
            if(bestv <= val){
                bestv = val;
                max_p = points[i];
            }
            if(val < -10e10f){
                System.out.println("really small value!!");
            }
            if(points[i] == null){
                System.out.println("null maplocation!!");
            }
        }
        if(max_p == null && size > 0){
            System.out.println("value calculation totally off!!!");
            System.out.println(size);
            for(MapLocation loc :  points){
                System.out.println(loc);
            }
            for(Float val : values){
                System.out.println(val);
            }
        }
        int end = Clock.getBytecodeNum();
        System.out.println(end-start);
        return max_p;
    }
    void addArbPoints(){
        final int num_cen_ps = 3;
        final int num_edge_ps = 5;
        Direction dir = Const.randomDirection();
        //gets random points in middle
        for(int j = 0; j < num_edge_ps; j++){
            dir = dir.rotateLeftDegrees(360f/num_edge_ps);
            MapLocation loc = cen.add(dir,speed);
            if(rc.canMove(loc)){
                addConsiderPoint(loc,0);
            }
        }
        for(int j = 0; j < num_cen_ps; j++){
            dir = dir.rotateLeftDegrees(360f/num_cen_ps);
            MapLocation loc = cen.add(dir,speed/2);
            if(rc.canMove(loc)){
                addConsiderPoint(loc,0);
            }
        }
    }
    void addLinPoints() {
        //add liniar point
        Direction dir = bestLinDir();
        if(dir == null){
            dir = Const.randomDirection();
        }
        if (rc.canMove(dir)) {
            addConsiderPoint(dir,0);
        }
        float degreeOffset = 20;
        int checksPerSide = 3;

        for(int check = 1; check <= checksPerSide; check++) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*check))) {
                addConsiderPoint(dir.rotateLeftDegrees(degreeOffset*check),0);
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*check))) {
                addConsiderPoint(dir.rotateRightDegrees(degreeOffset*check),0);
            }
        }
    }
    public void addLiniarPull(MapLocation loc,float value){
        float locdis = Math.max(loc.distanceTo(cen),speed);
        float dis_val = value / locdis;//normalizes on distance
        liniar_pull = new MapLocation(liniar_pull.x + (loc.x - cen.x) * dis_val,liniar_pull.y + (loc.y - cen.y) * dis_val);
    }
    public Direction bestLinDir(){
        Direction lin_dir = new MapLocation(0,0).directionTo(liniar_pull);
        return lin_dir;
    }
    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    boolean willCollideWithMe(BulletInfo bullet,MapLocation loc) {

        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        float distToRobot = bulletLocation.distanceTo(loc);

        // Get relevant bullet information
        if(distToRobot < body_rad){
            return true;
        }

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(loc);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= body_rad);
    }
    boolean blocked_loc(MapLocation loc) throws GameActionException {
        return !rc.onTheMap(loc) || rc.isLocationOccupiedByTree(loc);
    }
    void calc_avoid_trees() throws GameActionException {
        for(int level = 1; level <= 2; level++){
            int dir_split = Const.CIRC_SPLIT * level;
            float level_dis = body_rad + (speed * level);
            float AVD_TREE_EXP_L = Const.AVD_TREE_EXP_LEVEL[level];

            Direction dir = new Direction(1,0);
            //gets the whole thing started
            for(int d = 0; d < dir_split; d++){
                dir = dir.rotateLeftDegrees(360.0f / dir_split);
                if(!blocked_loc(cen.add(dir,level_dis))){
                    break;
                }
            }
            int count = 0;
            float val_avd_tree = Const.AVD_TREE_BASE_VAL;
            for(int d = 0; d < dir_split; d++){
                dir = dir.rotateLeftDegrees(360.0f / dir_split);
                MapLocation loc = cen.add(dir,level_dis);
                if(blocked_loc(loc)){
                    val_avd_tree *= AVD_TREE_EXP_L;
                    count += 1;
                }
                else{
                    if(count == 0){
                        continue;
                    }
                    float mid_dir_deg = (count * 0.5f * 360f) / dir_split;
                    Direction push_dir = dir.rotateLeftDegrees(-mid_dir_deg);
                    rc.setIndicatorDot(cen.add(push_dir),255,255,255);
                    addLiniarPull(cen.add(push_dir),-val_avd_tree);

                    count = 0;
                    val_avd_tree = Const.AVD_TREE_BASE_VAL;
                }
            }
        }
    }
}
