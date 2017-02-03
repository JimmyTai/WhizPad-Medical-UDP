package com.jimmytai.whizpad_medical_zenbo.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmytai.library.utils.activity.JActivity;
import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_zenbo.WhizPadClient;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadInfo;
import com.jimmytai.whizpad_medical_zenbo.demo.R;
import com.jimmytai.whizpad_medical_zenbo.demo.adapter.DeviceAdapter;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.LoadingDialog;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.PasswordDialog;
import com.jimmytai.whizpad_medical_zenbo.demo.thread.DiscoverDeviceThread;

import java.util.ArrayList;

public class MainActivity extends JActivity {

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = true;

    public Typeface FONT_LIGHT, FONT_BOLD;

    public WhizPadClient whizPadClient;

    private DiscoverDeviceThread discoverDeviceThread;
    private DeviceAdapter deviceAdapter;

    public WifiManager wifiManager;

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
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        whizPadClient = new WhizPadClient(getApplicationContext());
        getFonts();
        createDialog();
        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DiscoverDeviceThread.isRunning && discoverDeviceThread != null)
            discoverDeviceThread.stopListening();
        discoverDeviceThread = new DiscoverDeviceThread(this, whizPadClient);
        discoverDeviceThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        deviceAdapter.remove();
        if (DiscoverDeviceThread.isRunning && discoverDeviceThread != null)
            discoverDeviceThread.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* --- Listener --- */

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

    public void passwordResult(final int result, final WhizPadInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JLog.d(DEBUG, TAG, "password result -> " + result);
                if (loadingDialog != null && LoadingDialog.isShow)
                    loadingDialog.dismiss();
                String reason = null;
                switch (result) {
                    case 0x00:
                        reason = "配對失敗";
                        if (passwordDialog != null && PasswordDialog.isShow)
                            passwordDialog.dismiss();
                        break;
                    case 0x01:
                        reason = "配對成功";
                        if (passwordDialog != null && PasswordDialog.isShow)
                            passwordDialog.dismiss();
                        if (DiscoverDeviceThread.isRunning && discoverDeviceThread != null)
                            discoverDeviceThread.stopListening();
                        Intent intent = new Intent(jActivity, PadStatusActivity.class);
                        intent.putExtra(PadStatusActivity.EXTRA_DEVICE, info);
                        startActivity(intent);
                        break;
                    case 0x02:
                        reason = "密碼錯誤";
                        if (passwordDialog != null)
                            passwordDialog.setEnable();
                        break;
                }
                if (reason != null)
                    Toast.makeText(MainActivity.this, reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void discoverDevice(final WhizPadInfo item) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceAdapter.add(item);
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