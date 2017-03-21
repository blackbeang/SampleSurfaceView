package org.androidtown.mypaintlib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Arrays;

/**
 * Created by ejchoi on 2017-03-13.
 */

public class PaintBrushDraw {
    public static void drawDabLine(final PBDrawDabData sDDData, Canvas pDstCanvas, int[] pDabPixel, int tx, int ty) {
        if(pDstCanvas == null | pDabPixel == null) {
            assert(false);
            return;
        }

        Arrays.fill(pDabPixel, 0);

        if(renderDabMask(pDabPixel, sDDData.x - tx*PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE, sDDData.y - ty*PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE,  sDDData.radius, sDDData.hardness, sDDData.aspect_ratio, sDDData.angle, sDDData.color_r, sDDData.color_g, sDDData.color_b))
        {
            Bitmap sDabBitmap = Bitmap.createBitmap(pDabPixel, PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE, PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE, Bitmap.Config.ARGB_8888);
            Paint sPaint = new Paint();

            int dx = (tx*PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE);
            int dy = (ty*PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE);

            if(sDDData.color_a == 1.0 && sDDData.opaque == 1.0)
                sPaint.setAlpha(255);
            else {
                int color_a = (int)(sDDData.color_a * 255);
                int opaque = (int)(sDDData.opaque * 255);
                byte alpha = (byte)((color_a * opaque) / 0xFF);
                if(alpha == 0)
                    return;
                sPaint.setAlpha(alpha);
            }

            pDstCanvas.drawBitmap(sDabBitmap, dx, dy, sPaint);
        }
    }

    public static boolean renderDabMask(int[] pDabPixel, float x, float y, float radius, float hardness, float aspect_ratio, float angle,
                                        byte color_r, byte color_g, byte color_b) {
        if(pDabPixel == null) {
            assert(false);
            return false;
        }

        hardness = PBUtil.MINMAX(0.0f, hardness, 1.0f);
        if(aspect_ratio < 1.0f)
            aspect_ratio = 1.0f;
        assert(hardness != 0.0f);	// assured by caller

        // The hardness calculation is explained below:
        // Dab opacity gradually fades out from the center (rr=0) to fringe (rr=1) of the dab
        // How exactly depends on the hardness
        // We use two linear segments, for which we pre-calculate slope and offset here

        // opa
        // ^
        // *   .
        // |        *
        // |          .
        // +-----------*> rr = (distance_from_center/radius)^2
        // 0           1
        //

        //BrINT32 tile_size = radius * 2;
        float segment1_offset = 1.0f;
        float segment1_slope  = -(1.0f/hardness - 1.0f);
        float segment2_offset = hardness / (1.0f-hardness);
        float segment2_slope  = -hardness / (1.0f-hardness);

        float angle_rad = angle/360*2*PBUtil.PB_M_PI;
        float cs = (float)Math.cos(angle_rad);
        float sn = (float)Math.sin(angle_rad);

	    final float r_fringe = radius + 1.0f; // +1.0 should not be required, only to be sure
        int x0 = (int)Math.floor(x - r_fringe);
        int y0 = (int)Math.floor(y - r_fringe);
        int x1 = (int)Math.floor(x + r_fringe);
        int y1 = (int)Math.floor(y + r_fringe);
        if(x0 < 0)
            x0 = 0;
        if(y0 < 0)
            y0 = 0;
        if(x1 > PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE-1)
            x1 = PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE-1;
        if(y1 > PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE-1)
            y1 = PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE-1;

	    final float one_over_radius2 = 1.0f / (radius*radius);

        // Pre-calculate rr and put it in the mask
        // This an optimization that makes use of auto-vectorization
        // OPTIMIZE: if using floats for the brush engine, store these directly in the mask
        float[] rr_mask = new float[PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE * PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE];

        if(radius < 3.0f)
        {
		    final float aa_border = 1.0f;
            float r_aa_start = ((radius>aa_border)? (radius-aa_border):0);
            r_aa_start *= r_aa_start / aspect_ratio;

            for(int yp = y0; yp <= y1; yp++)
            {
                for(int xp = x0; xp <= x1; xp++)
                {
				    final float rr = PBMath.calculateRrAntialiased(xp, yp, x, y, aspect_ratio, sn, cs, one_over_radius2, r_aa_start);
                    rr_mask[(yp*PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE)+xp] = rr;
                }
            }
        }
        else
        {
            for(int yp = y0; yp <= y1; yp++)
            {
                for(int xp = x0; xp <= x1; xp++)
                {
				    final float rr = PBMath.calculateRr(xp, yp, x, y, aspect_ratio, sn, cs, one_over_radius2);
                    rr_mask[(yp*PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE)+xp] = rr;
                }
            }
        }

        for(int yp = y0; yp <= y1; yp++)
        {
            int xp;
            for(xp = x0; xp <= x1; xp++)
            {
			    final float rr = rr_mask[(yp*PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE)+xp];
			    final float opa = PBMath.calculateOpa(rr, hardness, segment1_offset, segment1_slope, segment2_offset, segment2_slope);
			    final byte newAlpha = (byte)(opa * 255);

                int newColor = Color.argb(newAlpha, color_r, color_g, color_b) & 0x00FFFFFF;
                int newMask = newAlpha << 24;
                pDabPixel[(yp*PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE)+xp] = newColor | newMask;
            }
        }

        int sx = x0, sy = y0, scx = (x1-x0) + 1, scy = (y1-y0) + 1;
        if(scx <= 0 || scy <= 0 || scx > PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE || scy > PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE)
        {
            assert(false);
            return false;
        }
        else
            return true;
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
