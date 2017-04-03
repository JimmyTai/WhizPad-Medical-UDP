package com.jimmytai.whizpad_medical_zenbo.demo.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmytai.library.utils.JResUtils;
import com.jimmytai.library.utils.JScreenUtils;
import com.jimmytai.library.utils.activity.JActivity;
import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_udp.WhizPadClient;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadEvent;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadInfo;
import com.jimmytai.whizpad_medical_zenbo.demo.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by JimmyTai on 2017/1/23.
 */

public class MultiplePadActivity extends JActivity {

    private static final String TAG = MultiplePadActivity.class.getSimpleName();
    private static final boolean DEBUG = false;

    public static final String EXTRA_ITEM = "EXTRA_ITEM";
    private int[] PAD_STATUS_PHOTO_ID = new int[]{R.mipmap.ic_pad_girl, R.mipmap.ic_pad_side_girl, R.mipmap.ic_pad_sit_girl, R.mipmap
            .ic_pad_lie_girl};

    public Typeface FONT_LIGHT;

    public WhizPadClient client;
    private MyPadEventCallback myPadEventCallback;

    private HashMap<String, Integer> statusHashMap = new HashMap<>();
    private List<WhizPadInfo> list;
    private int selectedPos = 0;

    private TextView tv_title;
    private RelativeLayout rl_content, rl_footer;
    private TextView[] tv_footers;
    private ImageView iv_pad;

    @Override
    public String setTag() {
        return TAG;
    }

    @Override
    public boolean setDebug() {
        return DEBUG;
    }

    @Override
    public int setLayout() {
        return R.layout.activity_multiple_pad;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = getIntent().getParcelableArrayListExtra(EXTRA_ITEM);
        getFonts();
        findViews();
        setFooterViews();
        client = WhizPadClient.getInstance();
        /* timeout millis can not less than 15000ms */
        client.setHeartBeatTimeout(15 * 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        client.listenEvent(list, myPadEventCallback = new MyPadEventCallback());
    }

    @Override
    protected void onPause() {
        super.onPause();
        client.stopListenEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* --- Listener --- */

    private class MyPadEventCallback implements WhizPadClient.EventCallback {

        @Override
        public void onEvent(WhizPadEvent event) {
            statusHashMap.put(event.getDeviceId(), event.getStatus().ordinal());
            if (!list.get(selectedPos).getDeviceId().equals(event.getDeviceId()))
                return;
            iv_pad.setImageResource(PAD_STATUS_PHOTO_ID[event.getStatus().ordinal()]);
        }

        @Override
        public void onStatus(WhizPadInfo info, boolean isOnline) {
            JLog.d(DEBUG, TAG, "床墊 " + info.getDeviceId() + " 目前： " + (isOnline ? "上線" : "離線"));
            Toast.makeText(jActivity, "床墊 " + info.getDeviceId() + " 目前： " + (isOnline ? "上線" : "離線"), Toast.LENGTH_SHORT).show();
            statusHashMap.put(info.getDeviceId(), !isOnline ? -1 : WhizPadEvent.Status.BED_EMPTY.ordinal());
            if (!list.get(selectedPos).getDeviceId().equals(info.getDeviceId()))
                return;
            iv_pad.setImageResource(isOnline ? PAD_STATUS_PHOTO_ID[WhizPadEvent.Status.BED_EMPTY.ordinal()] : R.mipmap.ic_pad_girl_offline);
        }
    }

    private class MyClickListener implements View.OnClickListener {

        private int pos;

        public MyClickListener(int pos) {
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            selectedPos = pos;
            Integer status = statusHashMap.get(list.get(pos).getDeviceId());
            if (status != null)
                if (status == -1)
                    iv_pad.setImageResource(R.mipmap.ic_pad_girl_offline);
                else
                    iv_pad.setImageResource(PAD_STATUS_PHOTO_ID[status]);
            else
                iv_pad.setImageResource(PAD_STATUS_PHOTO_ID[0]);
            for (int i = 0; i < list.size(); i++) {
                tv_footers[i].setBackgroundResource(selectedPos == i ? R.drawable.bg_item_device_dark : R.drawable.bg_item_device_normal);
            }
        }
    }

    /* --- Views --- */

    private void setFooterViews() {
        int totalWidth = list.size() * (int) JScreenUtils.dp2px(jActivity, 120) + list.size() - 1;
        if (totalWidth <= JScreenUtils.getWidth(jActivity)) {
            LinearLayout layout = new LinearLayout(jActivity);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                    .MATCH_PARENT);
            layout.setLayoutParams(params);
            rl_footer.removeAllViews();
            rl_footer.addView(layout);
            layout.removeAllViews();
            tv_footers = new TextView[list.size()];
            for (int i = 0; i < list.size(); i++) {
                TextView tv = new TextView(jActivity);
                tv.setOnClickListener(new MyClickListener(i));
                tv.setClickable(true);
                tv.setBackgroundResource(i == selectedPos ? R.drawable.bg_item_device_dark : R.drawable.bg_item_device_normal);
                tv.setText(list.get(i).getDeviceId());
                tv.setTypeface(FONT_LIGHT);
                tv.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams tvParam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                tvParam.weight = 1;
                tv.setLayoutParams(tvParam);
                layout.addView(tv);
                tv_footers[i] = tv;
                if (i < list.size() - 1) {
                    View view = new View(jActivity);
                    view.setBackgroundColor(JResUtils.getColor(jActivity, R.color.utilsDividerLB));
                    LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(1, (int) JScreenUtils.dp2px(jActivity, 25));
                    viewParams.gravity = Gravity.CENTER;
                    view.setLayoutParams(viewParams);
                    layout.addView(view);
                }
            }
        } else {
            HorizontalScrollView hsv = new HorizontalScrollView(jActivity);
            RelativeLayout.LayoutParams hsvParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                    .LayoutParams.MATCH_PARENT);
            hsv.setLayoutParams(hsvParams);
            LinearLayout layout = new LinearLayout(jActivity);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                    .MATCH_PARENT);
            layout.setLayoutParams(params);
            hsv.addView(layout);
            rl_footer.removeAllViews();
            rl_footer.addView(hsv);
            layout.removeAllViews();
            tv_footers = new TextView[list.size()];
            for (int i = 0; i < list.size(); i++) {
                TextView tv = new TextView(jActivity);
                tv.setOnClickListener(new MyClickListener(i));
                tv.setClickable(true);
                tv.setBackgroundResource(i == selectedPos ? R.drawable.bg_item_device_dark : R.drawable.bg_item_device_normal);
                tv.setText(list.get(i).getDeviceId());
                tv.setTypeface(FONT_LIGHT);
                tv.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams tvParam = new LinearLayout.LayoutParams((int) JScreenUtils.dp2px(jActivity, 120), ViewGroup
                        .LayoutParams.MATCH_PARENT);
                tv.setLayoutParams(tvParam);
                layout.addView(tv);
                tv_footers[i] = tv;
                if (i < list.size() - 1) {
                    View view = new View(jActivity);
                    view.setBackgroundColor(JResUtils.getColor(jActivity, R.color.utilsDividerLB));
                    LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(1, (int) JScreenUtils.dp2px(jActivity, 25));
                    viewParams.gravity = Gravity.CENTER;
                    view.setLayoutParams(viewParams);
                    layout.addView(view);
                }
            }
        }
    }

    private void getFonts() {
        FONT_LIGHT = Typeface.createFromAsset(getAssets(), "fonts/light.ttf");
    }

    private void findViews() {
        tv_title = (TextView) findViewById(R.id.multiplePad_tv_title);
        tv_title.setTypeface(FONT_LIGHT);
        rl_content = (RelativeLayout) findViewById(R.id.multiplePad_rl_content);
        rl_footer = (RelativeLayout) findViewById(R.id.multiplePad_rl_footer);
        iv_pad = (ImageView) findViewById(R.id.multiplePad_iv_pad);
    }
}
