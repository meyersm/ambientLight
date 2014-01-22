package com.meyersm;

public class Main {

    public static void main(String[] args) {
        System.out.println("Setting up....");

        String ip = "192.168.1.122";
        String username = "ambientControlUsername532";

        ambientControl ac = new ambientControl();

        //Step 1 - Create a new Link
        //ac.newLinkByIp(ip,username);

        //Ready To run
        ac.connectBridgeLink(username,ip);

        //ac.testLightConnection();  //Test lights
        ac.startScanThread();        //Start scanning
    }
}



