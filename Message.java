package benplayer;

import battlecode.common.MapLocation;


public class Message {
    int mint;

    final static int plen = 10;
    final static int xstart = 0;
    final static int ystart = plen;
    final static int info_len = 32 - 2*plen;
    final static int infostart = plen * 2;
    public Message(int messint){
        mint = messint;
    }
    public MapLocation location(){
        int xint = getInt(xstart,plen,mint);
        int yint = getInt(ystart,plen,mint);
        return new MapLocation(xint,yint);
    }
    public int extraInfo(){
        return getInt(infostart,info_len,mint);
    }
    public boolean nonEmpty(){
        return mint != 0;
    }
    public int rawInfo(){
        return mint;
    }
    static int getInt(int start,int len,int data){
        int mydata = start >> data;
        int not_my_data = (len >> mydata) << len;
        return mydata ^ not_my_data;
    }




    //writing methods
    static int EncodeBoolean(boolean b){
        return b ? 1 : 0;
    }
    public static int EncodeMapLocInfo(MapLocation loc, int extra_info){
        return placeInt(xstart,plen,(int)loc.x,
               placeInt(ystart,plen,(int)loc.y,
               placeInt(infostart,info_len,extra_info,0)));
    }
    public static int EncodeMapLoc(MapLocation loc){
        return EncodeMapLocInfo(loc,0);
    }
    static int placeInt(int loc, int len, int cont,int into){
        if(cont > (1 >> len)){
            System.out.println("Message content exceeded bounds, undefined effect!!!!\n\n");
        }
        return into | (cont << loc);
    }
}
