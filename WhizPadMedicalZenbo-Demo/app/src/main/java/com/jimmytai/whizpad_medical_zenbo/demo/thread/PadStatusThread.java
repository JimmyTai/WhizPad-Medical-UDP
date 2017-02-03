package com.jimmytai.whizpad_medical_zenbo.demo.thread;

import com.jimmytai.library.utils.lifecycle.JLifecycle;
import com.jimmytai.library.utils.log.JLog;
import com.jimmytai.library.whizpad_medical_zenbo.WhizPadClient;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadEvent;
import com.jimmytai.library.whizpad_medical_zenbo.item.WhizPadInfo;
import com.jimmytai.whizpad_medical_zenbo.demo.activity.PadStatusActivity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by JimmyTai on 2016/10/19.
 */

public class PadStatusThread extends Thread {

    private static final String TAG = "PadStatusThread";
    private static final boolean DEBUG = true;
    public static boolean isRunning = false;

    private PadStatusActivity activity;
    private WhizPadClient client;
    private WhizPadInfo info;
    private DatagramSocket socket;

    public PadStatusThread(PadStatusActivity activity, WhizPadInfo info) {
        this.activity = activity;
        this.client = new WhizPadClient(activity);
        this.info = info;
    }

    @Override
    public synchronized void start() {
        super.start();
        isRunning = true;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(info.getPort());
            byte[] cmd = client.getWhizPadLatestEventPacket(info.getDeviceId());
            DatagramPacket getStatusPacket = new DatagramPacket(cmd, cmd.length, InetAddress.getByName(info.getIp()), info.getPort());
            socket.send(getStatusPacket);
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            JLog.d(DEBUG, TAG, "start Listening -> port: " + info.getPort());
            while (isRunning) {
                socket.receive(packet);
                JLog.d(DEBUG, TAG, "packet length: " + packet.getLength());
                for (int i = 0; i < packet.getLength(); i++) {
                    JLog.d(DEBUG, TAG, String.valueOf(packet.getData()[i] < 0 ? packet.getData()[i] + 256 : packet.getData()[i]));
                }
                try {
                    WhizPadEvent event = client.parseWhizPadEvent(packet.getData(), packet.getLength());
                    DatagramPacket ackPacket = new DatagramPacket(event.getAck(), event.getAck().length, packet.getAddress(), packet
                            .getPort());
                    socket.send(ackPacket);
                    if (event.getDeviceId().equals(info.getDeviceId()) && activity.jLifeCycle != JLifecycle.ON_DESTROY)
                        activity.updatePadStatus(event.getStatus());
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
        JLog.d(DEBUG, TAG, "close socket");
        if (socket != null) {
            socket.close();
            JLog.d(DEBUG, TAG, "close");
        }
        isRunning = false;
    }
}
