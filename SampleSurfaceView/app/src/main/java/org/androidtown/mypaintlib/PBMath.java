package org.androidtown.mypaintlib;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by BlackBean on 2017-03-13.
 */

public class PBMath {
    // Returns the smallest angular difference (counterclockwise or clockwise) a to b, in degrees
    // Clockwise is positive
    public static float smallestAngularDifference(float a, float b) {
        a = (float)Math.IEEEremainder(a, 360.0f);
        b = (float)Math.IEEEremainder(b, 360.0f);

        float d_cw, d_ccw;

        if(a > b)
        {
            d_cw = a - b;
            d_ccw = b + 360.0f - a;
        }
        else
        {
            d_cw = a + 360.0f - b;
            d_ccw = b - a;
        }
        return (d_cw < d_ccw)? -d_cw:d_ccw;
    }

    // returns the fraction still left after t seconds
    public static float expDecay(float T_const, float t) {
        // the argument might not make mathematical sense (whatever)
        if(T_const <= 0.001)
            return 0.0f;

        final float arg = -t / T_const;
        return (float)Math.exp(arg);
    }

    public static float calculateRSample(float x, float y, float aspect_ratio, float sn, float cs) {
	    final float yyr = (y*cs-x*sn) * aspect_ratio;
        final float xxr = y*sn + x*cs;
        final float r = (yyr*yyr + xxr*xxr);
        return r;
    }

    public static float calculateRr(int xp, int yp, float x, float y, float aspect_ratio, float sn, float cs, float one_over_radius2) {
        // code duplication, see brush::count_dabs_to()
        final float yy = (yp + 0.5f - y);
        final float xx = (xp + 0.5f - x);
        final float yyr = (yy*cs - xx*sn) * aspect_ratio;
        final float xxr = yy*sn + xx*cs;
        final float rr = (yyr*yyr + xxr*xxr) * one_over_radius2;
        // rr is in range 0.0..1.0*sqrt(2)
        return rr;
    }

    public static float calculateOpa(float rr, float hardness, float segment1_offset, float segment1_slope, float segment2_offset, float segment2_slope) {
        final float fac = (rr <= hardness)? segment1_slope:segment2_slope;
        float opa = (rr <= hardness)? segment1_offset:segment2_offset;
        opa += rr*fac;

        if(rr > 1.0f)
            opa = 0.0f;

/* HEAVY_DEBUG
        assert(PBUtil.isFinite(opa));
        assert(opa >= 0.0f && opa <= 1.0f);
*/
        return opa;
    }

    public static float signPointInLine(float px, float py, float vx, float vy) {
        return (px - vx) * (-vy) - (vx) * (py - vy);
    }

    public static void closestPointToLine(float lx, float ly, float px, float py, PointF point) {
        final float l2 = lx*lx + ly*ly;
        final float ltp_dot = px*lx + py*ly;
        final float t = ltp_dot / l2;
        point.x = lx * t;
        point.y = ly * t;
    }

    // This works by taking the visibility at the nearest point and dividing by 1.0 + delta
    // - nearest point: point where the dab has more influence
    // - farthest point: point at a fixed distance away from the nearest point
    // - delta: how much occluded is the farthest point relative to the nearest point
    public static float calculateRrAntialiased(int xp, int yp, float x, float y, float aspect_ratio, float sn, float cs, float one_over_radius2, float r_aa_start) {
        // calculate pixel position and borders in a way
        // that the dab's center is always at zero
        float pixel_right = x - (float)xp;
        float pixel_bottom = y - (float)yp;
        float pixel_center_x = pixel_right - 0.5f;
        float pixel_center_y = pixel_bottom - 0.5f;
        float pixel_left = pixel_right - 1.0f;
        float pixel_top = pixel_bottom - 1.0f;

        PointF nearest = new PointF();	// nearest to origin, but still inside pixel
        float farthest_x, farthest_y; // farthest from origin, but still inside pixel
        float r_near, r_far, rr_near, rr_far;
        // Dab's center is inside pixel?
        if(pixel_left < 0 && pixel_right > 0 && pixel_top < 0 && pixel_bottom > 0)
        {
            nearest.x = 0.0f;
            nearest.y = 0.0f;
            r_near = rr_near = 0;
        }
        else
        {
            closestPointToLine(cs, sn, pixel_center_x, pixel_center_y, nearest);

            nearest.x = PBUtil.MINMAX(pixel_left, nearest.x, pixel_right);
            nearest.y = PBUtil.MINMAX(pixel_top, nearest.y, pixel_bottom);

            // XXX: precision of "nearest" values could be improved
            // by intersecting the line that goes from nearest_x/Y to 0
            // with the pixel's borders here, however the improvements
            // would probably not justify the perdormance cost.
            r_near = calculateRSample(nearest.x, nearest.y, aspect_ratio, sn, cs);
            rr_near = r_near * one_over_radius2;
        }

        // out of dab's reach?
        if(rr_near > 1.0f)
            return rr_near;

        // check on which side of the dab's line is the pixel center
        float center_sign = signPointInLine(pixel_center_x, pixel_center_y, cs, -sn);

        // radius of a circle with area=1
        //   A = pi * r * r
        //   r = sqrt(1/pi)
	    final float rad_area_1 = (float)Math.sqrt(1.0f / PBUtil.PB_M_PI);

        if(center_sign < 0)	// center is below dab
        {
            farthest_x = nearest.x - sn*rad_area_1;
            farthest_y = nearest.y + cs*rad_area_1;
        }
        else	// above dab
        {
            farthest_x = nearest.x + sn*rad_area_1;
            farthest_y = nearest.y - cs*rad_area_1;
        }

        r_far = calculateRSample(farthest_x, farthest_y, aspect_ratio, sn, cs);
        rr_far = r_far * one_over_radius2;

        // check if we can skip heavier AA
        if(r_far < r_aa_start)
            return (rr_far+rr_near) * 0.5f;

        // calculate AA approximate
        float visibilityNear = 1.0f - rr_near;
        float delta = rr_far - rr_near;
        float delta2 = 1.0f + delta;
        visibilityNear /= delta2;

        return 1.0f - visibilityNear;
    }
}
