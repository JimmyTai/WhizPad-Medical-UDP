package com.jimmytai.whizpad_medical_zenbo.demo.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmytai.library.utils.activity.JActivity;
import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_udp.WhizPadClient;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadInfo;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadPairingResult;
import com.jimmytai.whizpad_medical_zenbo.demo.R;
import com.jimmytai.whizpad_medical_zenbo.demo.adapter.DeviceAdapter;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.LoadingDialog;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.PasswordDialog;

import java.util.ArrayList;

public class MainActivity extends JActivity {

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = true;

    public Typeface FONT_LIGHT, FONT_BOLD;

    public WhizPadClient whizPadClient;
    private MyScanCallback myScanCallback;

    private DeviceAdapter deviceAdapter;

    public PasswordDialog passwordDialog;
    public LoadingDialog loadingDialog;

    private TextView tv_appBarTitle;
    private ListView lv_device;
    private RelativeLayout rl_multiplePad;

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
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFonts();
        createDialog();
        findViews();
        whizPadClient = WhizPadClient.getInstance();
        whizPadClient.setScanCallback(myScanCallback = new MyScanCallback());
    }

    @Override
    protected void onResume() {
        super.onResume();
        whizPadClient.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        deviceAdapter.remove();
        whizPadClient.stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* --- Listener --- */

    private class MyScanCallback implements WhizPadClient.ScanCallback {

        @Override
        public void onScan(WhizPadInfo info) {
            deviceAdapter.add(info);
        }
    }

    private class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.main_rl_multiplePad:
                    ArrayList<WhizPadInfo> list = deviceAdapter.getPaired();
                    if (list.size() < 2) {
                        Toast.makeText(jActivity, list.size() == 1 ? "只有配對一張床墊喔" : "沒有配對的床墊", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(jActivity, MultiplePadActivity.class);
                    intent.putParcelableArrayListExtra(MultiplePadActivity.EXTRA_ITEM, list);
                    startActivity(intent);
                    break;
            }
        }
    }

    /* --- Functions --- */

    public void passwordResult(final WhizPadPairingResult result, final WhizPadInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JLog.d(DEBUG, TAG, "password result -> " + result.getResponse());
                if (loadingDialog != null && LoadingDialog.isShow)
                    loadingDialog.dismiss();
                String reason = null;
                switch (result.getResponse()) {
                    case FAIL:
                        reason = "配對失敗";
                        if (passwordDialog != null && PasswordDialog.isShow)
                            passwordDialog.dismiss();
                        break;
                    case SUCCESS:
                        reason = "配對成功";
                        if (passwordDialog != null && PasswordDialog.isShow)
                            passwordDialog.dismiss();
                        whizPadClient.stopScan();
                        Intent intent = new Intent(jActivity, PadStatusActivity.class);
                        intent.putExtra(PadStatusActivity.EXTRA_DEVICE, info);
                        startActivity(intent);
                        break;
                    case FAIL_INCORRECT_PASSWORD:
                        reason = "密碼錯誤";
                        if (passwordDialog != null)
                            passwordDialog.setEnable();
                        break;
                }
                Toast.makeText(MainActivity.this, reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* --- Views --- */

    private void createDialog() {
        loadingDialog = LoadingDialog.newInstance();
        loadingDialog.setCancelable(false);
    }

    private void getFonts() {
        FONT_LIGHT = Typeface.createFromAsset(getAssets(), "fonts/light.ttf");
        FONT_BOLD = Typeface.createFromAsset(getAssets(), "fonts/noto_bold.ttf");
    }

    private void findViews() {
        tv_appBarTitle = (TextView) findViewById(R.id.main_tv_appBarTitle);
        tv_appBarTitle.setTypeface(FONT_LIGHT);
        lv_device = (ListView) findViewById(R.id.main_lv_device);
        deviceAdapter = new DeviceAdapter(this, whizPadClient);
        lv_device.setAdapter(deviceAdapter);
        rl_multiplePad = (RelativeLayout) findViewById(R.id.main_rl_multiplePad);
        rl_multiplePad.setOnClickListener(new MyClickListener());
    }
}