package com.atguihu.tencentqq.drag;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/*
 * Created by 李健 on 2016/1/20.
 */
public class DragLayout extends FrameLayout {
    private ViewDragHelper mDraghelp;
    private View mLeftContent;
    private View mMainContent;
    private int mHeight;
    private int mWith;
    private int mRang;
    private OnDragStatusChangeListener mDragListener;
    private Status mStatus = Status.Close;


    public Status getmStatus() {
        return mStatus;
    }

    public void setmStatus(Status mStatus) {
        this.mStatus = mStatus;
    }

    /**
     * 状态的枚举类
     */
    public static enum Status {
        Close, Open, Draging
    }


    public interface OnDragStatusChangeListener {
        void onClose();

        void onOpen();

        void onDraging(float persent);
    }

    public void setDragStatusListener(OnDragStatusChangeListener mDragListener) {
        this.mDragListener = mDragListener;
    }

    public DragLayout(Context context) {
        super(context, null);
        mDraghelp = ViewDragHelper.create(this, mCallBack);//初始化对象
    }

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mDraghelp = ViewDragHelper.create(this, mCallBack);//1.初始化对象
    }

    /**
     * 实现滑动的重要方法
     */
    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDraghelp = ViewDragHelper.create(this, mCallBack);//1.初始化对象
    }


    ViewDragHelper.Callback mCallBack = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            //当capture被捕获调用
            Log.i("TAG", "onViewCaptured()" + capturedChild);
            super.onViewCaptured(capturedChild, activePointerId);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            //不能控制移动范围，控制动画速度
            return mRang;
        }

        //根据返回值决定view要去到的位置(重要方法)
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            Log.i("TAG", "oldLeft:" + child.getLeft() + "   dx:" + dx + "   left:" + left);

            if (child == mMainContent) {

                left = fixLeft(left);
            }
            return left;
        }


        /**
         * view的位置发生改变时候调用的方法
         * 把左面板的值，传递给右面板
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            int newLeft = left;
            if (changedView == mLeftContent) {
                //把变化量传递给主面板；
                newLeft = mMainContent.getLeft() + dx;
            }

            newLeft = fixLeft(newLeft);//进行修正，控制左右滑动的距离

            if (changedView == mLeftContent) {
                mLeftContent.layout(0, 0, mWith, mHeight);
                mMainContent.layout(newLeft, 0, newLeft + mWith, mHeight);
            }

            dispatchDragEvent(newLeft);
            invalidate();//重绘，为了兼容低版本
        }

        /**
         * 设置缩放时候的伴随动画
         */
        private void dispatchDragEvent(int newLeft) {
            float persent = newLeft * 1.0f / mRang;

            if (mDragListener != null) {
                mDragListener.onDraging(persent);
            }

            //给滑动状态设置监听
            Status preStatus = mStatus;
            mStatus = updataStatus(persent);
            if (preStatus != mStatus) {
                if (mStatus == Status.Close) {
                    if (mDragListener != null) {
                        mDragListener.onClose();
                    }
                } else if (mStatus == Status.Open) {
                    if (mDragListener != null) {
                        mDragListener.onOpen();
                    }
                }
            }
            animationView(persent);
        }

        /**
         * 状态改变的方法
         */
        private Status updataStatus(float persent) {

            if (persent == 0) {
                return Status.Close;
            } else if (persent == 1.0f) {
                return Status.Open;
            } else
                return Status.Draging;
        }

        /**
         * 面板动画方法
         */
        private void animationView(float persent) {
            //左面板缩放，平移透明
            ViewHelper.setScaleX(mLeftContent, evaluate(persent, 0.5f, 1.0f));
            ViewHelper.setScaleY(mLeftContent, evaluate(persent, 0.5f, 1.0f));

            //平移动画
            ViewHelper.setTranslationX(mLeftContent, evaluate(persent, -mWith / 2.0f, 0));
            ViewHelper.setAlpha(mLeftContent, evaluate(persent, 0.5f, 1.0f));

            //主面板的缩放动画(1.0-0.8)
            ViewHelper.setScaleX(mMainContent, evaluate(persent, 1.0f, 0.8f));
            ViewHelper.setScaleY(mMainContent, evaluate(persent, 1.0f, 0.8f));

            //背景的亮度动画(从黑色到透明)
            //颜色选择器
            getBackground().setColorFilter((Integer) evaluateColor(persent, Color.BLACK, Color.TRANSPARENT),
                    PorterDuff.Mode.SRC_OVER);
        }

        /**
         * 估值器
         */
        public Float evaluate(float fraction, Number startValue, Number endValue) {
            float startFloat = startValue.floatValue();
            return startFloat + fraction * (endValue.floatValue() - startFloat);
        }

        //颜色变化过度
        public Object evaluateColor(float fraction, Object startValue, Object endValue) {
            int startInt = (Integer) startValue;
            int startA = (startInt >> 24) & 0xff;
            int startR = (startInt >> 16) & 0xff;
            int startG = (startInt >> 8) & 0xff;
            int startB = startInt & 0xff;

            int endInt = (Integer) endValue;
            int endA = (endInt >> 24) & 0xff;
            int endR = (endInt >> 16) & 0xff;
            int endG = (endInt >> 8) & 0xff;
            int endB = endInt & 0xff;

            return (startA + (int) (fraction * (endA - startA))) << 24 |
                    (startR + (int) (fraction * (endR - startR))) << 16 |
                    (startG + (int) (fraction * (endG - startG))) << 8 |
                    (startB + (int) (fraction * (endB - startB)));
        }

        /**
         * 当view被释放的时候调用
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            //xvel 向水平移动的速度

            if (xvel == 0 && mMainContent.getLeft() > mRang / 2.0f) {
                open();
            } else if (xvel > 0) {
                open();
            } else {
                close();
            }
        }
    };

    @Override
    public void computeScroll() {
        super.computeScroll();
        //动画持续高频的移动时调用
        if (mDraghelp.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 打开关闭住面板的方法
     */
    public void close(Boolean isSmoth) {
        int finaleft = 0;
        if (isSmoth) {
            if (mDraghelp.smoothSlideViewTo(mMainContent, finaleft, 0)) {
                //true表示动画还没有结束要进行重绘
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainContent.layout(finaleft, 0, finaleft + mWith, mHeight);
        }
    }

    public void open(Boolean isSmoth) {
        int finaleft = mRang;
        if (isSmoth) {
            if (mDraghelp.smoothSlideViewTo(mMainContent, finaleft, 0)) {
                //true表示动画还没有结束要进行重绘
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainContent.layout(finaleft, 0, finaleft + mWith, mHeight);
        }
    }

    public void open() {
        open(true);
    }

    public void close() {//方法重载
        close(true);
    }

    /**
     * 传递触摸事件
     */
    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return mDraghelp.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDraghelp.processTouchEvent(event);
        return true;
    }

    /**
     * 得到两个视图的对象
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int count = getChildCount();
        if (count < 2) {
            throw new IllegalStateException("穿入的参数至少为两个");
        }

        if (!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("子view必须是viewgroup的子类");
        }
        mLeftContent = getChildAt(0);
        mMainContent = getChildAt(1);
    }

    /**
     * 修正left值的方法
     */
    private int fixLeft(int left) {
        if (left < 0) {
            return 0;
        } else if (left > mRang) {
            return mRang;
        }
        return left;
    }

    /**
     * 尺寸发生变化的时候调用，得到屏幕的宽高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = getHeight();
        mWith = getWidth();

        mRang = (int) (mWith * 0.6f);//移动的偏移值
    }
}

