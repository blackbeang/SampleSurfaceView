package org.androidtown.mypaintlib;

/**
 * Created by ejchoi on 2017-03-13.
 */

import org.androidtown.mypaintlib.PaintBrushDefine.PaintBrushSetting;
import org.androidtown.mypaintlib.PaintBrushDefine.PaintBrushInput;


public class PaintBrushSetter {
    public static void DefaultPaintBrushSetting(PaintBrush sPaintBrush) {
        DefaultPaintBrushInfo sDefaultPaintBrushInfo = new DefaultPaintBrushInfo();
        for(int s = 0; s < PaintBrushSetting.PAINT_BRUSH_SETTINGS_COUNT.ordinal(); s++)
        {
            for(int i = 0; i < PaintBrushInput.PAINT_BRUSH_INPUTS_COUNT.ordinal(); i++)
                sPaintBrush.setMappingN(s, i, 0);

            sPaintBrush.setBaseValue(s, sDefaultPaintBrushInfo.getDefaultSettingInfo(s).fDefault);
        }

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE_MULTIPLY.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal(), 2);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE_MULTIPLY.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal(), 0, 0.0f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE_MULTIPLY.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal(), 1, 1.0f, 1.0f);
    }

    public static void CharcoalPaintBrushSetting(PaintBrush paintBrush) {
        // add code
    }

    public static void ShortGrassPaintBrushSetting(PaintBrush paintBrush) {
        // add code
    }

    public static void WatercolorExpressivePaintBrushSetting(PaintBrush paintBrush) {
        // add code
    }

    public static void LeavesPaintBrushSetting(PaintBrush sPaintBrush) {
        LeavesPaintBrushInfo sLeavesPaintBrushInfo = new LeavesPaintBrushInfo();
        for(int s = 0; s < PaintBrushSetting.PAINT_BRUSH_SETTINGS_COUNT.ordinal(); s++)
        {
            for(int i = 0; i < PaintBrushInput.PAINT_BRUSH_INPUTS_COUNT.ordinal(); i++)
                sPaintBrush.setMappingN(s, i, 0);

            sPaintBrush.setBaseValue(s, sLeavesPaintBrushInfo.getLeavesSettingInfo(s).fDefault);
        }

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_SPEED1.ordinal(), 2);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_SPEED1.ordinal(), 0, 0.0f, -0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_SPEED1.ordinal(), 1, 1.0f, -0.12f);

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 5);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 0, 0.0f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 1, 0.651235f, -0.007889f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 2, 0.70679f, 0.115704f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 3, 0.891975f, 0.101241f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 4, 1.0f, -0.0f);

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_HSV_S.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 2);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_HSV_S.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 0, 0.0f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_HSV_S.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 1, 1.0f, 1.69f);

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal(), 2);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal(), 0, 0.0f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal(), 1, 1.0f, -0.57f);

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_RANDOM.ordinal(), 2);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_RANDOM.ordinal(), 0, 0.0f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_RANDOM.ordinal(), 1, 1.0f, 0.355f);

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 5);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 0, 0.0f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 1, 0.080247f, -0.150625f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 2, 0.614198f, 0.150625f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 3, 0.753086f, 0.351458f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 4, 1.0f, 0.476979f);

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 3);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 0, 0.0f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 1, 0.62963f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 2, 1.0f, -1.0f);

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE_MULTIPLY.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal(), 2);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE_MULTIPLY.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal(), 0, 0.0f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE_MULTIPLY.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal(), 1, 1.0f, 1.0f);

        sPaintBrush.setMappingN(PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 5);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 0, 0.0f, 0.0f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 1, 0.598765f, -1.4175f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 2, 0.709877f, 0.227813f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 3, 0.898148f, -0.227813f);
        sPaintBrush.setMappingPoint(PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal(), PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal(), 4, 1.0f, -2.43f);
    }

    public static void CalligraphyPaintBrushSetting(PaintBrush paintBrush) {
        // add code
    }

    public static void GlowPaintBrushSetting(PaintBrush paintBrush) {
        // add code
    }
}
