package befplayer;

/**
 * Created by benblack on 1/11/2017.
 */
public class Const {
    //action constatnts
    static float LUMBER_TREE_AREA_HIRE_PERC = 0.33f;

    //movement constants
    static float TROOP_SPACE_VAL = 5f;
    static float SMALL_RAND_VAL = 0.01f;
    static float GAR_WAND_BLOCKED_VAL = 0.5f;
    static float GAR_WAND_ARCHON_AVOID = 1.0f;
    static float LUMBER_TREE_LOC_BASE = 1.0f;

    static float area(float rad){
        return (float)(Math.PI) * rad * rad;
    }
}
