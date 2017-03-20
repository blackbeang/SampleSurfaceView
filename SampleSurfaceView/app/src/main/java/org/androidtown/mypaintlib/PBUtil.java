package org.androidtown.mypaintlib;

/**
 * Created by ejchoi on 2017-03-13.
 */

import org.androidtown.mypaintlib.PaintBrushDefine.PaintBrushSetting;


public class PBUtil {
    public static boolean IS_ODD(int n) {
        int r = n & 1;
        return (r == 0)? false:true;
    }

    public static double MOD_SUM(double x, double y) {
        return (x+y) - (int)(x+y);  // (x+y) mod 1.0
    }

    public static int ROUND(float x) {
        return (int)(x + 0.5);
    }

    public static float SQR(float x) {
        return x * x;
    }

    public static float MAX3(float a, float b, float c) {
        return a > b? Math.max(a, c) : Math.max(b, c);
    }

    public static float MIN3(float a, float b, float c) {
        return a < b? Math.min(a, c) : Math.min(b, c);
    }

    public static float MINMAX(float a, float b, float c) {
        return Math.min(Math.max(a, b), c);
    }

    public static boolean CHECK_SETTING_ID(int id) {
        if(id >= 0 && id < PaintBrushSetting.PAINT_BRUSH_SETTINGS_COUNT.ordinal())
            return true;
        else {
            assert(false);
            return false;
        }
    }

    public static boolean CHECK_CPOINTS_ID(int id, int inputs) {
        if(inputs == 0)
            return false;

        if(id >= 0 && id < inputs)
            return true;
        else {
            assert(false);
            return false;
        }
    }

    public static boolean CHECK_CPOINTS_INDEX(int idx) {
        if(idx >= 0 && idx < PaintBrushDefine.PAINT_BRUSH_CPOINTS_COUNT)
            return true;
        else {
            assert(false);
            return false;
        }
    }

    public static boolean isFinite(float x) {
        return Float.isInfinite(x)? false:true;
    }

    public static float roundF(float x) {
        return x >= 0.0f? (float)Math.floor(x+0.5f) : (float)Math.ceil(x-0.5f);
    }

    public static final float PB_M_PI =  3.14159265358979323846f;

    public static final float ACTUAL_RADIUS_MIN = 0.2f;
    public static final float ACTUAL_RADIUS_MAX = 1000.0f;  // safety guard against radius like 1e20 and against rendering overload with unexpected brush dynamics
}
