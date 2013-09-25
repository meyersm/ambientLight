package com.meyersm;

public class Main {

    public static void main(String[] args) {
        System.out.println("Setting up....");
        ambientControl ac = new ambientControl();
        ac.connectBridgeLink("ambientControlUsername532","192.168.1.129");
        //ac.testLightConnection();
        ac.startScanThread();
    }
}



