package benplayer;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;

/**
 * Created by benblack on 1/11/2017.
 */
public class Const {
    //action constatnts
    final static float LUMBER_TREE_AREA_HIRE_PERC = 0.33f;

    //movement value constants
    final static float TROOP_SPACE_VAL = 5f;
    final static float SMALL_RAND_VAL = 0.01f;
    final static float GAR_WAND_BLOCKED_VAL = 0.5f;
    final static float GAR_WAND_ARCHON_AVOID = 1.0f;
    final static float LUMBER_TREE_LOC_BASE = 1.0f;
    final static float WANDER_MOVE_ON_VAL = 0.5f;
    final static float AVD_TREE_BASE_VAL = 0.1f;

    //movement action constants
    final static float MOVE_EFFICIENCY = 0.9f;
    final static int WANDER_MEMORY_LENGTH = 2;
    final static int CIRC_SPLIT = 10;
    final static float[] AVD_TREE_EXP_LEVEL = {0,1.3f,1.13f};

    //helper function
    static float area(float rad){
        return (float)(Math.PI) * rad * rad;
    }
    static float damage_value(RobotType type){
        final float effective_bullet_cost = type == RobotType.ARCHON ? 1000 : type.bulletCost;
        return effective_bullet_cost/type.maxHealth;
    }
    static float sqr(float val){return val * val;}
}
