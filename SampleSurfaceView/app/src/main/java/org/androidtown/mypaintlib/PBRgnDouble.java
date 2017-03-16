package org.androidtown.mypaintlib;

/**
 * Created by BlackBean on 2017-03-13.
 */

// Random Number Generator
public class PBRgnDouble {
    /*
    // original settings
    public static final int RNG_QUALITY = 1009;	// recommended quality level for high-res use
    public static final int RNG_TT = 70;			// guaranteed separation between streams
    public static final int RNG_KK = 100;			// the long lag
    public static final int RNG_LL = 37;			// the short lag
    */
    // low quality settings, seems to work for MyPaint
    // (Disclaimer: I don't understand what those numbers do, I just reduced them. --maxy)
    public static final int RNG_QUALITY = 19;
    public static final int RNG_TT = 7;
    public static final int RNG_KK = 10;
    public static final int RNG_LL = 7;


    private RandomBuffer dRanfArrDummy;
    private RandomBuffer dRanfArrStarted;

    private RandomBuffer m_arrRanU;     // the generator state
    private RandomBuffer m_arrRanfBuf;
    private RandomBuffer m_pRanfArr;    // the next random fraction, or -1
    private int m_nRndfIndex;


    public PBRgnDouble(long nSeed) {
        m_arrRanU = new RandomBuffer(RNG_KK);
        m_arrRanfBuf = new RandomBuffer(RNG_QUALITY);

        dRanfArrDummy = new RandomBuffer(1, -1);
        dRanfArrStarted = new RandomBuffer(1, -1);

        m_pRanfArr = dRanfArrDummy;
        m_nRndfIndex = 0;

        setSeed(nSeed);
    }

    private void setSeed(long nSeed) {
        RandomBuffer u = new RandomBuffer(RNG_KK+RNG_KK-1);

        int t, s, j;	// register int
        double ulp = (1.0 / (1L<<30)) / (1L<<22);	// 2 to the -52
        double ss = 2.0 * ulp * ((nSeed&0x3fffffff)+2);

        for(j = 0; j < RNG_KK; j++) {
            u.buf_nums[j] = ss;	// bootstrap the buffer
            ss += ss;
            if (ss >= 1.0)
                ss -= 1.0 - 2 * ulp;	// cyclic shift of 51 bits
        }
        u.buf_nums[1] += ulp;	// make u[1] (and only u[1]) "odd"

        for(s = (int)(nSeed&0x3fffffff), t = RNG_TT-1; t != 0; ) {
            for(j = RNG_KK-1; j > 0; j--) {
                u.buf_nums[j+j] = u.buf_nums[j];
                u.buf_nums[j+j-1] = 0.0;	// "square"
            }

            for(j = RNG_KK+RNG_KK-2; j >= RNG_KK; j--) {
                u.buf_nums[j-(RNG_KK-RNG_LL)] = PBUtil.MOD_SUM(u.buf_nums[j-(RNG_KK-RNG_LL)], u.buf_nums[j]);
                u.buf_nums[j-RNG_KK] = PBUtil.MOD_SUM(u.buf_nums[j-RNG_KK], u.buf_nums[j]);
            }

            if(PBUtil.IS_ODD(s))	// "multiply by z"
            {
                // shift the buffer cyclically
                for(j = RNG_KK; j > 0; j--)
                    u.buf_nums[j] = u.buf_nums[j-1];
                u.buf_nums[0] = u.buf_nums[RNG_KK];
                u.buf_nums[RNG_LL] = PBUtil.MOD_SUM(u.buf_nums[RNG_LL], u.buf_nums[RNG_KK]);
            }

            if(s != 0)
                s >>= 1;
            else
                t--;
        }

        for(j = 0; j < RNG_LL; j++)
            m_arrRanU.buf_nums[j+RNG_KK-RNG_LL] = u.buf_nums[j];
        for( ; j < RNG_KK; j++)
            m_arrRanU.buf_nums[j-RNG_LL] = u.buf_nums[j];
        for(j = 0; j < 10; j++)
            getArray(u, RNG_KK+RNG_KK-1);	// warm things up

        m_pRanfArr = dRanfArrStarted;
        m_nRndfIndex = 0;
    }

    public void getArray(RandomBuffer aa, int n) {
        int i, j;	// register int
        for(j = 0; j < RNG_KK; j++)
            aa.buf_nums[j] = m_arrRanU.buf_nums[j];
        for( ; j < n; j++)
            aa.buf_nums[j] = PBUtil.MOD_SUM(aa.buf_nums[j-RNG_KK], aa.buf_nums[j-RNG_LL]);
        for(i = 0; i < RNG_LL; i++,j++)
            m_arrRanU.buf_nums[i] = PBUtil.MOD_SUM(aa.buf_nums[j-RNG_KK], aa.buf_nums[j-RNG_LL]);
        for( ; i < RNG_KK; i++,j++)
            m_arrRanU.buf_nums[i] = PBUtil.MOD_SUM(aa.buf_nums[j-RNG_KK], m_arrRanU.buf_nums[i-RNG_LL]);
    }

    public float randGauss() {
        double dSum = 0.0;
        dSum += doubleNext();
        dSum += doubleNext();
        dSum += doubleNext();
        dSum += doubleNext();
        return (float) (dSum * 1.73205080757 - 3.46410161514);
    }

    public double doubleNext() {
        double ret = 0;
        if(m_pRanfArr.getNum(m_nRndfIndex) >= 0) {
            ret = m_pRanfArr.getNum(m_nRndfIndex);
            m_nRndfIndex++;
        }
        else    // *m_pRanfArr == -1이면 마지막을 가리키고 있는 것 ;)
            ret = doubleCycle();

        return ret;
    }

    public double doubleCycle() {
        getArray(m_arrRanfBuf, RNG_QUALITY);
        m_arrRanfBuf.buf_nums[RNG_KK] = -1;

        m_pRanfArr = m_arrRanfBuf;
        m_nRndfIndex = 1;

        return m_arrRanfBuf.buf_nums[0];
    }
}


class RandomBuffer {
    public double[] buf_nums;
    private int buf_count;

    public RandomBuffer(int count) {
        create(count);
    }

    public RandomBuffer(int count, double value) {
        create(count);
        for(int i = 0; i < count; i++)
            buf_nums[i] = value;
    }

    private void create(int count) {
        count = Math.max(1, count);
        buf_nums = new double[count];
        buf_count = count;
    }

    public double getNum(int index) {
        if(index < 0 || index >= buf_count)
            return -1;
        return buf_nums[index];
    }
}
