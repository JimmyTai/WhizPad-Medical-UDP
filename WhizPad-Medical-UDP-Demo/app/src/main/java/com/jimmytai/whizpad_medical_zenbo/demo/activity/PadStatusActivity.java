package com.jimmytai.whizpad_medical_zenbo.demo.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmytai.library.utils.activity.JActivity;
import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_udp.WhizPadClient;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadEvent;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadInfo;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadUnPairingResult;
import com.jimmytai.whizpad_medical_zenbo.demo.R;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.LoadingDialog;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.UnpairDialog;

/**
 * Created by JimmyTai on 2016/12/22.
 */

public class PadStatusActivity extends JActivity {

    private static final String TAG = PadStatusActivity.class.getSimpleName();
    private static final boolean DEBUG = false;

    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";

    public Typeface FONT_LIGHT, FONT_BOLD;

    private int[] PAD_STATUS_PHOTO_ID = new int[]{R.mipmap.ic_pad_girl, R.mipmap.ic_pad_side_girl, R.mipmap.ic_pad_sit_girl, R.mipmap
            .ic_pad_lie_girl};

    public WhizPadClient client;
    public MyPadEventCallback myPadEventCallback;

    public LoadingDialog loadingDialog;
    public UnpairDialog unpairDialog;

    public WhizPadInfo info;

    private TextView tv_appBarTitle, tv_unpair;
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
        return R.layout.activity_pad_status;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        info = getIntent().getParcelableExtra(EXTRA_DEVICE);
        getFonts();
        findViews();
        createDialog();
        client = WhizPadClient.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        client.listenEvent(info, myPadEventCallback = new MyPadEventCallback());
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

    /* --- Functions --- */

    public void unPairPadResult(final WhizPadUnPairingResult result) {
        if (loadingDialog != null && LoadingDialog.isShow)
            loadingDialog.dismiss();
        WhizPadUnPairingResult.Response response = result.getResponse();
        String reason = null;
        if (response == WhizPadUnPairingResult.Response.FAIL) {
            reason = "解除配對失敗";
            if (unpairDialog != null && UnpairDialog.isShow)
                unpairDialog.dismiss();
        } else if (response == WhizPadUnPairingResult.Response.FAIL_PASSWORD_INCORRECT) {
            reason = "密碼錯誤";
            if (unpairDialog != null)
                unpairDialog.setEnable();
        } else if (response == WhizPadUnPairingResult.Response.FAIL_MAC_INCORRECT) {
            reason = "不是配對的裝置，無法解除配對";
            if (unpairDialog != null && UnpairDialog.isShow)
                unpairDialog.dismiss();
        } else if (response == WhizPadUnPairingResult.Response.SUCCESS) {
            reason = "解除配對成功";
            if (unpairDialog != null && UnpairDialog.isShow)
                unpairDialog.dismiss();
        } else if (response == WhizPadUnPairingResult.Response.TIMEOUT) {
            reason = "逾時錯誤";
            if (unpairDialog != null && UnpairDialog.isShow)
                unpairDialog.dismiss();
        }
        if (reason != null)
            Toast.makeText(PadStatusActivity.this, reason, Toast.LENGTH_SHORT).show();
        if (response == WhizPadUnPairingResult.Response.SUCCESS)
            finish();
    }

    /* --- Listener --- */

    private class MyPadEventCallback implements WhizPadClient.EventCallback {

        @Override
        public void onEvent(WhizPadEvent event) {
            iv_pad.setImageResource(PAD_STATUS_PHOTO_ID[event.getStatus().ordinal()]);
        }

        @Override
        public void onStatus(WhizPadInfo info, boolean isOnline) {
            JLog.d(DEBUG, TAG, "床墊 " + info.getDeviceId() + " 目前： " + (isOnline ? "上線" : "離線"));
            Toast.makeText(jActivity, "床墊 " + info.getDeviceId() + " 目前： " + (isOnline ? "上線" : "離線"), Toast.LENGTH_SHORT).show();
            if (!isOnline) {
                iv_pad.setImageResource(R.mipmap.ic_pad_girl_offline);
            } else {
                iv_pad.setImageResource(R.mipmap.ic_pad_girl);
                client.getLatestEvent(info);
            }
        }
    }

    private class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.padStatus_tv_unPair:
                    if (unpairDialog != null && !UnpairDialog.isShow)
                        unpairDialog.show(getFragmentManager(), UnpairDialog.class.getSimpleName());
                    break;
            }
        }
    }

    /* --- Views --- */

    private void createDialog() {
        loadingDialog = LoadingDialog.newInstance();
        loadingDialog.setCancelable(false);
        unpairDialog = UnpairDialog.newInstance(info);
        unpairDialog.setCancelable(false);
    }

    private void getFonts() {
        FONT_LIGHT = Typeface.createFromAsset(getAssets(), "fonts/light.ttf");
        FONT_BOLD = Typeface.createFromAsset(getAssets(), "fonts/noto_bold.ttf");
    }

    private void findViews() {
        tv_appBarTitle = (TextView) findViewById(R.id.padStatus_tv_appBarTitle);
        tv_appBarTitle.setTypeface(FONT_LIGHT);
        iv_pad = (ImageView) findViewById(R.id.padStatus_iv_pad);
        tv_unpair = (TextView) findViewById(R.id.padStatus_tv_unPair);
        tv_unpair.setTypeface(FONT_LIGHT);
        tv_unpair.setOnClickListener(new MyClickListener());
    }
}
