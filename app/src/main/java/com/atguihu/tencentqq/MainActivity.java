package com.atguihu.tencentqq;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.atguihu.tencentqq.drag.DragLayout;
import com.atguihu.tencentqq.drag.MyLinerlayout;
import com.atguihu.tencentqq.utils.Cheeses;
import com.atguihu.tencentqq.utils.Utils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.Random;

public class MainActivity extends ListActivity {

    private DragLayout mDraglayout;
//   private SwipeListAdapter adapt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        setContentView(R.layout.activity_main);

        mDraglayout = (DragLayout) findViewById(R.id.dl);

        final ListView lv_left = (ListView) findViewById(android.R.id.list);
        final ListView lv_main = (ListView) findViewById(R.id.lv_main);
        final ImageView iv_header = (ImageView) findViewById(R.id.iv_header);
        MyLinerlayout myLinerlayout = (MyLinerlayout) findViewById(R.id.mll);

        myLinerlayout.setmDraglayout(mDraglayout);//设置draglayout

        lv_left.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings) {


            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mText = (TextView) view.findViewById(android.R.id.text1);
                mText.setTextColor(Color.WHITE);
                return view;
            }
        });

        lv_main.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.NAMES));

        mDraglayout.setDragStatusListener(new DragLayout.OnDragStatusChangeListener() {
            @Override
            public void onClose() {
                Utils.showToast(MainActivity.this, " onClose()");
                //关闭的时候图标晃动
                ObjectAnimator mAnim = ObjectAnimator.ofFloat(iv_header, "translationX", 15.0f);//属性动画
                mAnim.setInterpolator(new CycleInterpolator(4));//差值器晃动4次
                mAnim.setDuration(500);
                mAnim.start();
            }

            @Override
            public void onOpen() {
                Utils.showToast(MainActivity.this, " onOpen()");

                Random random = new Random();
                int next = random.nextInt(50);
                lv_left.smoothScrollToPosition(next);

            }

            @Override
            public void onDraging(float persent) {

                ViewHelper.setAlpha(iv_header, 1 - persent);//设置打开时候图片消失

            }
        });
    }
}
