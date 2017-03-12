package org.androidtown.samplesurfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by BlackBean on 2017-03-12.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Bitmap mBitmap;
    private Bitmap srcImg;
    private Canvas mCanvas;
    private Paint mPaint;

    public MySurfaceView(Context context) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);
        mPaint = new Paint();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        int action = event.getAction();
        int x = (int)event.getX();
        int y = (int)event.getY();

        x -= srcImg.getWidth()/2;
        y -= srcImg.getHeight()/2;

        if(action == MotionEvent.ACTION_DOWN) {
            if(srcImg != null && mCanvas != null)
                mCanvas.drawBitmap(srcImg, x, y, mPaint);
        }

        draw();
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas();
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(Color.WHITE);
        srcImg = BitmapFactory.decodeResource(getResources(), R.drawable.sample);

        draw();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void draw() {
        Canvas _canvas = null;
        try {
            _canvas = mHolder.lockCanvas(null);
            super.draw(_canvas);
            _canvas.drawBitmap(mBitmap, 0, 0, null);
        } finally {
            if(_canvas != null)
                mHolder.unlockCanvasAndPost(_canvas);
        }
    }
}
