/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dist_igra;

//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.util.Collections;

public class Symbols {
    public static final int Infinity = -1;
    // internet related

    public static final int ServerPort = 7033;
    public static final int coordinator = 0;
    // time bounds on messages for synchronous algorithms
    public static final int roundTime = 500; // ms
    public static final boolean debugFlag = true;

    public static final String nameServer = "192.168.100.42";

    @SuppressWarnings("unused")
    private static String resolveServer() {
        // 1. JVM property: java -Dserver=192.168.100.42 ...
        String prop = System.getProperty("server");
        if (prop != null && !prop.isBlank()) return prop;

        // 2. Environment variable: SERVER_HOST=192.168.100.42
        String env = System.getenv("SERVER_HOST");
        if (env != null && !env.isBlank()) return env;

        // 3. Default: same machine
        return "localhost";
    }
}
