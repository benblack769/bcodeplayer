package befplayer;

import battlecode.common.MapLocation;


public class Message {
    int mint;

    final static int plen = 10;
    final static int xstart = 0;
    final static int pmask = 0x3ff;
    final static int ystart = plen;
    final static int infostart = plen * 2;
    final static int info_len = 32 - infostart;
    public Message(int messint){
        mint = messint;
    }
    public MapLocation location(){
        int xint = mint & pmask;
        int yint = (mint >> plen) & pmask;
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
    static int getInt(int start,int mask,int data){
        return (start >> data) & mask;
    }




    //writing methods
    static int EncodeBoolean(boolean b){
        return b ? 1 : 0;
    }
    public static int EncodeMapLocInfo(MapLocation loc, int extra_info){
        return placeInt(xstart,plen,(int)loc.x) |
               placeInt(ystart,plen,(int)loc.y) |
               placeInt(infostart,info_len,extra_info);
    }
    public static int EncodeMapLoc(MapLocation loc){
        return EncodeMapLocInfo(loc,0);
    }
    static int placeInt(int loc, int len, int cont){
        if(cont >= (1 << len)){
            System.out.println("Message content exceeded bounds, undefined behavior!!!!\n\n\n\n");
        }
        return cont << loc;
    }
}
