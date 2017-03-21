package org.androidtown.mypaintlib;

/**
 * Created by ejchoi on 2017-03-20.
 */

public class PBColorConverter {
    public static void RgbToHsv(PBColor color) {
        float max, min, delta;
        float h, s, v;
        float r, g, b;

        r = PBUtil.MINMAX(0.0f, color.c1, 1.0f);
        g = PBUtil.MINMAX(0.0f, color.c2, 1.0f);
        b = PBUtil.MINMAX(0.0f, color.c3, 1.0f);

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

        color.c1 = h;
        color.c2 = s;
        color.c3 = v;
    }

    public static void HsvToRgb(PBColor color) {
        int i;
        float f, w, q, t;
        float h, s, v;
        float r, g, b;
        r = g = b = 0.0f;

        h = color.c1 - (float)Math.floor(color.c1);
        s = PBUtil.MINMAX(0.0f, color.c2, 1.0f);
        v = PBUtil.MINMAX(0.0f, color.c3, 1.0f);

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

        color.c1 = r;
        color.c2 = g;
        color.c3 = b;
    }

    public static void RgbToHsl(PBColor color) {
        float max, min, delta;
        float h, s, l;
        float r, g, b;

        r = PBUtil.MINMAX(0.0f, color.c1, 1.0f);
        g = PBUtil.MINMAX(0.0f, color.c2, 1.0f);
        b = PBUtil.MINMAX(0.0f, color.c3, 1.0f);

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

        color.c1 = h;
        color.c2 = s;
        color.c3 = l;
    }

    public static void HslToRgb(PBColor color) {
        float h, s, l;
        float r, g, b;

        h = color.c1 - (float)Math.floor(color.c1);
        s = PBUtil.MINMAX(0.0f, color.c2, 1.0f);
        l = PBUtil.MINMAX(0.0f, color.c3, 1.0f);

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

        color.c1 = r;
        color.c2 = g;
        color.c3 = b;
    }

    public static float hslValue(float n1, float n2, float hue)
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


class PBColor {
    public float c1;
    public float c2;
    public float c3;
}
