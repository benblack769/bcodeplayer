package befplayer;

import battlecode.common.*;

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

    //unit production constants
    final static int RAND_BUILD_TRIES = 20;

    //helper function
    static float area(float rad){
        return (float)(Math.PI) * rad * rad;
    }
    static float effectiveBulletCost(RobotType type){
        final float effective_bullet_cost = type == RobotType.ARCHON ? 1000 : type.bulletCost;
        return effective_bullet_cost;
    }
    static float sqr(float val){return val * val;}
    static float damageValue(RobotType type,float health){
        float low_health_val = 1 / (10 + health);
        float is_archon_bonus = type == RobotType.ARCHON ? 2 : 1;
        float bullet_cost_val = Const.effectiveBulletCost(type);

        return low_health_val * is_archon_bonus * bullet_cost_val;
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
