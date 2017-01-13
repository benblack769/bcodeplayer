package benplayer;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;

/**
 * Created by benblack on 1/11/2017.
 */
public class Const {
    //action constatnts
    static float LUMBER_TREE_AREA_HIRE_PERC = 0.33f;

    //movement value constants
    static float TROOP_SPACE_VAL = 5f;
    static float SMALL_RAND_VAL = 0.01f;
    static float GAR_WAND_BLOCKED_VAL = 0.5f;
    static float GAR_WAND_ARCHON_AVOID = 1.0f;
    static float LUMBER_TREE_LOC_BASE = 1.0f;
    static float WANDER_MOVE_ON_VAL = 0.5f;

    //movement action constants
    static float MOVE_EFFICIENCY = 0.9f;
    static int WANDER_MEMORY_LENGTH = 2;

    //helper function
    static float area(float rad){
        return (float)(Math.PI) * rad * rad;
    }
    static float damage_value(RobotType type){
        final float effective_bullet_cost = type == RobotType.ARCHON ? 1000 : type.bulletCost;
        return effective_bullet_cost/type.maxHealth;
    }
}
