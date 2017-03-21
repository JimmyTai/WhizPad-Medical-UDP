package com.jimmytai.whizpad_medical_zenbo.demo.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_udp.WhizPadClient;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadInfo;
import com.jimmytai.library.whizpad_medical_udp.item.WhizPadPairedInfo;
import com.jimmytai.whizpad_medical_zenbo.demo.R;
import com.jimmytai.whizpad_medical_zenbo.demo.activity.MainActivity;
import com.jimmytai.whizpad_medical_zenbo.demo.activity.PadStatusActivity;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.LoadingDialog;
import com.jimmytai.whizpad_medical_zenbo.demo.dialog.PasswordDialog;
import com.jimmytai.whizpad_medical_zenbo.demo.utils.WifiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by JimmyTai on 2016/10/19.
 */

public class DeviceAdapter extends BaseAdapter {

    private static final String TAG = "DeviceAdapter";
    private static final boolean DEBUG = true;

    private MainActivity activity;
    private WhizPadClient client;
    private List<WhizPadInfo> list;

    public DeviceAdapter(MainActivity activity, WhizPadClient client) {
        this.activity = activity;
        this.client = client;
        this.list = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public WhizPadInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void add(WhizPadInfo item) {
        for (WhizPadInfo i : list) {
            if (i.getDeviceId().equals(item.getDeviceId()))
                return;
        }
        JLog.d(DEBUG, TAG, "add device: " + item.getDeviceId());
        list.add(item);
        notifyDataSetChanged();
    }

    public void remove() {
        list.clear();
        notifyDataSetChanged();
    }

    public ArrayList<WhizPadInfo> getPaired() {
        ArrayList<WhizPadInfo> list = new ArrayList<>();
        for (WhizPadInfo info : this.list) {
            if (info.isPaired())
                list.add(info);
        }
        return list;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(activity).inflate(R.layout.item_device, null, false);
            viewHolder.rl_root = (RelativeLayout) view.findViewById(R.id.itemDevice_rl_root);
            viewHolder.view_paired = view.findViewById(R.id.itemDevice_view_paired);
            viewHolder.tv_ip = (TextView) view.findViewById(R.id.itemDevice_tv_ip);
            viewHolder.tv_ip.setTypeface(activity.FONT_LIGHT);
            viewHolder.tv_id = (TextView) view.findViewById(R.id.itemDevice_tv_id);
            viewHolder.tv_id.setTypeface(activity.FONT_BOLD);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.rl_root.setOnClickListener(new MyClickListener(position));
        if (position % 2 == 0) {
            viewHolder.rl_root.setBackgroundResource(R.drawable.bg_item_device_dark);
        } else {
            viewHolder.rl_root.setBackgroundResource(R.drawable.bg_item_device_normal);
        }

        viewHolder.view_paired.setVisibility(list.get(position).isPaired() ? View.VISIBLE : View.INVISIBLE);
        viewHolder.tv_ip.setText(list.get(position).getIp());
        viewHolder.tv_id.setText(list.get(position).getDeviceId());

        return view;
    }

    private class ViewHolder {
        RelativeLayout rl_root;
        View view_paired;
        TextView tv_ip, tv_id;
    }

    private class MyActionListener extends WhizPadClient.ActionListener {

        private WhizPadInfo info;

        public MyActionListener(WhizPadInfo info) {
            this.info = info;
        }

        @Override
        public void onPairedInfo(WhizPadPairedInfo pairedInfo) {
            if (activity.loadingDialog != null && LoadingDialog.isShow)
                activity.loadingDialog.dismiss();
            JLog.d(DEBUG, TAG, "onPairedInfo response: " + pairedInfo.getResponse());
            if (pairedInfo.getResponse() == WhizPadPairedInfo.Response.PAIRED) {
                String localIpAddr = WifiUtils.getIPv4Str(activity);
                byte[] localMac = WifiUtils.getMacAddr();
                String localMacAddr = localMac == null ? "00:00:00:00:00:00" : String.format(Locale.getDefault(),
                        "%02X:%02X:%02X:%02X:%02X:%02X", localMac[0], localMac[1], localMac[2], localMac[3], localMac[4],
                        localMac[5]);
                if (pairedInfo.getMac().equals(localMacAddr)) {
                    if (pairedInfo.getIp().equals(localIpAddr)) {
                        Intent intent = new Intent(activity, PadStatusActivity.class);
                        intent.putExtra(PadStatusActivity.EXTRA_DEVICE, info);
                        activity.startActivity(intent);
                    } else {
                        activity.passwordDialog = PasswordDialog.newInstance(PasswordDialog.Page.IP_CHANGED, info);
                        activity.passwordDialog.setCancelable(false);
                        activity.passwordDialog.show(activity.getFragmentManager(), "PasswordDialog");
                    }
                } else {
                    activity.passwordDialog = PasswordDialog.newInstance(PasswordDialog.Page.ALARM_PAIRED, info);
                    activity.passwordDialog.setCancelable(false);
                    activity.passwordDialog.show(activity.getFragmentManager(), "PasswordDialog");
                }
            } else if (pairedInfo.getResponse() == WhizPadPairedInfo.Response.NOT_PAIRED) {
                    /* not paired */
                activity.passwordDialog = PasswordDialog.newInstance(PasswordDialog.Page.SET_PASSWORD, info);
                activity.passwordDialog.setCancelable(false);
                activity.passwordDialog.show(activity.getFragmentManager(), "PasswordDialog");
            } else {
                Toast.makeText(activity, "發生不明問題，請稍後再試！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class MyClickListener implements View.OnClickListener {

        private int position;

        MyClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (activity.whizPadClient.getPairedInfo(list.get(position), new MyActionListener(list.get(position)))) {
                if (activity.loadingDialog != null && !LoadingDialog.isShow) {
                    activity.loadingDialog.show(activity.getFragmentManager(), LoadingDialog.class.getSimpleName());
                }
            }
        }
    }
}
