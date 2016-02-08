package com.atguihu.tencentqq.drag;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
/*
 * Created by 李健 on 2016/1/22.
 * 自定义的linerlayout
 */
public class MyLinerlayout extends LinearLayout {

    private DragLayout mDraglayout;

    public MyLinerlayout(Context context) {
        super(context);
    }

    public MyLinerlayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyLinerlayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setmDraglayout(DragLayout mDraglayout) {
        this.mDraglayout = mDraglayout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDraglayout.getmStatus() == DragLayout.Status.Close) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDraglayout.getmStatus() == DragLayout.Status.Close) {
            return super.onInterceptTouchEvent(event);
        } else {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mDraglayout.close();
            }
            return true;
        }
    }
}
