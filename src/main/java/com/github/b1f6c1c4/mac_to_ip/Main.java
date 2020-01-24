package com.github.b1f6c1c4.mac_to_ip;

import java.io.IOException;

class Main {
    public static void main(String[] args) {
        var mac = args.length > 0 ? args[0] : null;
        var ip = args.length > 1 ? args[1] : null;
        try {
            var res = Scanner.Scan(mac, ip);
            for (var l : res) {
                System.out.println(l);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
