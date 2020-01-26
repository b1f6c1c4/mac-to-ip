package com.github.b1f6c1c4.mac_to_ip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;

import org.apache.commons.net.util.SubnetUtils;

import static java.lang.Thread.*;

public class Scanner {

    private static ArrayList<InterfaceAddress> getMyAddresses() throws IOException {
        var res = new ArrayList<InterfaceAddress>();
        var interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            var i = interfaces.nextElement();
            if (i.isLoopback() || !i.isUp())
                continue;
            res.addAll(i.getInterfaceAddresses());
        }
        return res;
    }

    public static ArrayList<String> ParseLinux(String macPattern, InputStream stream) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        var res = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("?"))
                return null;
            if (line.matches(macPattern)) {
                var s = line.split(" ", 3)[1];
                res.add(s.substring(1, s.length() - 1));
            }
        }
        return res;
    }

    public static ArrayList<String> ParseWin(String macPattern, InputStream stream) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        var res = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            if (line.replaceAll("-", ":").matches(macPattern)) {
                var s = line.trim().split(" ", 2)[0];
                res.add(s);
            }
        }
        return res;
    }

    public static ArrayList<String> Scan(String macPattern, String ipPattern) throws IOException {
        var shouldSleep = false;
        for (var ia : getMyAddresses()) {
            if (!ia.getAddress().isSiteLocalAddress())
                continue;
            var ias = ia.getAddress().getHostAddress();
            if (ipPattern != null && !ias.matches(ipPattern))
                continue;
            var subnet = new SubnetUtils(ia.getAddress().getHostAddress() + "/" + ia.getNetworkPrefixLength());
            for (var as : subnet.getInfo().getAllAddresses()) {
                var a = InetAddress.getByName(as);
                shouldSleep |= !a.isReachable(1);
            }
        }

        if (shouldSleep) {
            try {
                sleep(500);
            } catch (InterruptedException ignored) {
            }
        }

        var pb = new ProcessBuilder("arp", "-na");
        var p = pb.start();
        var res = ParseLinux(macPattern, p.getInputStream());
        if (res != null)
            return res;

        pb = new ProcessBuilder("arp", "-a");
        p = pb.start();
        res = ParseWin(macPattern, p.getInputStream());
        return res;
    }
}
