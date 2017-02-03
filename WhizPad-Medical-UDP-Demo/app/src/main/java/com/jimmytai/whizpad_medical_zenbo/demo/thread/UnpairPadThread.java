package com.jimmytai.whizpad_medical_zenbo.demo.thread;

import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_zenbo.WhizPadClient;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadInfo;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadUnPairingAck;
import com.jimmytai.whizpad_medical_zenbo.demo.activity.PadStatusActivity;
import com.jimmytai.whizpad_medical_zenbo.demo.utils.WifiUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by JimmyTai on 2016/12/25.
 */

public class UnpairPadThread extends Thread {

    private static final String TAG = "UnpairPadThread";
    private static final boolean DEBUG = true;
    public static boolean isRunning = false;

    private PadStatusActivity activity;
    private WhizPadClient client;
    private WhizPadInfo info;
    private String password;

    public UnpairPadThread(PadStatusActivity activity, WhizPadInfo info, String password) {
        this.activity = activity;
        this.client = new WhizPadClient(activity);
        this.info = info;
        this.password = password;
    }

    @Override
    public synchronized void start() {
        super.start();
        isRunning = true;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        WhizPadUnPairingAck.Response response = WhizPadUnPairingAck.Response.FAIL;
        try {
            socket = new DatagramSocket(info.getPort());
            socket.setBroadcast(false);
            byte[] cmd = client.getUnpairPacket(info.getDeviceId(), password, WifiUtils.getMacAddr());
            DatagramPacket packet = new DatagramPacket(cmd, cmd.length, InetAddress.getByName(info.getIp()), info.getPort());
            socket.send(packet);
            byte[] buf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            socket.setSoTimeout(10 * 1000);
            socket.receive(receivePacket);
            JLog.d(DEBUG, TAG, "receive unpaired response ->");
            for (int i = 0; i < receivePacket.getLength(); i++) {
                JLog.d(DEBUG, TAG, String.valueOf(receivePacket.getData()[i]));
            }
            WhizPadUnPairingAck ack = client.parseUnpairingResponse(receivePacket.getData(), receivePacket.getLength());
            if (ack.getDeviceId().equals(info.getDeviceId())) {
                response = ack.getResponse();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
            activity.unPairPadResult(response);
            isRunning = false;
        }
    }
}
