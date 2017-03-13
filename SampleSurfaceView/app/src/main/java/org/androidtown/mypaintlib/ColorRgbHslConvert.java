package org.androidtown.mypaintlib;

/**
 * Created by BlackBean on 2017-03-13.
 */

public class ColorRgbHslConvert {
    public float color_r_h;
    public float color_g_s;
    public float color_b_l;

    public ColorRgbHslConvert(float r_h, float g_s, float b_l) {
        color_r_h = r_h;
        color_g_s = g_s;
        color_b_l = b_l;
    }

    public void RgbToHsl() {
        float max, min, delta;
        float h, s, l;
        float r, g, b;

        r = PBUtil.MINMAX(0.0f, color_r_h, 1.0f);
        g = PBUtil.MINMAX(0.0f, color_g_s, 1.0f);
        b = PBUtil.MINMAX(0.0f, color_b_l, 1.0f);

        max = PBUtil.MAX3(r, g, b);
        min = PBUtil.MIN3(r, g, b);

        l = (max + min) / 2.0f;

        if(max == min)
        {
            s = 0.0f;
            h = 0.0f;
        }
        else
        {
            if(l <= 0.5f)
                s = (max - min) / (max + min);
            else
                s = (max - min) / (2.0f - max - min);

            delta = max - min;
            if(delta == 0.0f)
                delta = 1.0f;

            if(r == max)
                h = (g - b) / delta;
            else if(g == max)
                h = 2.0f + (b - r) / delta;
            else if(b == max)
                h = 4.0f + (r - g) / delta;
            else
                h = 0.0f;

            h /= 6.0f;
            if(h < 0.0f)
                h += 1.0f;
        }

        color_r_h = h;
        color_g_s= s;
        color_b_l = l;
    }

    public void HslToRgb() {
        float h, s, l;
        float r, g, b;

        h = color_r_h - (float)Math.floor(color_r_h);
        s = PBUtil.MINMAX(0.0f, color_g_s, 1.0f);
        l = PBUtil.MINMAX(0.0f, color_b_l, 1.0f);

        if(s == 0)
        {
            // achromatic case
            r = l;
            g = l;
            b = l;
        }
        else
        {
            float m1, m2;

            if(l <= 0.5f)
                m2 = l * (1.0f + s);
            else
                m2 = l + s - l * s;

            m1 = 2.0f * l - m2;

            r = hslValue(m1, m2, h * 6.0f + 2.0f);
            g = hslValue(m1, m2, h * 6.0f);
            b = hslValue(m1, m2, h * 6.0f - 2.0f);
        }

        color_r_h = r;
        color_g_s = g;
        color_b_l = b;
    }

    private float hslValue(float n1, float n2, float hue)
    {
        if(hue > 6.0f)
            hue -= 6.0f;
        else if(hue < 0.0f)
            hue += 6.0f;

        float val;

        if(hue < 1.0f)
            val = n1 + (n2 - n1) * hue;
        else if(hue < 3.0f)
            val = n2;
        else if(hue < 4.0f)
            val = n1 + (n2 - n1) * (4.0f - hue);
        else
            val = n1;

        return val;
    }
}
