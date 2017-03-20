package org.androidtown.mypaintlib;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LongSparseArray;

import java.util.Arrays;

import org.androidtown.mypaintlib.PaintBrushDefine.PaintBrushState;
import org.androidtown.mypaintlib.PaintBrushDefine.PaintBrushSetting;
import org.androidtown.mypaintlib.PaintBrushDefine.PaintBrushInput;

/**
 * Created by ejchoi on 2017-03-13.
 */

// The Brush class stores two things:
//	a) states: modified during a stroke (eg. speed, smudge colors, time/distance to next dab, position filter states)
//	b) settings: constant during a stroke (eg. size, spacing, dynamics, color selected by the user)
//	FIXME: Actually those are two orthogonal things. Should separate them:
//		a) brush settings class that is saved/loaded/selected  (without states)
//		b) brush core class to draw the dabs (using an instance of the above)
public class PaintBrush {
    private static final String PB_DEBUG_TAG = "[LOG_PaintBrush]";

    private Bitmap m_pBitmap;
    private Bitmap m_DabBitmap;

    // for stroke splitting (undo/redo)
    private double m_dStrokeTotalPaintingTime;
    private double m_dStrokeCurrentIdlingTime;

    // the states (get_state, set_state, reset) that change during a stroke
    private float[] m_arrStates;
    private PBRgnDouble m_Rng;

    // Those mappings describe how to calculate the current value for each setting
    // Most of settings will be constant (eg. only their base_value is used)
    private PBMapping[] m_pArrSettings;

    // the current value of all settings (calculated using the current state)
    private float[] m_arrSettingsValue;

    // cached calculation results
    private float[] m_arrSpeedMappingGamma;
    private float[] m_arrSpeedMappingM;
    private float[] m_arrSpeedMappingQ;

    boolean	m_bResetRequested;


    public PaintBrush() {
        m_DabBitmap = Bitmap.createBitmap(PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE, PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE, Bitmap.Config.ARGB_8888);

        m_dStrokeTotalPaintingTime = 0;
        m_dStrokeCurrentIdlingTime = 0;

        m_arrStates = new float[PaintBrushState.PAINT_BRUSH_STATES_COUNT.ordinal()];
        Arrays.fill(m_arrStates, 0);
        m_Rng = new PBRgnDouble(1000);

        int settingsCount = PaintBrushSetting.PAINT_BRUSH_SETTINGS_COUNT.ordinal();
        m_pArrSettings = new PBMapping[settingsCount];
        for(int i = 0; i < settingsCount; i++)
            m_pArrSettings[i] = new PBMapping(PaintBrushInput.PAINT_BRUSH_INPUTS_COUNT.ordinal());

        m_arrSettingsValue = new float[settingsCount];

        m_arrSpeedMappingGamma = new float[PaintBrushDefine.PAINT_BRUSH_CALC_COUNT];
        m_arrSpeedMappingM = new float[PaintBrushDefine.PAINT_BRUSH_CALC_COUNT];
        m_arrSpeedMappingQ = new float[PaintBrushDefine.PAINT_BRUSH_CALC_COUNT];

        m_bResetRequested = true;

        settingsBaseValuesHaveChanged();
    }

    // setting 값이 바뀔때마다 새로 계산되기 위해서 호출해주고 있는 것 같음
    private void settingsBaseValuesHaveChanged() {
        // precalculate stuff that does not change dynamically

        // Precalculate how the physical speed will be mapped to the speed input value
        // The forumla for this mapping is:

        // y = log(gamma+x)*m + q;

        // x: the physical speed (pixels per basic dab radius)
        // y: the speed input that will be reported
        // gamma: parameter set by ths user (small means a logarithmic mapping, big linear)
        // m, q: parameters to scale and translate the curve

        // The code below calculates m and q given gamma and two hardcoded constraints

        float gamma, fix1_x, fix1_y, fix2_x, fix2_dy, m, q, c1;
        for(int i = 0; i < PaintBrushDefine.PAINT_BRUSH_CALC_COUNT; i++)
        {
            int index = PaintBrushSetting.PAINT_BRUSH_SETTING_SPEED2_GAMMA.ordinal();
            if(i == 0)
                index = PaintBrushSetting.PAINT_BRUSH_SETTING_SPEED1_GAMMA.ordinal();

            gamma = m_pArrSettings[index].getBaseValue();
            gamma = (float)Math.exp(gamma);

            fix1_x	= 45.0f;
            fix1_y	= 0.5f;
            fix2_x	= 45.0f;
            fix2_dy	= 0.015f;

            c1 = (float)Math.log(fix1_x+gamma);
            m = fix2_dy * (fix2_x + gamma);
            q = fix1_y - m*c1;

            m_arrSpeedMappingGamma[i] = gamma;
            m_arrSpeedMappingM[i] = m;
            m_arrSpeedMappingQ[i] = q;
        }
    }

    public void setBitmap(Bitmap bitmap) {
        m_pBitmap = bitmap;
    }

    public void setMappingN(int id, int input, int n) {
        if(PBUtil.CHECK_SETTING_ID(id) == false)
            return;
        m_pArrSettings[id].setN(input, n);
    }

    public void setMappingPoint(int id, int input, int index, float x, float y) {
        if(PBUtil.CHECK_SETTING_ID(id) == false)
            return;
        m_pArrSettings[id].setPoint(input, index, x, y);
    }

    public void setBaseValue(int id, float value) {
        if(PBUtil.CHECK_SETTING_ID(id) == false)
            return;
        m_pArrSettings[id].setBaseValue(value);
        settingsBaseValuesHaveChanged();
    }


    // Should be called once for each motion event
    // @dtime: Time since last motion event, in seconds
    // Returns: TRUE if the stroke is finished or empty, else FALSE
    public boolean strokeTo(float x, float y, float pressure, float xtilt, float ytilt, float dtime) {
        float tilt_ascension = 0.0f;
        float tilt_declination = 90.0f;

        if(xtilt != 0 || ytilt != 0)
        {
            // shield us from insane tilt input
            xtilt = PBUtil.MINMAX(-1.0f, xtilt, 1.0f);
            ytilt = PBUtil.MINMAX(-1.0f, ytilt, 1.0f);
            assert(PBUtil.isFinite(xtilt) && PBUtil.isFinite(ytilt));

            tilt_ascension = 180.0f * (float)Math.atan2(-xtilt, ytilt)/PBUtil.PB_M_PI;
		    final float rad = (float)Math.hypot(xtilt, ytilt);
            tilt_declination = 90 - (rad*60);

            assert(PBUtil.isFinite(tilt_ascension));
            assert(PBUtil.isFinite(tilt_declination));
        }

        if(pressure <= 0.0)
            pressure = 0.0f;

        if(!PBUtil.isFinite(x) || !PBUtil.isFinite(y) ||
                (x > 1e10 || y > 1e10 || x < -1e10 || y < -1e10))
        {
            Log.i(PB_DEBUG_TAG, "[PB_TRACE]Warning: ignoring brush::strokeTo with insane inputs (x = " + x + ", y = " + y + ")");
            x = 0.0f;
            y = 0.0f;
            pressure = 0.0f;
        }
        // the assertion below is better than out-of-memory later at save time
        assert(x < 1e8 && y < 1e8 && x > -1e8 && y > -1e8);

        if(dtime < 0) {
            Log.i(PB_DEBUG_TAG, "[PB_TRACE]Time jumped backwards by dtime = " + dtime + " seconds!");
        }

        if(dtime <= 0.0f)
            dtime = 0.0001f; // protect against possible division by zero bugs

        if(dtime > 0.100 && pressure != 0 && m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_PRESSURE.ordinal()] == 0)
        {
            // Workaround for tablets that don't report motion events without pressure
            // This is to avoid linear interpolation of the pressure between two events
            strokeTo(x, y, 0.0f, 90.0f, 0.0f, dtime-0.0001f);
            dtime = 0.0001f;
        }

        {	// calculate the actual "virtual" cursor position
            // noise first
            if(m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_TRACKING_NOISE.ordinal()].getBaseValue() != 0)
            {
                // OPTIMIZE: expf() called too often
			    final float base_radius = (float)Math.exp(m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal()].getBaseValue());

                x += m_Rng.randGauss() * m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_TRACKING_NOISE.ordinal()].getBaseValue() * base_radius;
                y += m_Rng.randGauss() * m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_TRACKING_NOISE.ordinal()].getBaseValue() * base_radius;
            }

            final float fac = 1.0f - PBMath.expDecay(m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_SLOW_TRACKING.ordinal()].getBaseValue(), 100.0f*dtime);
            x = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()] + (x - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()]) * fac;
            y = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()] + (y - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()]) * fac;
        }

        // draw many (or zero) dabs to the next position

        float dist_moved = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIST.ordinal()];
        float dist_todo = countDabsTo(x, y, pressure, dtime);
        Log.i(PB_DEBUG_TAG, "[PB_TRACE]=== dist_todo: " + dist_todo);

        if(dtime > 5 || m_bResetRequested)
        {
            m_bResetRequested = false;

            for(int i = 0; i < PaintBrushState.PAINT_BRUSH_STATES_COUNT.ordinal(); i++)
                m_arrStates[i] = 0;

            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()] = x;
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()] = y;
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_PRESSURE.ordinal()] = pressure;

            // not resetting, because they will get overwritten below:
            // dx, dy, dpress, dtime

            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_X.ordinal()] = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()];
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_Y.ordinal()] = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()];
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] = 1.0f; // start in a state as if the stroke was long finished

            return true;
        }

        // 0 : PB_UNKNOWN
        // 1 : PB_YES
        // -1 : PB_NO
        int painted = 0;

        float dtime_left = dtime;

        float step_dx, step_dy, step_dpressure, step_dtime;
        float step_declination, step_ascension;
        while(dist_moved + dist_todo >= 1.0)
        {	// there are dabs pending
            {	// linear interpolation (nonlinear variant was too slow, see SVN log)
                float frac;	// fraction of the remaining distance to move
                if(dist_moved > 0)
                {
                    // "move" the brush exactly to the first dab (moving less than one dab)
                    frac = (1.0f - dist_moved) / dist_todo;
                    dist_moved = 0;
                }
                else
                    // "move" the brush from one dab to the next
                    frac = 1.0f / dist_todo;

                step_dx        = frac * (x - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()]);
                step_dy        = frac * (y - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()]);
                step_dpressure = frac * (pressure - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_PRESSURE.ordinal()]);
                step_dtime     = frac * (dtime_left - 0.0f);
                // Though it looks different, time is interpolated exactly like x/y/pressure.
                step_declination = frac * (tilt_declination - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DECLINATION.ordinal()]);
                step_ascension   = frac * PBMath.smallestAngularDifference(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ASCENSION.ordinal()], tilt_ascension);
            }

            updateStatesAndSettingValues(step_dx, step_dy, step_dpressure, step_declination, step_ascension, step_dtime);
            boolean painted_now = prepareAndDrawDab();
            if(painted_now)
                painted = 1;
            else if(painted == 0)
                painted = -1;

            dtime_left -= step_dtime;
            dist_todo = countDabsTo(x, y, pressure, dtime_left);
        }

        {
            // "move" the brush to the current time (no more dab will happen)
            // Important to do this at least once every event, because brush_count_dabs_to depends on the radius
            // and the radius can depend on something that changes much faster than only every dab (eg speed)
            step_dx        = x - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()];
            step_dy        = y - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()];
            step_dpressure = pressure - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_PRESSURE.ordinal()];
            step_declination = tilt_declination - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DECLINATION.ordinal()];
            step_ascension = PBMath.smallestAngularDifference(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ASCENSION.ordinal()], tilt_ascension);
            step_dtime     = dtime_left;
            //dtime_left = 0; but that value is not used any more

            updateStatesAndSettingValues(step_dx, step_dy, step_dpressure, step_declination, step_ascension, step_dtime);
        }

        // save the fraction of a dab that is already done now
        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIST.ordinal()] = dist_moved + dist_todo;

        // stroke separation logic (for undo/redo)

        if(painted == 0)
        {
            if(m_dStrokeCurrentIdlingTime > 0 || m_dStrokeTotalPaintingTime == 0)
                // still idling
                painted = -1;
            else
                // probably still painting (we get more events than brushdabs)
                painted = 1;
        }

        if(painted == 1)
        {
            m_dStrokeTotalPaintingTime += dtime;
            m_dStrokeCurrentIdlingTime = 0;
            // force a stroke split after some time
            if(m_dStrokeTotalPaintingTime > 4 + 3*pressure)
            {
                // but only if pressure is not being released
                // FIXME: use some smoothed state for dpressure, not the output of the interpolation code
                // (which might easily wrongly give dpressure == 0)
                if(step_dpressure >= 0)
                    return true;
            }
        }
        else if(painted == -1)
        {
            m_dStrokeCurrentIdlingTime += dtime;
            if(m_dStrokeTotalPaintingTime == 0)
            {
                // not yet painted, start a new stroke if we have accumulated a lot of irrelevant motion events
                if(m_dStrokeCurrentIdlingTime > 1.0)
                    return true;
            }
            else
            {
                // Usually we have pressure==0 here
                // But some brushes can paint nothing at full pressure (eg gappy lines, or a stroke that fades out)
                // In either case this is the prefered moment to split
                if(m_dStrokeTotalPaintingTime + m_dStrokeCurrentIdlingTime > 0.9 + 5*pressure)
                    return true;
            }
        }

        return false;
    }

    // How many dabs will be drawn between the current and the next (x, y, pressure, +dt) position?
    private float countDabsTo(float x, float y, float pressure, float dtime) {
        float xx, yy;
        float res1, res2, res3;
        float dist;

        if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] == 0.0)
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] = (float)Math.exp(m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal()].getBaseValue());
        if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] < PBUtil.ACTUAL_RADIUS_MIN)
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] = PBUtil.ACTUAL_RADIUS_MIN;
        if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] > PBUtil.ACTUAL_RADIUS_MAX)
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] = PBUtil.ACTUAL_RADIUS_MAX;

        // OPTIMIZE: expf() called too often
        float base_radius = (float)Math.exp(m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal()].getBaseValue());
        if(base_radius < PBUtil.ACTUAL_RADIUS_MIN)
            base_radius = PBUtil.ACTUAL_RADIUS_MIN;
        if(base_radius > PBUtil.ACTUAL_RADIUS_MAX)
            base_radius = PBUtil.ACTUAL_RADIUS_MAX;

        xx = x - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()];
        yy = y - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()];
        // TODO: control rate with pressure (dabs per pressure) (dpressure is useless)

        if (m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_ELLIPTICAL_DAB_RATIO.ordinal()] > 1.0f)
        {
            // code duplication, see tiledsurface::draw_dab()
            float angle_rad = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_ELLIPTICAL_DAB_ANGLE.ordinal()]/360*2*PBUtil.PB_M_PI;
            float cs = (float)Math.cos(angle_rad);
            float sn = (float)Math.sin(angle_rad);
            float yyr = (yy*cs - xx*sn) * m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_ELLIPTICAL_DAB_RATIO.ordinal()];
            float xxr = yy*sn + xx*cs;
            dist = (float)Math.sqrt(yyr*yyr + xxr*xxr);
        }
        else
            dist = (float)Math.hypot(xx, yy);

        // FIXME: no need for base_value or for the range checks above IF always the interpolation
        //        function will be called before this one
        res1 = dist / m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] * m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_DABS_PER_ACTUAL_RADIUS.ordinal()].getBaseValue();
        res2 = dist / base_radius * m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_DABS_PER_BASIC_RADIUS.ordinal()].getBaseValue();
        res3 = dtime * m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_DABS_PER_SECOND.ordinal()].getBaseValue();

        return res1 + res2 + res3;
    }

    // This function runs a brush "simulation" step.
    // Usually it is called once or twice per dab.
    // In theory the precision of the "simulation" gets better when it is called more often.
    // In practice this only matters if there are some highly nonlinear mappings in critical places or extremely few events per second.
    // note: parameters are is dx/ddab, ..., dtime/ddab (dab is the number, 5.0 = 5th dab)
    private void updateStatesAndSettingValues(float step_dx, float step_dy, float step_dpressure, float step_declination, float step_ascension, float step_dtime) {
        float pressure;
        float[] inputs = new float[PaintBrushInput.PAINT_BRUSH_INPUTS_COUNT.ordinal()];

        if(step_dtime < 0.0)
        {
            Log.i(PB_DEBUG_TAG, "[PB_TRACE]Time is running backwards!");
            step_dtime = 0.001f;
        }
        else if(step_dtime == 0.0)
            // FIXME: happens about every 10th start, workaround (against division by zero)
            step_dtime = 0.001f;

        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()] += step_dx;
        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()] += step_dy;
        Log.i(PB_DEBUG_TAG, "[PB_TRACE]STATE_X: " + m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()] + ", STATE_Y: " + m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()]);

        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_PRESSURE.ordinal()] += step_dpressure;

        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DECLINATION.ordinal()]	+= step_declination;
        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ASCENSION.ordinal()]	+= step_ascension;

        float base_radius = (float)Math.exp(m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal()].getBaseValue());

        // FIXME: does happen (interpolation problem?)
        if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_PRESSURE.ordinal()] <= 0.0f)
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_PRESSURE.ordinal()] = 0.0f;
        pressure = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_PRESSURE.ordinal()];

        {	// start / end stroke (for "stroke" input only)
            if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE_STARTED.ordinal()] == 0.0f)
            {
                if(pressure > m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_STROKE_THRESHOLD.ordinal()].getBaseValue() + 0.0001f)
                {
                    // start new stroke
                    m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE_STARTED.ordinal()] = 1.0f;
                    m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] = 0.0f;
                }
            }
            else
            {
                if(pressure <= m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_STROKE_THRESHOLD.ordinal()].getBaseValue() * 0.9f + 0.0001f)
                // end stroke
                m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE_STARTED.ordinal()] = 0.0f;
            }
        }

        // now follows input handling

        float norm_dx, norm_dy, norm_dist, norm_speed;
        norm_dx = step_dx / step_dtime / base_radius;
        norm_dy = step_dy / step_dtime / base_radius;
        norm_speed = (float)Math.sqrt(PBUtil.SQR(norm_dx) + PBUtil.SQR(norm_dy));
        norm_dist = norm_speed * step_dtime;

        inputs[PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal()] = pressure * (float)Math.exp(m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_PRESSURE_GAIN_LOG.ordinal()].getBaseValue());
        inputs[PaintBrushInput.PAINT_BRUSH_INPUT_SPEED1.ordinal()] = (float)Math.log(m_arrSpeedMappingGamma[0] + m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_SPEED1_SLOW.ordinal()]) * m_arrSpeedMappingM[0] + m_arrSpeedMappingQ[0];
        inputs[PaintBrushInput.PAINT_BRUSH_INPUT_SPEED2.ordinal()] = (float)Math.log(m_arrSpeedMappingGamma[1] + m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_SPEED2_SLOW.ordinal()]) * m_arrSpeedMappingM[1] + m_arrSpeedMappingQ[1];
        inputs[PaintBrushInput.PAINT_BRUSH_INPUT_RANDOM.ordinal()] = (float)m_Rng.doubleNext();
        inputs[PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal()] = Math.min(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()], 1.0f);
        inputs[PaintBrushInput.PAINT_BRUSH_INPUT_DIRECTION.ordinal()] = (float)Math.IEEEremainder(Math.atan2(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIRECTION_DY.ordinal()], m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIRECTION_DX.ordinal()]) / (2*PBUtil.PB_M_PI)*360 + 180.0f, 180.0f);
        inputs[PaintBrushInput.PAINT_BRUSH_INPUT_TILT_DECLINATION.ordinal()] = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DECLINATION.ordinal()];
        inputs[PaintBrushInput.PAINT_BRUSH_INPUT_TILT_ASCENSION.ordinal()] = (float)Math.IEEEremainder(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ASCENSION.ordinal()] + 180.0f, 360.0f) - 180.0f;

        inputs[PaintBrushInput.PAINT_BRUSH_INPUT_CUSTOM.ordinal()] = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_CUSTOM_INPUT.ordinal()];
        Log.i(PB_DEBUG_TAG, "[PB_TRACE]press = " + inputs[PaintBrushInput.PAINT_BRUSH_INPUT_PRESSURE.ordinal()] +
                                ", speed1 = " + inputs[PaintBrushInput.PAINT_BRUSH_INPUT_SPEED1.ordinal()] +
                                ", speed2 = " + inputs[PaintBrushInput.PAINT_BRUSH_INPUT_SPEED2.ordinal()] +
                                ", stroke = " + inputs[PaintBrushInput.PAINT_BRUSH_INPUT_STROKE.ordinal()] +
                                ", custom = " + inputs[PaintBrushInput.PAINT_BRUSH_INPUT_CUSTOM.ordinal()]);

        // FIXME: this one fails!!!
        //BRTHREAD_ASSERT(inputs[PAINT_BRUSH_INPUT_SPEED1] >= 0.0 && inputs[PAINT_BRUSH_INPUT_SPEED1] < 1e8); // checking for inf

        for(int i = 0; i < PaintBrushSetting.PAINT_BRUSH_SETTINGS_COUNT.ordinal(); i++)
            m_arrSettingsValue[i] = m_pArrSettings[i].calculate(inputs);

        {
            float fac = 1.0f - PBMath.expDecay(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SLOW_TRACKING_PER_DAB.ordinal()], 1.0f);
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_X.ordinal()] += (m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_X.ordinal()] - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_X.ordinal()]) * fac;	// FIXME: should this depend on base radius?
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_Y.ordinal()] += (m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_Y.ordinal()] - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_Y.ordinal()]) * fac;
        }

        {	// slow speed
            float fac;
            fac = 1.0f - PBMath.expDecay(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SPEED1_SLOWNESS.ordinal()], step_dtime);
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_SPEED1_SLOW.ordinal()] += (norm_speed - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_SPEED1_SLOW.ordinal()]) * fac;
            fac = 1.0f - PBMath.expDecay (m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SPEED2_SLOWNESS.ordinal()], step_dtime);
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_SPEED2_SLOW.ordinal()] += (norm_speed - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_SPEED2_SLOW.ordinal()]) * fac;
        }

        {	// slow speed, but as vector this time
            // FIXME: offset_by_speed should be removed
            //   Is it broken, non-smooth, system-dependent math?!
            //   A replacement could be a directed random offset
            float time_constant = (float)Math.exp(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OFFSET_BY_SPEED_SLOWNESS.ordinal()]*0.01f) - 1.0f;
            // Workaround for a bug that happens mainly on Windows, causing individual dabs to be placed far far away
            // Using the speed with zero filtering is just asking for trouble anyway
            if(time_constant < 0.002f)
                time_constant = 0.002f;
            float fac = 1.0f - PBMath.expDecay(time_constant, step_dtime);
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_DX_SLOW.ordinal()] += (norm_dx - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_DX_SLOW.ordinal()]) * fac;
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_DY_SLOW.ordinal()] += (norm_dy - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_DY_SLOW.ordinal()]) * fac;
        }

        {	// orientation (similar lowpass filter as above, but use dabtime instead of wallclock time)
            float dx = step_dx / base_radius;
            float dy = step_dy / base_radius;
            float step_in_dabtime = (float)Math.hypot(dx, dy); // FIXME: are we recalculating something here that we already have?
            float fac = 1.0f - PBMath.expDecay((float)Math.exp(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_DIRECTION_FILTER.ordinal()]*0.5f) - 1.0f, step_in_dabtime);

            float dx_old = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIRECTION_DX.ordinal()];
            float dy_old = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIRECTION_DY.ordinal()];
            // use the opposite speed vector if it is closer (we don't care about 180 degree turns)
            if(PBUtil.SQR(dx_old-dx) + PBUtil.SQR(dy_old-dy) > PBUtil.SQR(dx_old-(-dx)) + PBUtil.SQR(dy_old-(-dy)))
            {
                dx = -dx;
                dy = -dy;
            }
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIRECTION_DX.ordinal()] += (dx - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIRECTION_DX.ordinal()]) * fac;
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIRECTION_DY.ordinal()] += (dy - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_DIRECTION_DY.ordinal()]) * fac;
        }

        {	// custom input
            float fac = 1.0f - PBMath.expDecay(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_CUSTOM_INPUT_SLOWNESS.ordinal()], 0.1f);
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_CUSTOM_INPUT.ordinal()] += (m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_CUSTOM_INPUT.ordinal()] - m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_CUSTOM_INPUT.ordinal()]) * fac;
        }

        {	// stroke length
            float frequency = (float)Math.exp(-m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_STROKE_DURATION_LOGARITHMIC.ordinal()]);
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] += norm_dist * frequency;
            // can happen, probably caused by rounding
            if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] < 0)
                m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] = 0;

            float wrap = 1.0f + m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_STROKE_HOLDTIME.ordinal()];
            if (m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] > wrap)
            {
                if (wrap > 9.9 + 1.0)
                    // "inifinity", just hold stroke somewhere >= 1.0
                    m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] = 1.0f;
                else
                {
                    m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] = (float)Math.IEEEremainder(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()], wrap);
                    // just in case
                    if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] < 0)
                        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_STROKE.ordinal()] = 0;
                }
            }
        }

        // calculate final radius
        float radius_log = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal()];
        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] = (float)Math.exp(radius_log);
        if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] < PBUtil.ACTUAL_RADIUS_MIN)
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] = PBUtil.ACTUAL_RADIUS_MIN;
        if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] > PBUtil.ACTUAL_RADIUS_MAX)
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] = PBUtil.ACTUAL_RADIUS_MAX;

        // aspect ratio (needs to be calculated here because it can affect the dab spacing)
        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_ELLIPTICAL_DAB_RATIO.ordinal()] = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_ELLIPTICAL_DAB_RATIO.ordinal()];
        m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_ELLIPTICAL_DAB_ANGLE.ordinal()] = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_ELLIPTICAL_DAB_ANGLE.ordinal()];
    }

    // Called only from stroke_to()
    // Calculate everything needed to draw the dab, then let the surface do the actual drawing
    // This is only gets called right after update_states_and_setting_values()
    // Returns TRUE if the surface was modified
    private boolean prepareAndDrawDab() {
        float x, y, opaque;
        float radius;

        // ensure we don't get a positive result with two negative opaque values
        if(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE.ordinal()] < 0)
            m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE.ordinal()] = 0;
        opaque = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE.ordinal()] * m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE_MULTIPLY.ordinal()];
        opaque = PBUtil.MINMAX(0.0f, opaque, 1.0f);

        if(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE_LINEARIZE.ordinal()] != 0)
        {
            // OPTIMIZE: no need to recalculate this for each dab
            float alpha, beta, alpha_dab, beta_dab;
            float dabs_per_pixel;
            // dabs_per_pixel is just estimated roughly, I didn't think hard
            // about the case when the radius changes during the stroke
            dabs_per_pixel = (m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_DABS_PER_ACTUAL_RADIUS.ordinal()].getBaseValue() +
                    m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_DABS_PER_BASIC_RADIUS.ordinal()].getBaseValue()) * 2.0f;

            // the correction is probably not wanted if the dabs don't overlap
            if(dabs_per_pixel < 1.0f)
                dabs_per_pixel = 1.0f;

            // interpret the user-setting smoothly
            dabs_per_pixel = 1.0f + m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_OPAQUE_LINEARIZE.ordinal()].getBaseValue() * (dabs_per_pixel-1.0f);

            // beta = beta_dab^dabs_per_pixel
            // <==> beta_dab = beta^(1/dabs_per_pixel)
            alpha		= opaque;
            beta		= 1.0f - alpha;
            beta_dab	= (float)Math.pow(beta, 1.0/dabs_per_pixel);
            alpha_dab	= 1.0f - beta_dab;
            opaque		= alpha_dab;
        }

        x = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_X.ordinal()];
        y = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_Y.ordinal()];

        float base_radius = (float)Math.exp(m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal()].getBaseValue());

        if(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OFFSET_BY_SPEED.ordinal()] != 0)
        {
            x += m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_DX_SLOW.ordinal()] * m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OFFSET_BY_SPEED.ordinal()] * 0.1f * base_radius;
            y += m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_NORM_DY_SLOW.ordinal()] * m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OFFSET_BY_SPEED.ordinal()] * 0.1f * base_radius;
        }

        if(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OFFSET_BY_RANDOM.ordinal()] != 0)
        {
            float amp = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_OFFSET_BY_RANDOM.ordinal()];
            if(amp < 0.0f)
                amp = 0.0f;
            x += m_Rng.randGauss() * amp * base_radius;
            y += m_Rng.randGauss() * amp * base_radius;
        }

        radius = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()];
        if(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_BY_RANDOM.ordinal()] != 0)
        {
            float radius_log, alpha_correction;
            // go back to logarithmic radius to add the noise
            radius_log = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_LOGARITHMIC.ordinal()];
            radius_log += m_Rng.randGauss() * m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_RADIUS_BY_RANDOM.ordinal()];
            radius = (float)Math.exp(radius_log);

            radius = PBUtil.MINMAX(PBUtil.ACTUAL_RADIUS_MIN, radius, PBUtil.ACTUAL_RADIUS_MAX);
            alpha_correction = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_RADIUS.ordinal()] / radius;
            alpha_correction = PBUtil.SQR(alpha_correction);
            if(alpha_correction <= 1.0f)
                opaque *= alpha_correction;
        }

        // update smudge color
        if(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SMUDGE_LENGTH.ordinal()] < 1.0f &&
                // optimization, since normal brushes have smudge_length == 0.5 without actually smudging
                (m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SMUDGE.ordinal()] != 0.0f || !m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_SMUDGE.ordinal()].isConstant()))
        {
            float fac = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SMUDGE_LENGTH.ordinal()];
            if(fac < 0.01f)
                fac = 0.01f;
            int px, py;
            px = PBUtil.ROUND(x);
            py = PBUtil.ROUND(y);

            // Calling get_color() is almost as expensive as rendering a dab
            // Because of this we use the previous value if it is not expected to hurt quality too much
            // We call it at most every second dab
            float r, g, b, a;
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_RECENTNESS.ordinal()] *= fac;
            if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_RECENTNESS.ordinal()] < 0.5*fac)
            {
                if(m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_RECENTNESS.ordinal()] == 0.0)
                    // first initialization of smudge color
                    fac = 0.0f;

                m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_RECENTNESS.ordinal()] = 1.0f;

                float smudge_radius = radius * (float)Math.exp(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SMUDGE_RADIUS_LOG.ordinal()]);
                smudge_radius = PBUtil.MINMAX(PBUtil.ACTUAL_RADIUS_MIN, smudge_radius, PBUtil.ACTUAL_RADIUS_MAX);

/*
                assert(0);
                //mypaint_surface_get_color(surface, px, py, smudge_radius, &r, &g, &b, &a);	// To do: 함수 추가해야됨(일단 테스트할 때 들어올 일 없어서 막아둠)
                //m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_R.ordinal()] = r;
                //m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_G.ordinal()] = g;
                //m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_B.ordinal()] = b;
                //m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_A.ordinal()] = a;
*/
                r = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_R.ordinal()];
                g = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_G.ordinal()];
                b = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_B.ordinal()];
                a = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_A.ordinal()];
            }
            else
            {
                r = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_R.ordinal()];
                g = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_G.ordinal()];
                b = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_B.ordinal()];
                a = m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_LAST_GETCOLOR_A.ordinal()];
            }

            // updated the smudge color (stored with premultiplied alpha)
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_A.ordinal()] = fac*m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_A.ordinal()] + (1-fac)*a;
            // fix rounding errors
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_A.ordinal()] = PBUtil.MINMAX(0.0f, m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_A.ordinal()], 1.0f);

            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_RA.ordinal()] = fac*m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_RA.ordinal()] + (1-fac)*r*a;
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_GA.ordinal()] = fac*m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_GA.ordinal()] + (1-fac)*g*a;
            m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_BA.ordinal()] = fac*m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_BA.ordinal()] + (1-fac)*b*a;
        }

        // color part

        PBColor color = new PBColor();
        color.c1 = m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_COLOR_H.ordinal()].getBaseValue();
        color.c2 = m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_COLOR_S.ordinal()].getBaseValue();
        color.c3 = m_pArrSettings[PaintBrushSetting.PAINT_BRUSH_SETTING_COLOR_V.ordinal()].getBaseValue();
        float eraser_target_alpha = 1.0f;

        if(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SMUDGE.ordinal()] > 0.0)
        {
            // HSV를 RGB로 바꿔서 계산하고 다시 HSV로 바꾸고 있음
            // mix (in RGB) the smudge color with the brush color

            PBColorConverter.HsvToRgb(color);

            float fac = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SMUDGE.ordinal()];
            if(fac > 1.0f)
                fac = 1.0f;

            // If the smudge color somewhat transparent, then the resulting dab will do erasing towards that transparency level
            eraser_target_alpha = (1-fac)*1.0f + fac*m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_A.ordinal()];
            // fix rounding errors (they really seem to happen in the previous line)
            eraser_target_alpha = PBUtil.MINMAX(0.0f, eraser_target_alpha, 1.0f);
            if(eraser_target_alpha > 0)
            {
                color.c1 = (fac*m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_RA.ordinal()] + (1-fac)*color.c1) / eraser_target_alpha;
                color.c2 = (fac*m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_GA.ordinal()] + (1-fac)*color.c2) / eraser_target_alpha;
                color.c3 = (fac*m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_SMUDGE_BA.ordinal()] + (1-fac)*color.c3) / eraser_target_alpha;
            }
            else
            {
                // we are only erasing; the color does not matter
                color.c1 = 1.0f;
                color.c2 = 0.0f;
                color.c3 = 0.0f;
            }

            PBColorConverter.RgbToHsv(color);
        }

        // eraser
        if(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_ERASER.ordinal()] != 0)
            eraser_target_alpha *= (1.0-m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_ERASER.ordinal()]);

        // HSV color change
        color.c1 += m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_H.ordinal()];
        color.c2 += m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_HSV_S.ordinal()];
        color.c3 += m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_V.ordinal()];

        // HSL color change
        if(m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_L.ordinal()] != 0 || m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_HSL_S.ordinal()] != 0)
        {
            // (calculating way too much here, can be optimized if neccessary)
            // this function will CLAMP the inputs
            PBColorConverter.HsvToRgb(color);
            PBColorConverter.RgbToHsl(color);
            color.c3 += m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_L.ordinal()];
            color.c2 += m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_CHANGE_COLOR_HSL_S.ordinal()];
            PBColorConverter.HslToRgb(color);
            PBColorConverter.RgbToHsv(color);
        }

        float hardness = PBUtil.MINMAX(0.0f, m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_HARDNESS.ordinal()], 1.0f);

        // anti-aliasing attempt (works surprisingly well for ink brushes)
        float current_fadeout_in_pixels = radius * (1.0f - hardness);
        float min_fadeout_in_pixels = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_ANTI_ALIASING.ordinal()];
        if(current_fadeout_in_pixels < min_fadeout_in_pixels)
        {
            // need to soften the brush (decrease hardness), but keep optical radius
            // so we tune both radius and hardness, to get the desired fadeout_in_pixels
            float current_optical_radius = radius - (1.0f-hardness)*radius/2.0f;

            // Equation 1: (new fadeout must be equal to min_fadeout)
            //   min_fadeout_in_pixels = radius_new*(1.0 - hardness_new)
            // Equation 2: (optical radius must remain unchanged)
            //   current_optical_radius = radius_new - (1.0-hardness_new)*radius_new/2.0
            //
            // Solved Equation 1 for hardness_new, using Equation 2: (thanks to mathomatic)
            float hardness_new = ((current_optical_radius - (min_fadeout_in_pixels/2.0f))/(current_optical_radius + (min_fadeout_in_pixels/2.0f)));
            // Using Equation 1:
            float radius_new = (min_fadeout_in_pixels/(1.0f - hardness_new));

            hardness = hardness_new;
            radius = radius_new;
        }

        // snap to pixel
        float snapToPixel = m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_SNAP_TO_PIXEL.ordinal()];
        if(snapToPixel > 0.0)
        {
            // linear interpolation between non-snapped and snapped
            float snapped_x = (float)Math.floor(x) + 0.5f;
            float snapped_y = (float)Math.floor(y) + 0.5f;
            x = x + (snapped_x - x) * snapToPixel;
            y = y + (snapped_y - y) * snapToPixel;

            float snapped_radius = PBUtil.roundF(radius * 2.0f) / 2.0f;
            if(snapped_radius < 0.5f)
                snapped_radius = 0.5f;

            if(snapToPixel > 0.9999f)
                snapped_radius -= 0.0001f; // this fixes precision issues where neighboor pixels could be wrongly painted

            radius = radius + (snapped_radius - radius) * snapToPixel;
        }

        PBColorConverter.HsvToRgb(color);

        return drawDab(x, y, radius, color.c1, color.c2, color.c3, opaque, hardness, eraser_target_alpha,
                m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_ELLIPTICAL_DAB_RATIO.ordinal()], m_arrStates[PaintBrushState.PAINT_BRUSH_STATE_ACTUAL_ELLIPTICAL_DAB_ANGLE.ordinal()],
                m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_LOCK_ALPHA.ordinal()], m_arrSettingsValue[PaintBrushSetting.PAINT_BRUSH_SETTING_COLORIZE.ordinal()]);
    }

    private boolean drawDab(float x, float y, float radius, float color_r, float color_g, float color_b, float opaque, float hardness, float color_a,
                            float aspect_ratio, float angle, float lock_alpha, float colorize) {
        Log.i(PB_DEBUG_TAG, "[PB_TRACE][drawDab] x: " + x + ", y: " + y + ", radius: " + radius + ", color_r: " + color_r + ", color_g: " + color_g + ", color_b: " + color_b + ", opaque: " + opaque +
                                ", hardness: " + hardness + ", color_a: " + color_a + ", aspect_ratio: " + aspect_ratio + ", angle: " + angle + ", lock_alpha: " + lock_alpha + ", colorize: " + colorize);

        PBDrawDabData sDDData = new PBDrawDabData();

        sDDData.x = x;
        sDDData.y = y;
        sDDData.radius = radius;
        if(sDDData.radius > PaintBrushDefine.PAINT_BRUSH_MAX_RADIUS)
            sDDData.radius = PaintBrushDefine.PAINT_BRUSH_MAX_RADIUS;
        sDDData.opaque = PBUtil.MINMAX(0.0f, opaque, 1.0f);
        sDDData.hardness = PBUtil.MINMAX(0.0f, hardness, 1.0f);
        sDDData.aspect_ratio = aspect_ratio;
        sDDData.angle = angle;
        sDDData.lock_alpha = PBUtil.MINMAX(0.0f, lock_alpha, 1.0f);
        sDDData.colorize = PBUtil.MINMAX(0.0f, colorize, 1.0f);

        if(sDDData.radius < 0.1f)
            return false;	// don't bother with dabs smaller than 0.1 pixel
        if(sDDData.hardness == 0.0f)
            return false;	// infinitely small center point, fully transparent outside
        if(sDDData.opaque == 0.0f)
            return false;

        color_r = PBUtil.MINMAX(0.0f, color_r, 1.0f);
        color_g = PBUtil.MINMAX(0.0f, color_g, 1.0f);
        color_b = PBUtil.MINMAX(0.0f, color_b, 1.0f);
        color_a = PBUtil.MINMAX(0.0f, color_a, 1.0f);

        sDDData.color_r = (byte)(color_r * 255);
        sDDData.color_g = (byte)(color_g * 255);
        sDDData.color_b = (byte)(color_b * 255);
        sDDData.color_a = color_a;

        // blending mode preparation
        sDDData.normal = 1.0f;
        sDDData.normal *= 1.0f - sDDData.lock_alpha;
        sDDData.normal *= 1.0f - sDDData.colorize;

        if(sDDData.aspect_ratio < 1.0f)
            sDDData.aspect_ratio = 1.0f;

        // Determine the tiles influenced by operation, and queue it for processing for each tile
        float r_fringe = radius + 1.0f; // +1.0 should not be required, only to be sure
        int tx1 = (int)Math.floor(Math.floor(x - r_fringe) / PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE);
        int tx2 = (int)Math.floor(Math.floor(x + r_fringe) / PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE);
        int ty1 = (int)Math.floor(Math.floor(y - r_fringe) / PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE);
        int ty2 = (int)Math.floor(Math.floor(y + r_fringe) / PaintBrushDefine.PAINT_BRUSH_MAX_TILE_SIZE);

        for(int ty = ty1; ty <= ty2; ty++)
            for(int tx = tx1; tx <= tx2; tx++)
                PaintBrushDraw.drawDabLine(sDDData, m_pBitmap, m_DabBitmap, tx, ty);

        return true;
    }
}
