package com.example.myphotoviewtest;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;
import androidx.annotation.Nullable;

public class MyPhotoView extends View {
    private static final String TAG = "AAAAAAAAAA";

    private Bitmap mBitmap;
    private Paint mPaint;

    // 偏移值 绘制bitmap时，
    private float mOriginalOffsetX;
    private float mOriginalOffsetY;

    // 小放大：放到后图片在屏幕内，一边等于屏幕，另一边留白
    private float mSmallScale;

    // 大放大：图片超过屏幕，一边等于屏幕，另一边超出屏幕
    private float mBigScale;

    // 放大系数
    private float OVER_SCALE_FACTOR = 1.5f;

    // 当前的缩放值
    private float mCurrentScale;

    // 单指操作
    private GestureDetector mGestureDetector;

    // 是否放大
    private boolean isEnlarge;

    // 滑动时，产生偏移
    private float mOffsetX;
    private float mOffsetY;

    private OverScroller mOverScroller;

    // 手势缩放
    private ScaleGestureDetector mScaleGestureDetector;

    // 是否缩放
    private boolean isScale;

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

    /**
     * 初始化
     * @param context
     */
    private void init(Context context) {
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);

        mPaint = new Paint();

        mGestureDetector = new GestureDetector(context, new PhotoGestureListener());

        mOverScroller = new OverScroller(context);

        mScaleGestureDetector = new ScaleGestureDetector(context, new PhotoScaleGestureDetector());
    }

    // 在onMeasure之后调用
    // 每次改变尺寸后会调用
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: ");

        super.onSizeChanged(w, h, oldw, oldh);

        // 偏移值 为了让图片居中 浮点数
        mOriginalOffsetX = (getWidth() - mBitmap.getWidth()) / 2f;
        mOriginalOffsetY = (getHeight() - mBitmap.getHeight()) / 2f;

        // 如果 图片的宽高比 > 屏幕的宽高比，说明图片是横着的，否则是竖着的
        // 用于缩放，是横向全屏、竖向全屏等，留白处理
        if ((float)mBitmap.getWidth() / mBitmap.getHeight() > (float) getWidth() / getHeight()) {
            // 图片是横向的
            // 图片的宽度放大到和屏幕一样，高度也就会留白
            mSmallScale = (float) getWidth() / mBitmap.getWidth();
            // 图片的高度放大到和屏幕一样，宽度也就会超出屏幕了
            mBigScale = (float) getHeight() / mBitmap.getHeight() * OVER_SCALE_FACTOR;
        } else {
            // 图片是纵向的
            // 图片的高度放大到和屏幕一样，宽度也就会留白
            mSmallScale = (float) getHeight() / mBitmap.getHeight();
            // 图片的宽度放大到和屏幕一样，高度也就会超出屏幕了
            mBigScale = (float) getWidth() / mBitmap.getWidth() * OVER_SCALE_FACTOR;
        }
        // 当前的缩放比例 赋值
        mCurrentScale = mSmallScale;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: ");
        super.onDraw(canvas);

        // 在已经缩放的基础上，进行移动时，需要计算当前移动对应的距离是多少
        // 不同的缩放比例，对应的偏移不一样
        // 根据当前的缩放比例，重新计算偏移
        float scaleFraction = (mCurrentScale - mSmallScale) / (mBigScale - mSmallScale);
        // 移动的距离 x 放大的比例，就是真实的移动距离
        canvas.translate(mOffsetX * scaleFraction, mOffsetY * scaleFraction);

        // x、y轴进行缩放，缩放比例为mCurrentScale; 以px、px参数为基准点进行缩放
        canvas.scale(mCurrentScale, mCurrentScale, getWidth()/2f, getHeight()/2f);

        // 绘制bitmap
        canvas.drawBitmap(mBitmap, mOriginalOffsetX, mOriginalOffsetY, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: ");

        boolean result = mScaleGestureDetector.onTouchEvent(event);

        if (!mScaleGestureDetector.isInProgress()) {
            result = mGestureDetector.onTouchEvent(event);
        }
        return result;
    }

    private ObjectAnimator mScaleAnimator;
    private ObjectAnimator getScaleAnimator() {
        if (mScaleAnimator == null) {
            mScaleAnimator = ObjectAnimator.ofFloat(this, "mCurrentScale", 0);
        }
        if (isScale) {
            isScale = false;
            mScaleAnimator.setFloatValues(mSmallScale, mCurrentScale);
        } else {
            mScaleAnimator.setFloatValues(mSmallScale, mBigScale);
        }
        return mScaleAnimator;
    }

    // 属性动画，通过反射调用该方法设置值
    public void setMCurrentScale(float mCurrentScale) {
        this.mCurrentScale = mCurrentScale;
        invalidate();
    }
    public float getMCurrentScale() {
        return mCurrentScale;
    }

    // 使偏移不能出现白边
    private void fixOffsets() {
        // 偏移是在一个区间内的
        mOffsetX = Math.min(mOffsetX, (mBitmap.getWidth() * mBigScale - getWidth())/2);
        mOffsetX = Math.max(mOffsetX, -(mBitmap.getWidth() * mBigScale - getWidth())/2);

        mOffsetY = Math.min(mOffsetY, (mBitmap.getHeight() * mBigScale - getHeight())/2);
        mOffsetY = Math.max(mOffsetY, -(mBitmap.getHeight() * mBigScale - getHeight())/2);
    }

    class PhotoGestureListener extends GestureDetector.SimpleOnGestureListener {
        // 点击，抬起时触发
        // 双击，第二次抬起时触发
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp: ");
            return super.onSingleTapUp(e);
        }

        // 长按触发，300ms
        // 触发顺序：onDown --> onShowPress --> onLongPress
        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress: ");
            super.onLongPress(e);
        }

        /**
         * 滑动，类似move
         * @param e1
         * @param e2
         * @param distanceX 在 X轴 上的滑动的距离， 旧位置 - 新位置，也就是有可能是负的
         * @param distanceY 在 Y轴 上的滑动的距离， 旧位置 - 新位置，也就是有可能是负的
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll: ");
            // 在只有放大的基础上才会进行滑动
            if (isEnlarge) {
                mOffsetX -= distanceX;
                mOffsetY -= distanceY;
                // 处理留白
                fixOffsets();
                // 重新绘制
                invalidate();
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        /**
         * 抛掷，按下屏幕快速滑动松开，使其还有惯性的效果
         * @param e1 第一个 down事件
         * @param e2 最后一个 move事件
         * @param velocityX 在 X轴 上的滑动速度
         * @param velocityY 在 Y轴 上的滑动速度
         * @return
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: ");
            if (isEnlarge) {
                // 只会处理一次
                mOverScroller.fling(
                        (int) mOffsetX,
                        (int) mOffsetY,
                        (int) velocityX,
                        (int) velocityY,
                        -(int)(mBitmap.getWidth() * mBigScale - getWidth())/2,
                        (int)(mBitmap.getWidth() * mBigScale - getWidth())/2,
                        -(int)(mBitmap.getHeight() * mBigScale - getHeight())/2,
                        (int)(mBitmap.getHeight() * mBigScale - getHeight())/2,
                        600,
                        600
                );
            }

            postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    // 动画还在执行返回true
                    if (mOverScroller.computeScrollOffset()) {
                        mOffsetX = mOverScroller.getCurrX();
                        mOffsetY = mOverScroller.getCurrY();
                        invalidate();
                        // 每帧动画执行一次，性能更好
                        postOnAnimation(this);
                    }
                }
            });

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        // 延时触发 100ms  如实现点击的效果
        @Override
        public void onShowPress(MotionEvent e) {
            Log.d(TAG, "onShowPress: ");
            super.onShowPress(e);
        }

        // 用户按下屏幕就会触发
        // 需要返回true，表示消费
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown: ");
            return true;
        }

        // 双击 第二次按下的时候触发  40ms - 300ms  当小于40ms的时候，有可能是抖动导致的
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: ");
            isEnlarge = !isEnlarge;
            if (isEnlarge) {
                mOffsetX = (e.getX() - getWidth()/2f) - (e.getX() - getWidth()/2f)*mBigScale/mSmallScale;
                mOffsetY = (e.getY() - getHeight()/2f) - (e.getY() - getHeight()/2f)*mBigScale/mSmallScale;
                fixOffsets();
                getScaleAnimator().start();
            } else {
                getScaleAnimator().reverse();
            }
            return super.onDoubleTap(e);
        }

        // 双击的第二次的down、move、up事件触发
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.d(TAG, "onDoubleTapEvent: ");
            return super.onDoubleTapEvent(e);
        }

        // 单击按下时触发，双击时不触发
        // 延时300ms触发
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed: ");
            return super.onSingleTapConfirmed(e);
        }
    }

    class PhotoScaleGestureDetector implements ScaleGestureDetector.OnScaleGestureListener {
        float initialScale;
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if ((mCurrentScale > mSmallScale && !isEnlarge) || (mCurrentScale == mSmallScale && isEnlarge)) {
                isEnlarge = !isEnlarge;
            }
            mCurrentScale = initialScale * detector.getScaleFactor();
            isScale = true;
            invalidate();
            return false;
        }

        // 返回true，消费事件
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            initialScale = mCurrentScale;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }
}
