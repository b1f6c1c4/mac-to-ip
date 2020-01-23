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

public class Main {

    public static ArrayList<InterfaceAddress> getMyAddresses() throws IOException {
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

    public static boolean ping(String ipAddress) throws IOException {
        var geek = InetAddress.getByName(ipAddress);
        return geek.isReachable(20);
    }

    public static void main(String[] args) throws IOException {
        for (var ia : getMyAddresses()) {
            if (!ia.getAddress().isSiteLocalAddress())
                continue;
            var ias = ia.getAddress().getHostAddress();
            if (args.length >= 2 && !ias.matches(args[1]))
                continue;
            System.err.println("Scanning " + ias);
            var subnet = new SubnetUtils(ia.getAddress().getHostAddress() + "/" + ia.getNetworkPrefixLength());
            for (var as : subnet.getInfo().getAllAddresses()) {
                var a = InetAddress.getByName(as);
                a.isReachable(1);
            }
        }

//        System.err.println("Sleeping...");
//        try {
//            sleep(500);
//        } catch (InterruptedException ignored) {
//        }

        var pb = new ProcessBuilder("arp", "-na");
        var p = pb.start();
        var reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.matches(args[0])) {
                System.out.println(line);
            }
        }
    }
}
