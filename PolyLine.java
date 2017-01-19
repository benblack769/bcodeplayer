package benplayer;

import battlecode.common.*;

import java.util.ArrayList;

final public class PolyLine {
    ArrayList<LinSeg> segs;
    public PolyLine(){
        segs = new ArrayList<LinSeg>();
    }
    void addBody(BodyInfo body,float extraRad){
        MapLocation cen = body.getLocation();
        float rad = body.getRadius() + extraRad;
        segs.add(new LinSeg(cen.add(Direction.getSouth(),rad),cen.add(Direction.getNorth(),rad)));
        segs.add(new LinSeg(cen.add(Direction.getWest(),rad),cen.add(Direction.getEast(),rad)));
    }
    void addSmallBody(BodyInfo body,float extraRad){
        MapLocation cen = body.getLocation();
        float rad = body.getRadius() + extraRad;
        Direction rand_dir = BaseRobot.randomDirection();
        segs.add(new LinSeg(cen.add(rand_dir,rad),cen.add(rand_dir.opposite(),rad)));
    }
    LinSeg bul_seg(BulletInfo bul,int turns){
        float dis = bul.speed * turns;
        MapLocation beg = bul.location;
        MapLocation end = beg.add(bul.dir,dis);
        return new LinSeg(beg,end);
    }
    boolean intersects(LinSeg other){
        for(LinSeg seg : segs){
            if(seg.intersects(other)){
                return true;
            }
        }
        return false;
    }
}
