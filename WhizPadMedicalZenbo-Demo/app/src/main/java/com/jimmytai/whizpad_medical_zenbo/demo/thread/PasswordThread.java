package com.jimmytai.whizpad_medical_zenbo.demo.thread;

import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_zenbo.WhizPadClient;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadInfo;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadPairingAck;
import com.jimmytai.whizpad_medical_zenbo.demo.activity.MainActivity;
import com.jimmytai.whizpad_medical_zenbo.demo.utils.WifiUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by JimmyTai on 2016/10/19.
 */

public class PasswordThread extends Thread {

    private static final String TAG = "PasswordThread";
    private static final boolean DEBUG = true;

    private MainActivity activity;
    private WhizPadClient client;
    private WhizPadInfo info;
    private String password;

    public PasswordThread(MainActivity activity, WhizPadClient client, WhizPadInfo info, String password) {
        this.activity = activity;
        this.client = client;
        this.info = info;
        this.password = password;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        int result = 0;
        try {
            socket = new DatagramSocket(info.getPort());
            socket.setBroadcast(false);
            byte[] cmd = client.getPairPacket(info.getDeviceId(), password, WifiUtils.getMacAddr(), WifiUtils.getIPv4(activity));
            DatagramPacket packet = new DatagramPacket(cmd, cmd.length, InetAddress.getByName(info.getIp()), info.getPort());
            socket.send(packet);
            byte[] buf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            socket.setSoTimeout(10 * 1000);
            socket.receive(receivePacket);
            JLog.d(DEBUG, TAG, "password data -> ");
            for (int i = 0; i < receivePacket.getLength(); i++) {
                JLog.d(DEBUG, TAG, String.valueOf(receivePacket.getData()[i]));
            }
            WhizPadPairingAck ack = client.parsePairResponse(receivePacket.getData(), receivePacket.getLength());
            if (ack.getResponse() == WhizPadPairingAck.Response.FAIL)
                result = 0x00;
            else if (ack.getResponse() == WhizPadPairingAck.Response.FAIL_INCORRECT_PASSWORD)
                result = 0x02;
            else if (ack.getResponse() == WhizPadPairingAck.Response.SUCCESS)
                result = 0x01;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
            activity.passwordResult(result, info);
        }
    }
}