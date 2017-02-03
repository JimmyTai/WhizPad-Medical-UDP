package com.jimmytai.whizpad_medical_zenbo.demo.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmytai.library.utils.activity.JActivity;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadEvent;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadInfo;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadUnPairingAck;
import com.jimmytai.whizpad_medical_zenbo.demo.R;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.LoadingDialog;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.UnpairDialog;
import com.jimmytai.whizpad_medical_zenbo.demo.thread.PadStatusThread;

/**
 * Created by JimmyTai on 2016/12/22.
 */

public class PadStatusActivity extends JActivity {

    private static final String TAG = "PadStatusActivity";
    private static final boolean DEBUG = false;

    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";

    public Typeface FONT_LIGHT, FONT_BOLD;

    private int[] PAD_STATUS_PHOTO_ID = new int[]{R.mipmap.ic_pad_girl, R.mipmap.ic_pad_side_girl, R.mipmap.ic_pad_sit_girl, R.mipmap
            .ic_pad_lie_girl};

    public LoadingDialog loadingDialog;
    public UnpairDialog unpairDialog;

    private WhizPadInfo item;
    private PadStatusThread padStatusThread;

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
        item = getIntent().getParcelableExtra(EXTRA_DEVICE);
        getFonts();
        findViews();
        createDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPadStatusListen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPadStatusListen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* --- Functions --- */

    public void startPadStatusListen() {
        if (PadStatusThread.isRunning && padStatusThread != null)
            padStatusThread.stopListening();
        padStatusThread = new PadStatusThread(this, item);
        padStatusThread.start();
    }

    public void stopPadStatusListen() {
        if (PadStatusThread.isRunning && padStatusThread != null)
            padStatusThread.stopListening();
    }

    public void unPairPadResult(final WhizPadUnPairingAck.Response response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog != null && LoadingDialog.isShow)
                    loadingDialog.dismiss();
                String reason = null;
                if (response == WhizPadUnPairingAck.Response.FAIL) {
                    reason = "解除配對失敗";
                    if (unpairDialog != null && UnpairDialog.isShow)
                        unpairDialog.dismiss();
                } else if (response == WhizPadUnPairingAck.Response.FAIL_PASSWORD_INCORRECT) {
                    reason = "密碼錯誤";
                    if (unpairDialog != null)
                        unpairDialog.setEnable();
                } else if (response == WhizPadUnPairingAck.Response.FAIL_MAC_INCORRECT) {
                    reason = "不是配對的裝置，無法解除配對";
                    if (unpairDialog != null && UnpairDialog.isShow)
                        unpairDialog.dismiss();
                } else if (response == WhizPadUnPairingAck.Response.SUCCESS) {
                    reason = "解除配對成功";
                    if (unpairDialog != null && UnpairDialog.isShow)
                        unpairDialog.dismiss();
                }
                if (reason != null)
                    Toast.makeText(PadStatusActivity.this, reason, Toast.LENGTH_SHORT).show();
                if (response == WhizPadUnPairingAck.Response.SUCCESS)
                    finish();
            }
        });
    }

    public void updatePadStatus(final WhizPadEvent.Status status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_pad.setImageResource(PAD_STATUS_PHOTO_ID[status.ordinal()]);
            }
        });
    }

    /* --- Listener --- */

    private class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.padStatus_tv_unPair:
                    if (unpairDialog != null && !UnpairDialog.isShow) {
                        unpairDialog.show(getFragmentManager(), "UnpairDialog");
                        stopPadStatusListen();
                    }
                    break;
            }
        }
    }

    /* --- Views --- */

    private void createDialog() {
        loadingDialog = LoadingDialog.newInstance();
        loadingDialog.setCancelable(false);
        unpairDialog = UnpairDialog.newInstance(item);
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
