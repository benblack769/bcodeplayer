package befplayer;

import battlecode.common.*;

final public class LinSeg{
    final MapLocation l1,l2;
    final float m,b;
    final float xmax,ymax,xmin,ymin;
    public LinSeg(MapLocation inl1, MapLocation inl2){
        l1 = inl1;
        l2 = inl2;

        float x1 = inl1.x;
        float y1 = inl1.y;

        float x2 = inl2.x;
        float y2 = inl2.y;

        m = (y2-y1)/(x2-x1);
        b = y1 - x1 * m;

        xmax = Math.max(x1,x2);
        ymax = Math.max(y1,y2);

        xmin = Math.min(x1,x2);
        ymin = Math.min(y1,y2);
    }
    boolean intersects(LinSeg other){
        float xi = (other.b - b)/(m - other.m);
        float yi = m*xi + b;
        return xi < xmax && xi > xmin &&
                yi < ymax && yi > ymin;
    }
}
