package org.androidtown.samplesurfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import org.androidtown.mypaintlib.PaintBrush;
import org.androidtown.mypaintlib.PaintBrushDefine;
import org.androidtown.mypaintlib.PaintBrushSetter;

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
        int x1 = 100, y1 = 100, x2 = 300, y2 = 100;

        /*
        mPaint.setStrokeWidth(10);
        mPaint.setARGB(255, 0, 0, 255);
        mPaint.setStyle(Paint.Style.STROKE);

        mCanvas.drawLine(x1, y1, x2, y2, mPaint);
        */

        PaintBrush sExtPenBrush = new PaintBrush(mBitmap);
        PaintBrushSetter.LeavesPaintBrushSetting(sExtPenBrush);
        //PaintBrushSetter.DefaultPaintBrushSetting(sExtPenBrush);
        sExtPenBrush.strokeTo(x1, y1, PaintBrushDefine.PAINT_BRUSH_PRESSURE, PaintBrushDefine.PAINT_BRUSH_XTILT, PaintBrushDefine.PAINT_BRUSH_YTILT, PaintBrushDefine.PAINT_BRUSH_DTIME);
        sExtPenBrush.strokeTo(x2, y2, PaintBrushDefine.PAINT_BRUSH_PRESSURE, PaintBrushDefine.PAINT_BRUSH_XTILT, PaintBrushDefine.PAINT_BRUSH_YTILT, PaintBrushDefine.PAINT_BRUSH_DTIME);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mBitmap != null)
            canvas.drawBitmap(mBitmap, 0, 0, null);
    }
}
