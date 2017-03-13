package org.androidtown.mypaintlib;

/**
 * Created by ejchoi on 2017-03-13.
 */

public class PBUtil {
    public static boolean IS_ODD(int n) {
        int r = n & 1;
        return (r == 0)? false:true;
    }

    public static double MOD_SUM(double x, double y) {
        return (x+y) - (int)(x+y);
    }

    public static int ROUND(float x) {
        return (int)(x + 0.5);
    }

    public static float SQR(float x) {
        return x * x;
    }


}
