package benplayer;

import battlecode.common.*;

final public class Movement {
    final float speed;
    final float body_rad;
    final MapLocation cen;
    final float damage_value;
    final RobotType type;

    BodyInfo[] blocking_objs;

    MapLocation liniar_pull;

    public Movement(RobotController rc){
        type = rc.getType();
        speed = type.strideRadius;
        cen = rc.getLocation();
        body_rad = type.bodyRadius;
        damage_value = Const.damage_value(type);

        TreeInfo trees[] = rc.senseNearbyTrees();
        RobotInfo robots[] = rc.senseNearbyRobots();

        blocking_objs = new BodyInfo[trees.length + robots.length];
        liniar_pull = new MapLocation(0,0);

        System.arraycopy(trees, 0, blocking_objs, 0, trees.length);
        System.arraycopy(robots, 0, blocking_objs, trees.length, robots.length);
    }
    void addLiniarPull(MapLocation loc,float value){
        float locdis = loc.distanceTo(cen);
        float dis_val = value / locdis;//normalizes on distance
        liniar_pull = new MapLocation(liniar_pull.x + (loc.x - cen.x) * dis_val,liniar_pull.y + (loc.y - cen.y) * dis_val);
    }
    MapLocation bestPoint(){
        return cen.add(cen.directionTo(new MapLocation(cen.x + liniar_pull.x,cen.y + liniar_pull.y)),speed);
    }
    void calc_bullet_collision_values(BulletInfo[] bullets){
        for(BulletInfo bul : bullets){
            if(willCollideWithMe(bul,cen)){
                Direction perp_dir1 = bul.dir.rotateLeftDegrees(90);
                Direction perp_dir2 = bul.dir.rotateLeftDegrees(270);
                Direction fin_dir = cen.add(perp_dir1).distanceTo(bul.location) >
                                    cen.add(perp_dir2).distanceTo(bul.location)
                                        ? perp_dir1 : perp_dir2;
                addLiniarPull(cen.add(fin_dir),damage_value * bul.damage);
            }
        }
    }
    boolean willCollideWithMe(BulletInfo bullet,MapLocation loc) {
        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(loc);
        float distToRobot = bulletLocation.distanceTo(loc);
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

        return (perpendicularDist <= type.bodyRadius);
    }
}
