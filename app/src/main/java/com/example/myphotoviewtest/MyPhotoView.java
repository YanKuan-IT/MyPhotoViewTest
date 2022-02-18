package com.example.myphotoviewtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

public class MyPhotoView extends View {
    private static final String TAG = "AAAAAAAAAA";

    private Bitmap mBitmap;
    private Paint mPaint;

    // 偏移值
    private float originalOffsetX;
    private float originalOffsetY;

    // 放大：放到后图片在屏幕内，一边等于屏幕，另一边留白
    private float smallScale;
    // 放大：图片超过屏幕，一边等于屏幕，另一边超出屏幕
    private float bigScale;

    public MyPhotoView(Context context) {
        this(context, null);
    }
    public MyPhotoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public MyPhotoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure: ");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // 在onMeasure之后调用
    // 每次改变尺寸后会调用
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: ");

        super.onSizeChanged(w, h, oldw, oldh);

        // 偏移值 为了让图片居中
        originalOffsetX = (getWidth() - mBitmap.getWidth()) / 2;
        originalOffsetY = (getHeight() - mBitmap.getHeight()) / 2;

        // 如果 图片的宽高比 > 屏幕的宽高比，说明图片是横着的，否则是竖着的
        // 用于缩放，是横向全屏、竖向全屏等，留白处理
        if ((float)mBitmap.getWidth() / mBitmap.getHeight() > (float) getWidth() / getHeight()) {
            smallScale
        } else {

        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout: ");
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: ");
        super.onDraw(canvas);

//        canvas.translate((getWidth() - mBitmap.getWidth()) / 2, (getHeight() - mBitmap.getHeight()) / 2);

//        canvas.scale(1.5f, 1.5f, getWidth()/2, getHeight()/2);

        canvas.drawBitmap(mBitmap, originalOffsetX, originalOffsetY, mPaint);

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: ");
        return super.onTouchEvent(event);
    }
}
