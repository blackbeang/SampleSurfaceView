package org.androidtown.samplesurfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by ejchoi on 2017-03-13.
 */

public class MyTestView extends View{

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;

    public MyTestView(Context context) {
        super(context);
        mPaint = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas();
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(Color.WHITE);
        testDrawing();
    }

    private void testDrawing() {
        mPaint.setStrokeWidth(10);
        mPaint.setARGB(255, 0, 0, 255);
        mPaint.setStyle(Paint.Style.STROKE);

        mCanvas.drawLine(100, 100, 300, 100, mPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mBitmap != null)
            canvas.drawBitmap(mBitmap, 0, 0, null);
    }
}
