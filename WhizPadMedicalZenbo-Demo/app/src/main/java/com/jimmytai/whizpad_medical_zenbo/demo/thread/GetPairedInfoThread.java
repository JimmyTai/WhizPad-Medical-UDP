package com.jimmytai.whizpad_medical_zenbo.demo.thread;

import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_zenbo.WhizPadClient;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadInfo;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadPairedInfo;
import com.jimmytai.whizpad_medical_zenbo.demo.activity.MainActivity;
import com.jimmytai.whizpad_medical_zenbo.demo.adapter.DeviceAdapter;
import com.jimmytai.whizpad_medical_zenbo.demo.utils.WifiUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;

/**
 * Created by JimmyTai on 2016/12/25.
 */

public class GetPairedInfoThread extends Thread {

    private static final String TAG = "GetPairedInfoThread";
    private static final boolean DEBUG = true;
    public static boolean isRunning = false;

    private MainActivity activity;
    private WhizPadClient client;
    private DeviceAdapter adapter;
    private WhizPadInfo info;

    public GetPairedInfoThread(MainActivity activity, WhizPadClient client, DeviceAdapter adapter, WhizPadInfo info) {
        this.activity = activity;
        this.client = client;
        this.adapter = adapter;
        this.info = info;
    }

    @Override
    public synchronized void start() {
        super.start();
        isRunning = true;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        WhizPadPairedInfo pairedInfo = null;
        int result = 0;
        try {
            socket = new DatagramSocket(info.getPort());
            socket.setBroadcast(false);
            byte[] cmd = client.getPairedInfoPacket(info.getDeviceId());
            DatagramPacket packet = new DatagramPacket(cmd, cmd.length, InetAddress.getByName(info.getIp()), info.getPort());
            socket.send(packet);
            byte[] buf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            socket.setSoTimeout(10 * 1000);
            socket.receive(receivePacket);
            JLog.d(DEBUG, TAG, "packet size: " + receivePacket.getLength() + ", packet: ");
            for (int i = 0; i < receivePacket.getLength(); i++) {
                JLog.d(DEBUG, TAG, String.valueOf(receivePacket.getData()[i]));
            }
            pairedInfo = client.parsePairedInfo(receivePacket.getData(), receivePacket.getLength());
            String localIpAddr = WifiUtils.getIPv4Str(activity);
            byte[] localMac = WifiUtils.getMacAddr();
            String localMacAddr = localMac == null ? "00:00:00:00:00:00" : String.format(Locale.getDefault(),
                    "%02X:%02X:%02X:%02X:%02X:%02X", localMac[0], localMac[1], localMac[2], localMac[3], localMac[4], localMac[5]);
            JLog.d(DEBUG, TAG, "response: " + pairedInfo.getResponse());
            if (pairedInfo.getResponse() == WhizPadPairedInfo.Response.Fail)
                result = 0x00;
            else if (pairedInfo.getResponse() == WhizPadPairedInfo.Response.Not_Paired)
                result = 0x03;
            else if (pairedInfo.getResponse() == WhizPadPairedInfo.Response.Paired)
                if (pairedInfo.getMac().equals(localMacAddr))
                    result = pairedInfo.getIp().equals(localIpAddr) ? 0x01 : 0x04;
                else
                    result = 0x02;
        } catch (Exception e) {
            e.printStackTrace();
            result = 0x00;
        } finally {
            if (socket != null)
                socket.close();
            adapter.getPairedInfoResult(result, pairedInfo, this.info);
            isRunning = false;
        }
    }
}
