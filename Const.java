package befplayer;

import battlecode.common.*;

public class Const {
    //action constatnts
    final static float LUMBER_TREE_AREA_HIRE_PERC = 0.33f;
    final static int SCOUT_PESTER_LENGTH = 15;
    final static int FIGHT_LENGTH = 30;
    final static int TROOPS_TO_FIGHT = 2;

    //round numbers
    final static int SOLDIER_DEF_ROUND = 120;


    //attackc constants
    final static float FRIENDLY_FIRE_VAL = 0.25f;
    final static float FIGHT_RADIUS = 12.0f;

    //movement value constants
    final static float TROOP_SPACE_VAL = 5f;
    final static float SMALL_RAND_VAL = 0.01f;
    final static float GAR_WAND_BLOCKED_VAL = 0.5f;
    final static float GAR_WAND_ARCHON_AVOID = 1.0f;
    final static float LUMBER_TREE_LOC_BASE = 1.0f;
    final static float WANDER_MOVE_ON_VAL = 1.5f;
    final static float AVD_TREE_BASE_VAL = 0.1f;
    final static float SCOUT_CHASE_SCOUT_VAL = 1.0f;
    final static float MOVE_TO_FIGHT_VAL = 0.3f;
    final static float BULLET_TREE_VAL = 0.1f;

    //movement action constants
    final static float MOVE_EFFICIENCY = 0.9f;
    final static int WANDER_MEMORY_LENGTH = 2;
    final static int CIRC_SPLIT = 10;
    final static float[] AVD_TREE_EXP_LEVEL = {0,1.3f,1.13f};

    //unit production constants
    final static int RAND_BUILD_TRIES = 20;
    final static int MIN_TREE_OPENINGS = 4;

    //message indicies
    final static int IS_DENSE_MAP = 0;
    final static int SCOUTS_PESTERING = 1;
    final static int SCOUTS_PESTERING_TURN = 2;
    final static int FIGHT_START_LOC = 10;
    final static int MAX_NUM_FIGHTS = 3;

    //helper function
    static float area(float rad){
        return (float)(Math.PI) * rad * rad;
    }
    static float effectiveBulletCost(RobotType type){
        final float effective_bullet_cost = type == RobotType.ARCHON ? 600.0f : type.bulletCost;
        return effective_bullet_cost;
    }
    static float sqr(float val){return val * val;}
    static float damageValue(RobotType type,float health){
        float low_health_val = 1.0f / (10.0f + health);
        float bullet_cost_val = Const.effectiveBulletCost(type);
        float is_scout_bonus = type == RobotType.SCOUT ? 2f : 1;

        return low_health_val * bullet_cost_val * is_scout_bonus;
    }
    static float chase_val(RobotType chaser_ty,float chaser_h,MapLocation chaser_loc,RobotType chased_ty,float chased_h,MapLocation chased_loc){
        if(!chaser_ty.canAttack()){
            return 0;
        }
        float attack_val = chaser_ty.attackPower;
        float bul_speeed_val = sqr(chaser_ty.bulletSpeed);
        float damage_value = damageValue(chased_ty,chased_h);
        return attack_val * bul_speeed_val * damage_value;
    }
}
