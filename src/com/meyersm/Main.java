package com.meyersm;

public class Main {

    public static void main(String[] args) {
        System.out.println("Setting up....");
        ambientControl ac = new ambientControl();
        ac.scanAll();
    }
}



