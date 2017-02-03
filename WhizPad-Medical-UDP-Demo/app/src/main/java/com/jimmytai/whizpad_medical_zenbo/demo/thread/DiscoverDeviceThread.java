package com.jimmytai.whizpad_medical_zenbo.demo.thread;

import com.jimmytai.library.whizpad_medical_zenbo.WhizPadClient;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadInfo;
import com.jimmytai.whizpad_medical_zenbo.demo.activity.MainActivity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by JimmyTai on 2016/10/19.
 */

public class DiscoverDeviceThread extends Thread {

    private static final String TAG = "DiscoverDeviceThread";
    private static final boolean DEBUG = false;
    public static boolean isRunning = false;

    private MainActivity activity;
    private WhizPadClient client;
    private DatagramSocket socket;

    public DiscoverDeviceThread(MainActivity activity, WhizPadClient client) {
        this.activity = activity;
        this.client = client;
    }

    @Override
    public synchronized void start() {
        super.start();
        isRunning = true;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(5591);
            socket.setBroadcast(true);
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while (isRunning) {
                socket.receive(packet);
                try {
                    WhizPadInfo info = client.parseWhizPadInfo(packet.getData(), packet.getLength());
                    activity.discoverDevice(info);
                } catch (WhizPadClient.PacketFormatException e) {
                    e.printStackTrace();
                }
            }
            isRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopListening() {
        if (socket != null) {
            socket.close();
        }
        isRunning = false;
    }
}
