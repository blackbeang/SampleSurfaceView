package org.androidtown.mypaintlib;

import android.graphics.Bitmap;

/**
 * Created by ejchoi on 2017-03-13.
 */

public class PaintBrushDraw {
    public static void drawDabLine(final PBDrawDabData sDDData, Bitmap pDstBitmap, Bitmap pDabBitmap, int tx, int ty) {

        /*
        BrINT32 color_a = sDDData.color_a * 255;
        BrINT32 opaque = sDDData.opaque * 255;
        BrBYTE alpha = (BrBYTE)((color_a * opaque) / 0xFF);
        */
    }

    public static boolean renderDabMask(int[] mask, float x, float y, float radius, float hardness, float aspect_ratio, float angle) {

        return false;
    }
}


class PBDrawDabData {
    public float x;
    public float y;
    public float radius;
    public byte color_r;
    public byte color_g;
    public byte color_b;
    public float color_a;
    public float opaque;
    public float hardness;
    public float aspect_ratio;
    public float angle;
    public float lock_alpha;
    public float colorize;
    public float normal;
}
