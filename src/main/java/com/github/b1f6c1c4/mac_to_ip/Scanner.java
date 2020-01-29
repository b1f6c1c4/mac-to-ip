package com.github.b1f6c1c4.mac_to_ip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;

import org.apache.commons.net.util.SubnetUtils;

import static java.lang.Thread.*;

interface IScanner {
    void Scan(String ipPattern) throws IOException;

    ArrayList<String> Parse(String macPattern) throws IOException;
}

abstract class BaseScanner implements IScanner {
    protected abstract void Ping(InetAddress target) throws IOException;

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

    public void Scan(String ipPattern) throws IOException {
        for (var ia : getMyAddresses()) {
            if (!ia.getAddress().isSiteLocalAddress())
                continue;
            var ias = ia.getAddress().getHostAddress();
            if (ipPattern != null && !ias.matches(ipPattern))
                continue;
            var subnet = new SubnetUtils(ia.getAddress().getHostAddress() + "/" + ia.getNetworkPrefixLength());
            for (var as : subnet.getInfo().getAllAddresses()) {
                var a = InetAddress.getByName(as);
                Ping(a);
            }
        }
    }
}

class ScannerLinux extends BaseScanner {
    protected void Ping(InetAddress target) throws IOException {
        target.isReachable(1);
    }

    public ArrayList<String> Parse(String macPattern) throws IOException {
        var pb = new ProcessBuilder("arp", "-na");
        var p = pb.start();
        var reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        var res = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("?"))
                return null;
            var s = line.split(" ");
            if (s[3].matches(macPattern)) {
                res.add(s[1].substring(1, s[1].length() - 1));
            }
        }
        return res;
    }
}

class ScannerWin extends BaseScanner {
    protected void Ping(InetAddress target) throws IOException {
        var pb = new ProcessBuilder("ping", "-w", "1", "-n", "1", target.getHostAddress());
        pb.start();
    }

    public ArrayList<String> Parse(String macPattern) throws IOException {
        var pb = new ProcessBuilder("arp", "-a");
        var p = pb.start();
        var reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        var res = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("  "))
                continue;
            if (line.matches(".*Internet Address.*"))
                continue;
            var s = line.replaceAll("-", ":").split(" ");
            if (s[1].matches(macPattern)) {
                res.add(s[0]);
            }
        }
        return res;
    }
}
