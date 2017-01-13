package befplayer;

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
    boolean intersects(LinSeg other){
        for(LinSeg seg : segs){
            if(seg.intersects(other)){
                return true;
            }
        }
        return false;
    }
}
