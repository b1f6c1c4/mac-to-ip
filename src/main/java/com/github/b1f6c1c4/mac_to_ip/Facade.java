package com.github.b1f6c1c4.mac_to_ip;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.SystemUtils;

import static java.lang.Thread.sleep;

public class Facade {
    public static ArrayList<String> Execute(String macPattern, String ipPattern) {
        IScanner scanner;
        if (SystemUtils.IS_OS_WINDOWS)
            scanner = new ScannerWin();
        else
            scanner = new ScannerLinux();

        try {
            scanner.Scan(ipPattern);
            try {
                sleep(500);
            } catch (InterruptedException ignored) {
            }
            return scanner.Parse(macPattern);
        } catch (IOException e) {
            return null;
        }
    }
}
