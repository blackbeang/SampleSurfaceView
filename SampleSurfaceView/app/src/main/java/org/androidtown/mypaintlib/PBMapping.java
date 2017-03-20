package org.androidtown.mypaintlib;

/**
 * Created by BlackBean on 2017-03-13.
 */

public class PBMapping {
    private float m_fBaseValue;	// FIXME: accessed directly from mypaint-brush.c // PrPaintBrush class

    private int m_nInputs;
    private ControlPoints[] m_pPointsList;	// one for each input
    private int m_nInputUsed;	// optimization


    public PBMapping(int nInputs) {
        m_fBaseValue = 0;
        m_nInputUsed = 0;

        m_pPointsList = new ControlPoints[nInputs];
        if(m_pPointsList != null) {
            for(int i = 0; i < nInputs; i++)
                m_pPointsList[i] = new ControlPoints();
            m_nInputs = nInputs;
        }
        else
            m_nInputs = 0;
    }

    public float getBaseValue() {
        return m_fBaseValue;
    }

    public void setN(int input, int n) {
        if(PBUtil.CHECK_CPOINTS_ID(input, m_nInputs) == false || PBUtil.CHECK_CPOINTS_INDEX(n) == false)
            return;
        assert(n != 1);	// cannot build a linear mapping with only one point

        ControlPoints pCtrPt = m_pPointsList[input];

        if(n != 0 && m_pPointsList[input].n == 0)
            m_nInputUsed++;
        if(n == 0 && m_pPointsList[input].n != 0)
            m_nInputUsed--;

        assert(m_nInputUsed >= 0);
        assert(m_nInputUsed <= m_nInputs);

        m_pPointsList[input].n = n;
    }

    public void setPoint(int input, int index, float x, float y) {
        if(PBUtil.CHECK_CPOINTS_ID(input, m_nInputs) == false || PBUtil.CHECK_CPOINTS_INDEX(index) == false)
            return;

        assert(index < m_pPointsList[input].n);

        if(index > 0)
            assert(x >= m_pPointsList[input].arrXValues[index-1]);

        m_pPointsList[input].arrXValues[index] = x;
        m_pPointsList[input].arrYValues[index] = y;
    }

    public void setBaseValue(float value) {
        m_fBaseValue = value;
    }

    public float calculate(float[] data) {
        float result = m_fBaseValue;
        if(m_nInputUsed == 0)
            return result;

        float x, y, x0, y0, x1, y1;;
        int i, j;

        for(j = 0; j < m_nInputs; j++)
        {
            if(m_pPointsList[j].n != 0)
            {
                x = data[j];

                // find the segment with the slope that we need to use
                x0 = m_pPointsList[j].arrXValues[0];
                y0 = m_pPointsList[j].arrYValues[0];
                x1 = m_pPointsList[j].arrXValues[1];
                y1 = m_pPointsList[j].arrYValues[1];

                for(i = 2; i < m_pPointsList[j].n && x > x1; i++)
                {
                    x0 = x1;
                    y0 = y1;
                    x1 = m_pPointsList[j].arrXValues[i];
                    y1 = m_pPointsList[j].arrYValues[i];
                }

                if(x0 == x1)
                    y = y0;
                else
                    // linear interpolation
                    y = (y1*(x - x0) + y0*(x1 - x)) / (x1 - x0);

                result += y;
            }
        }

        return result;
    }

    public boolean isConstant() {
        return (m_nInputUsed == 0);
    }
}


class ControlPoints {
    // a set of control points (stepwise linear)
    public float[] arrXValues;
    public float[] arrYValues;
    public int n;

    public ControlPoints() {
        arrXValues = new float[PaintBrushDefine.PAINT_BRUSH_CPOINTS_COUNT];
        arrYValues = new float[PaintBrushDefine.PAINT_BRUSH_CPOINTS_COUNT];
        n = 0;
    }
}
