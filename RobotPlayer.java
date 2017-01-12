package benplayer;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static BaseRobot robot;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        init(rc);
        while(true){
            try {
                robot.run();
            }
            catch (Exception e) {
                System.out.println("Exception:");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }
    static void init(RobotController rc){
        switch (rc.getType()) {
            case ARCHON:
                robot = new Archon(rc);
                break;
            case GARDENER:
                robot = new Gardener(rc);
                break;
            case SOLDIER:
                robot = new Soldier(rc);
                break;
            case LUMBERJACK:
                robot = new Lumberjack(rc);
                break;
        }
    }

}
