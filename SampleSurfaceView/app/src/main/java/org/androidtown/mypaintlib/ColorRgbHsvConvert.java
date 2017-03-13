package org.androidtown.mypaintlib;

/**
 * Created by BlackBean on 2017-03-13.
 */

public class ColorRgbHsvConvert {
    public float color_r_h;
    public float color_g_s;
    public float color_b_v;

    public ColorRgbHsvConvert(float r_h, float g_s, float b_v) {
        color_r_h = r_h;
        color_g_s = g_s;
        color_b_v = b_v;
    }

    public void RgbToHsv() {
        float max, min, delta;
        float h, s, v;
        float r, g, b;

        r = PBUtil.MINMAX(0.0f, color_r_h, 1.0f);
        g = PBUtil.MINMAX(0.0f, color_g_s, 1.0f);
        b = PBUtil.MINMAX(0.0f, color_b_v, 1.0f);

        max = PBUtil.MAX3(r, g, b);
        min = PBUtil.MIN3(r, g, b);

        v = max;
        delta = max - min;

        if(delta > 0.0001f)
        {
            s = delta / max;

            if(r == max)
            {
                h = (g - b) / delta;
                if (h < 0.0f)
                    h += 6.0f;
            }
            else if(g == max)
                h = 2.0f + (b - r) / delta;
            else if(b == max)
                h = 4.0f + (r - g) / delta;
            else
                h = 0.0f;

            h /= 6.0f;
        }
        else
        {
            s = 0.0f;
            h = 0.0f;
        }

        color_r_h = h;
        color_g_s = s;
        color_b_v = v;
    }

    public void HsvToRgb() {
        int i;
        float f, w, q, t;
        float h, s, v;
        float r, g, b;
        r = g = b = 0.0f;

        h = color_r_h - (float)Math.floor(color_r_h);
        s = PBUtil.MINMAX(0.0f, color_g_s, 1.0f);
        v = PBUtil.MINMAX(0.0f, color_b_v, 1.0f);

        float hue;

        if(s == 0.0f)
        {
            r = v;
            g = v;
            b = v;
        }
        else
        {
            hue = h;
            if(hue == 1.0f)
                hue = 0.0f;
            hue *= 6.0f;

            i = (int)hue;
            f = hue - i;
            w = v * (1.0f - s);
            q = v * (1.0f - (s * f));
            t = v * (1.0f - (s * (1.0f - f)));

            switch(i)
            {
                case 0:
                    r = v;
                    g = t;
                    b = w;
                    break;
                case 1:
                    r = q;
                    g = v;
                    b = w;
                    break;
                case 2:
                    r = w;
                    g = v;
                    b = t;
                    break;
                case 3:
                    r = w;
                    g = q;
                    b = v;
                    break;
                case 4:
                    r = t;
                    g = w;
                    b = v;
                    break;
                case 5:
                    r = v;
                    g = w;
                    b = q;
                    break;
            }
        }

        color_r_h = r;
        color_g_s = g;
        color_b_v = b;
    }
}
