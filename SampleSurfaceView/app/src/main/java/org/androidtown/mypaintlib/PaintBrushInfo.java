package org.androidtown.mypaintlib;

/**
 * Created by BlackBean on 2017-03-13.
 */

public class PaintBrushInfo {
    private boolean bConstant;
    private float fMin;
    private float fDefault;
    private float fMax;

    public void set(boolean bConstant, float fMin, float fDefault, float fMax) {
        this.bConstant = bConstant;
        this.fMin = fMin;
        this.fDefault = fDefault;
        this.fMax = fMax;
    }
}


class DefaultPaintBrushInfo {
    private PaintBrushInfo[] arrDefaultPaintBrush;

    public DefaultPaintBrushInfo() {
        arrDefaultPaintBrush = new PaintBrushInfo[45];
        arrDefaultPaintBrush[0].set(false, 0.0f, 1.0f, 2.0f);// opaque
        arrDefaultPaintBrush[1].set(false, 0.0f, 0.0f, 2.0f);// opaque_multiply
        arrDefaultPaintBrush[2].set(true, 0.0f, 0.9f, 2.0f);// opaque_linearize
        arrDefaultPaintBrush[3].set(false, -2.0f, 2.0f, 6.0f);// radius_logarithmic
        arrDefaultPaintBrush[4].set(false, 0.0f, 0.8f, 1.0f);// hardness
        arrDefaultPaintBrush[5].set(false, 0.0f, 1.0f, 5.0f);// anti_aliasing
        arrDefaultPaintBrush[6].set(true, 0.0f, 0.0f, 6.0f);// dabs_per_basic_radius
        arrDefaultPaintBrush[7].set(true, 0.0f, 2.0f, 6.0f);// dabs_per_actual_radius
        arrDefaultPaintBrush[8].set(true, 0.0f, 0.0f, 80.0f);// dabs_per_second
        arrDefaultPaintBrush[9].set(false, 0.0f, 0.0f, 1.5f);// radius_by_random
        arrDefaultPaintBrush[10].set(false, 0.0f, 0.04f, 0.2f);// speed1_slowness(Fine speed filter)
        arrDefaultPaintBrush[11].set(false, 0.0f, 0.8f, 3.0f);// speed2_slowness(Gross speed filter)
        arrDefaultPaintBrush[12].set(true, -8.0f, 4.0f, 8.0f);// speed1_gamma(Fine speed gamma)
        arrDefaultPaintBrush[13].set(true, -8.0f, 4.0f, 8.0f);// speed2_gamma(Gross speed gamma)
        arrDefaultPaintBrush[14].set(false, 0.0f, 0.0f, 25.0f);// offset_by_random(Jitter)
        arrDefaultPaintBrush[15].set(false, -3.0f, 0.0f, 3.0f);// offset_by_speed
        arrDefaultPaintBrush[16].set(false, 0.0f, 1.0f, 15.0f);// offset_by_speed_slowness
        arrDefaultPaintBrush[17].set(true, 0.0f, 0.0f, 10.0f);// slow_tracking
        arrDefaultPaintBrush[18].set(false, 0.0f, 0.0f, 10.0f);	// slow_tracking_per_dab
        arrDefaultPaintBrush[19].set(true, 0.0f, 0.0f, 12.0f);// tracking_noise
        arrDefaultPaintBrush[20].set(true, 0.0f, 0.0f, 1.0f);// color_h(hue)
        arrDefaultPaintBrush[21].set(true, -0.5f, 0.0f, 1.5f);// color_s(saturation)
        arrDefaultPaintBrush[22].set(true, -0.5f, 0.0f, 1.5f);// color_v(value)
        arrDefaultPaintBrush[23].set(true, 0.0f, 0.0f, 1.0f);// restore_color
        arrDefaultPaintBrush[24].set(false, -2.0f, 0.0f, 2.0f);// change_color_h
        arrDefaultPaintBrush[25].set(false, -2.0f, 0.0f, 2.0f);// change_color_l(lightness)(HSL)
        arrDefaultPaintBrush[26].set(false, -2.0f, 0.0f, 2.0f);// change_color_hsl_s(HSL)
        arrDefaultPaintBrush[27].set(false, -2.0f, 0.0f, 2.0f);// change_color_v(HSV)
        arrDefaultPaintBrush[28].set(false, -2.0f, 0.0f, 2.0f);// change_color_hsv_s(HSV)
        arrDefaultPaintBrush[29].set(false, 0.0f, 0.0f, 1.0f);// smudge
        arrDefaultPaintBrush[30].set(false, 0.0f, 0.5f, 1.0f);// smudge_length
        arrDefaultPaintBrush[31].set(false, -1.6f, 0.0f, 1.6f);// smudge_radius_log
        arrDefaultPaintBrush[32].set(false, 0.0f, 0.0f, 1.0f);// eraser
        arrDefaultPaintBrush[33].set(true, 0.0f, 0.0f, 0.5f);// stroke_threshold
        arrDefaultPaintBrush[34].set(false, -1.0f, 4.0f, 7.0f);// stroke_duration_logarithmic
        arrDefaultPaintBrush[35].set(false, 0.0f, 0.0f, 10.0f);// stroke_holdtime
        arrDefaultPaintBrush[36].set(false, -5.0f, 0.0f, 5.0f);// custom_input
        arrDefaultPaintBrush[37].set(false, 0.0f, 0.0f, 10.0f);// custom_input_slowness
        arrDefaultPaintBrush[38].set(false, 1.0f, 1.0f, 10.0f);// elliptical_dab_ratio
        arrDefaultPaintBrush[39].set(false, 0.0f, 90.0f, 180.0f);// elliptical_dab_angle
        arrDefaultPaintBrush[40].set(false, 0.0f, 2.0f, 10.0f);// direction_filter
        arrDefaultPaintBrush[41].set(false, 0.0f, 0.0f, 1.0f);// lock_alpha
        arrDefaultPaintBrush[42].set(false, 0.0f, 0.0f, 1.0f);// colorize
        arrDefaultPaintBrush[43].set(false, 0.0f, 0.0f, 1.0f);// snap_to_pixel
        arrDefaultPaintBrush[44].set(true, -1.8f, 0.0f, 1.8f);// pressure_gain_log
    }
}


class CharcoalPaintBrushInfo {
    private PaintBrushInfo[] arrCharcoalPaintBrush;

    public CharcoalPaintBrushInfo() {
        arrCharcoalPaintBrush = new PaintBrushInfo[45];
        // add code
    }
}


class ShortGrassPaintBrushInfo {
    private PaintBrushInfo[] arrShortGrassPaintBrush;

    public ShortGrassPaintBrushInfo() {
        arrShortGrassPaintBrush = new PaintBrushInfo[45];
        // add code
    }
}


class WatercolorExpressivePaintBrushInfo {
    private PaintBrushInfo[] arrWatercolorExpressivePaintBrush;

    public WatercolorExpressivePaintBrushInfo() {
        arrWatercolorExpressivePaintBrush = new PaintBrushInfo[45];
        // add code
    }
}


class LeavesPaintBrushInfo {
    private PaintBrushInfo[] arrLeavesPaintBrush;

    public LeavesPaintBrushInfo() {
        arrLeavesPaintBrush = new PaintBrushInfo[45];
        arrLeavesPaintBrush[0].set(false, 0.0f, 1.0f, 2.0f);// opaque
        arrLeavesPaintBrush[1].set(false, 0.0f, 0.0f, 2.0f);// opaque_multiply
        arrLeavesPaintBrush[2].set(true, 0.0f, 0.0f, 2.0f);// opaque_linearize
        arrLeavesPaintBrush[3].set(false, -2.0f, 1.89f, 6.0f);// radius_logarithmic
        arrLeavesPaintBrush[4].set(false, 0.0f, 0.8f, 1.0f);// hardness
        arrLeavesPaintBrush[5].set(false, 0.0f, 1.0f, 5.0f);// anti_aliasing
        arrLeavesPaintBrush[6].set(true, 0.0f, 0.0f, 6.0f);// dabs_per_basic_radius
        arrLeavesPaintBrush[7].set(true, 0.0f, 2.0f, 6.0f);// dabs_per_actual_radius
        arrLeavesPaintBrush[8].set(true, 0.0f, 0.0f, 80.0f);	// dabs_per_second
        arrLeavesPaintBrush[9].set(false, 0.0f, 0.0f, 1.5f);// radius_by_random
        arrLeavesPaintBrush[10].set(false, 0.0f, 0.04f, 0.2f);// speed1_slowness(Fine speed filter)
        arrLeavesPaintBrush[11].set(false, 0.0f, 0.8f, 3.0f);// speed2_slowness(Gross speed filter)
        arrLeavesPaintBrush[12].set(true, -8.0f, -7.16f, 8.0f);// speed1_gamma(Fine speed gamma)
        arrLeavesPaintBrush[13].set(true, -8.0f, 4.0f, 8.0f);// speed2_gamma(Gross speed gamma)
        arrLeavesPaintBrush[14].set(false, 0.0f, 0.0f, 25.0f);// offset_by_random(Jitter)
        arrLeavesPaintBrush[15].set(false, -3.0f, 0.0f, 3.0f);// offset_by_speed
        arrLeavesPaintBrush[16].set(false, 0.0f, 1.0f, 15.0f);// offset_by_speed_slowness
        arrLeavesPaintBrush[17].set(true, 0.0f, 0.0f, 10.0f);// slow_tracking
        arrLeavesPaintBrush[18].set(false, 0.0f, 0.0f, 10.0f);// slow_tracking_per_dab
        arrLeavesPaintBrush[19].set(true, 0.0f, 0.0f, 12.0f);// tracking_noise
        arrLeavesPaintBrush[20].set(true, 0.0f, 0.0f, 1.0f);	// color_h(hue)
        arrLeavesPaintBrush[21].set(true, -0.5f, 0.0f, 1.5f);// color_s(saturation)
        arrLeavesPaintBrush[22].set(true, -0.5f, 0.0f, 1.5f);// color_v(value)
        arrLeavesPaintBrush[23].set(true, 0.0f, 0.0f, 1.0f);	// restore_color
        arrLeavesPaintBrush[24].set(false, -2.0f, 0.0f, 2.0f);// change_color_h
        arrLeavesPaintBrush[25].set(false, -2.0f, 0.0f, 2.0f);// change_color_l(lightness)(HSL)
        arrLeavesPaintBrush[26].set(false, -2.0f, 0.0f, 2.0f);// change_color_hsl_s(HSL)
        arrLeavesPaintBrush[27].set(false, -2.0f, 0.195f, 2.0f);// change_color_v(HSV)
        arrLeavesPaintBrush[28].set(false, -2.0f, 0.0f, 2.0f);// change_color_hsv_s(HSV)
        arrLeavesPaintBrush[29].set(false, 0.0f, 0.0f, 1.0f);// smudge
        arrLeavesPaintBrush[30].set(false, 0.0f, 0.5f, 1.0f);// smudge_length
        arrLeavesPaintBrush[31].set(false, -1.6f, 0.0f, 1.6f);// smudge_radius_log
        arrLeavesPaintBrush[32].set(false, 0.0f, 0.0f, 1.0f);// eraser
        arrLeavesPaintBrush[33].set(true, 0.0f, 0.0f, 0.5f);	// stroke_threshold
        arrLeavesPaintBrush[34].set(false, -1.0f, 2.76f, 7.0f);// stroke_duration_logarithmic
        arrLeavesPaintBrush[35].set(false, 0.0f, 10.0f, 10.0f);// stroke_holdtime
        arrLeavesPaintBrush[36].set(false, -5.0f, 0.0f, 5.0f);// custom_input
        arrLeavesPaintBrush[37].set(false, 0.0f, 0.0f, 10.0f);// custom_input_slowness
        arrLeavesPaintBrush[38].set(false, 1.0f, 1.0f, 10.0f);// elliptical_dab_ratio
        arrLeavesPaintBrush[39].set(false, 0.0f, 90.0f, 180.0f);// elliptical_dab_angle
        arrLeavesPaintBrush[40].set(false, 0.0f, 2.0f, 10.0f);// direction_filter
        arrLeavesPaintBrush[41].set(false, 0.0f, 0.0f, 1.0f);// lock_alpha
        arrLeavesPaintBrush[42].set(false, 0.0f, 0.0f, 1.0f);// colorize
        arrLeavesPaintBrush[43].set(false, 0.0f, 0.0f, 1.0f);// snap_to_pixel
        arrLeavesPaintBrush[44].set(true, -1.8f, 0.0f, 1.8f);// pressure_gain_log
    }
}


class CalligraphyPaintBrushInfo {
    private PaintBrushInfo[] arrCharcoalPaintBrush;

    public CalligraphyPaintBrushInfo() {
        arrCharcoalPaintBrush = new PaintBrushInfo[45];
        // add code
    }
}


class GlowPaintBrushInfo {
    private PaintBrushInfo[] arrGlowPaintBrush;

    public GlowPaintBrushInfo() {
        arrGlowPaintBrush = new PaintBrushInfo[45];
        // add code
    }
}
