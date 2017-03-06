package com.jimmytai.whizpad_medical_zenbo.demo.utils;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.math.BigInteger;
import java.net.NetworkInterface;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by JimmyTai on 2016/12/27.
 */

public class WifiUtils {

    public static byte[] getIPv4(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
            ip = Integer.reverseBytes(ip);
        return BigInteger.valueOf(ip).toByteArray();
    }

    public static String getIPv4Str(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
            ip = Integer.reverseBytes(ip);
        byte[] byteIp = BigInteger.valueOf(ip).toByteArray();
        return String.format(Locale.getDefault(), "%d.%d.%d.%d", (256 + byteIp[0]) % 256, (256 + byteIp[1]) % 256, (256 + byteIp[2]) %
                256, (256 + byteIp[3]) % 256);
    }

    public static byte[] getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                return nif.getHardwareAddress();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
