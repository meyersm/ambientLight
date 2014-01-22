package com.meyersm;

import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


import de.jaetzold.philips.hue.*;


public class ambientControl {


    public int pixelScanSpeed = 1;
    public int resyncFequency = 100;

    public HueBridge bridge = null;


    public int leftLightId = 2;
    public int topLightId = 1;
    public int rightLightId = 3;
    public int bottomLightId = -1;

    protected Robot robotHelper;
    protected Rectangle screenRectangle;
    protected int screenWidth;
    protected int screenHeight;
    protected int runsSinceResync = 0;

    protected Thread scanThread;

    protected double scanSpan  = 0.8;
    protected double scanDepth = 0.3;
    protected HashMap <String,HashMap<Integer,Integer>> scanAreas;




    public ambientControl()
    {
          this.setup();
    }


    /**
     *
     */
    public void scanAllOnce()
    {
        runsSinceResync++;
        if (runsSinceResync >= resyncFequency)
            reSyncScreenSize();

        float[] left = scanSection(scanAreas.get("left"));
        float[] right = scanSection(scanAreas.get("right"));
        float[] top = scanSection(scanAreas.get("top"));
        float[] bottom = scanSection(scanAreas.get("bottom"));

        updateAllLights(left,right,top,bottom);

    }

    public void startScanThread()
    {
        scanThread = new Thread()
        {
            public void run() {
                System.out.println("Starting scan thread");

                while(true)
                {
                    scanAllOnce();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
        };
        scanThread.start();
    }

    public void stopScanThread()
    {
        scanThread.stop();
    }



    public String newLink()
    {
        return newLink(0);
    }

    public String newLinkByIp(String ipAddress,String username)
    {
        try{bridge = new HueBridge(InetAddress.getByName(ipAddress),username);}catch (UnknownHostException e) {e.printStackTrace();}
        System.out.println("Establishing new link to hue bridge......\nPress button on bridge");
        bridge.authenticate(true);
        username = bridge.getUsername();
        System.out.println("Finished: " +  username);
        return username;
    }

    public String newLink(int bridgeIndex)
    {

        List<HueBridge> bridges = HueBridge.discover();
        if (bridges.size() == 0)
        {
            System.out.println("NO BRIDGES FOUND!");
            return "";
        }
        bridge = bridges.get(bridgeIndex);
        System.out.println("Establishing new link to hue bridge......\nPress button on bridge");
        bridge.authenticate(true);
        String username = bridge.getUsername();
        System.out.println("Finished: " +  username);
        return username;
    }

    public Boolean connectBridgeLink(String username,int bridgeIndex)
    {
        List<HueBridge> bridges = HueBridge.discover();
        bridge = bridges.get(bridgeIndex);
        return bridge.authenticate(username, true);
    }

    public Boolean connectBridgeLink(String username,String ipAddress)
    {
        try{bridge = new HueBridge(InetAddress.getByName(ipAddress),username);}catch (UnknownHostException e) {e.printStackTrace();}
        return bridge.authenticate(username, true);
    }

    public void testLightConnection()
    {
        if (bridge == null)
        {
            System.out.println("No active bridge, cannot test lights");
            return;
        }
        Collection<? extends HueLightBulb> lights = bridge.getLights();
        System.out.println("Found " + lights.size() + " lights:");
        for(final HueLightBulb lighttmp : lights) {
            System.out.println(lighttmp);
        }
        HueLightBulb light;

        System.out.println("Testing LEFT light now");
        light = bridge.getLight(leftLightId);
        light.setOn(true);
        try {Thread.sleep(1500);} catch (InterruptedException e) {}
        light.setOn(false);

        System.out.println("Testing TOP light now");
        light = bridge.getLight(topLightId);
        light.setOn(true);
        try {Thread.sleep(1500);} catch (InterruptedException e) {}
        light.setOn(false);

        System.out.println("Testing RIGHT light now");
        light = bridge.getLight(rightLightId);
        light.setOn(true);
        try {Thread.sleep(1500);} catch (InterruptedException e) {}
        light.setOn(false);

        if (bottomLightId != -1)
        {
            System.out.println("Testing BOTTOM light now");
            light = bridge.getLight(bottomLightId);
            light.setOn(true);
            try {Thread.sleep(1500);} catch (InterruptedException e) {}
            light.setOn(false);
        }


    }

    public void updateAllLights(float[] left,float[] right, float[] top, float[] bottom)
    {
        updateLight(leftLightId,left);
        updateLight(rightLightId,right);
        updateLight(topLightId,top);
        updateLight(bottomLightId,bottom);
    }

    public void updateLight(int lightIndex,float[] hsv)
    {
        if (lightIndex == -1)
            return;

        if (bridge == null)
        {
            System.out.println("No active bridge, cannot test lights");
            return;
        }
        float HUE= hsv[0] * 65535;
        float SAT= hsv[1] * 255;
        float BRI= hsv[2] * 255;

        HueLightBulb light = bridge.getLight(lightIndex);
        light.setOn(true);
        try{
            light.setBrightness(Math.round(BRI));
        }catch (IllegalArgumentException e){
            System.out.println("Illegal argument exception:" + Math.round(BRI));
        }
        try{
            light.setSaturation(Math.round(SAT));
        }catch (IllegalArgumentException e){
            System.out.println("Illegal argument exception:" + Math.round(BRI));
        }
        try{
            light.setHue(Math.round(HUE));
        }catch (IllegalArgumentException e){
            System.out.println("Illegal argument exception:" + Math.round(BRI));
        }

    }

    public float[] scanSection(HashMap<Integer,Integer> coords)
    {
        return scanSection(coords.get(0),coords.get(1),coords.get(2),coords.get(3));
    }


    public float[] scanSection(int startX, int startY, int width, int height)
    {
        int pixel; //ARGB variable with 32 int bytes where
//sets of 8 bytes are: Alpha, Red, Green, Blue
        float r=0;
        float g=0;
        float b=0;



//get screenshot into object "screenshot" of class BufferedImage
        Rectangle area = new Rectangle(startX,startY,width,height);
        BufferedImage screenshot = this.robotHelper.createScreenCapture(area);


        int i=0;
        int j=0;
//I skip every alternate pixel making my program 4 times faster
        for(i=0; i<width; i=i+pixelScanSpeed){
            for(j=0; j<height; j=j+pixelScanSpeed){
                pixel = screenshot.getRGB(i,j); //the ARGB integer has the colors of pixel (i,j)
                r = r+(int)(255&(pixel>>16)); //add up reds
                g = g+(int)(255&(pixel>>8)); //add up greens
                b = b+(int)(255&(pixel)); //add up blues
            }
        }
        int aX = width/pixelScanSpeed;
        int aY = height/pixelScanSpeed;
        r=r/(aX*aY); //average red
        g=g/(aX*aY); //average green
        b=b/(aX*aY); //average blue

//println(r+","+g+","+b);

// filter values to increase saturation
        float maxColorInt;
        float minColorInt;

        maxColorInt = Math.max(r,g);
        maxColorInt = Math.max(maxColorInt,b);
        if(maxColorInt == r){
            // red
            if(maxColorInt < (225-20)){
                r = maxColorInt + 20;
            }
        }
        else if (maxColorInt == g){
            //green
            if(maxColorInt < (225-20)){
                g = maxColorInt + 20;
            }
        }
        else {
            //blue
            if(maxColorInt < (225-20)){
                b = maxColorInt + 20;
            }
        }

//minimise smallest
        minColorInt = Math.min(r, g);
        minColorInt = Math.min(minColorInt,b);
        if(minColorInt == r){
            // red
            if(minColorInt > 20){
                r = minColorInt - 20;
            }
        }
        else if (minColorInt == g){
            //green
            if(minColorInt > 20){
                g = minColorInt - 20;
            }
        }
        else {
            //blue
            if(minColorInt > 20){
                b = minColorInt - 20;
            }
        }

//Convert RGB values to HSV(Hue Saturation and Brightness)
        float[] hsv = new float[3];
        Color.RGBtoHSB(Math.round(r),Math.round(g),Math.round(b),hsv);
//You can multiply SAT or BRI by a digit to make it less saturated or bright
        float HUE= hsv[0] * 65535;
        float SAT= hsv[1] * 255;
        float BRI= hsv[2] * 255;
//Convert floats to integers
        String hue = String.valueOf(Math.round(HUE));
        String sat = String.valueOf(Math.round(SAT));
        String bri = String.valueOf(Math.round(BRI));
        return hsv;


    }


    protected void setup()
    {
        try //standard Robot class error check
        {
            robotHelper = new Robot();
        }
        catch (AWTException e)
        {
            System.out.println("Robot class not supported by your system!");
        }
        scanAreas = new HashMap<>();
        reSyncScreenSize();
    }

    protected void reSyncScreenSize()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int tempWidth = (int)screenSize.getWidth();
        int tempHeight= (int)screenSize.getHeight();
        if ((Integer.compare(screenWidth,tempWidth) == 0) && (Integer.compare(screenHeight,tempHeight) == 0))
            return;
        screenWidth = tempWidth;
        screenHeight = tempHeight;
        screenRectangle = new Rectangle(screenSize);


        int sideChunkWidth   = (int)Math.round(screenWidth *  scanDepth);
        int sideChunkHeight  = (int)Math.round(screenHeight * scanSpan);
        int sideIndent       = (int)Math.round(screenHeight * (1.0 - scanSpan));

        int middleChunkWidth = (int)Math.round(screenWidth *  scanSpan);
        int middleChunkHeight= (int)Math.round(screenHeight * scanDepth);
        int middleIndent     = (int)Math.round(screenWidth * (1.0 - scanSpan));

        scanAreas.clear();

        HashMap<Integer,Integer> left = new HashMap<>();
        HashMap<Integer,Integer> right = new HashMap<>();
        HashMap<Integer,Integer> top = new HashMap<>();
        HashMap<Integer,Integer> bottom = new HashMap<>();
        left.put(0,0);
        left.put(1,sideIndent);
        left.put(2,sideChunkWidth);
        left.put(3,sideChunkHeight);
        scanAreas.put("left",left);

        right.put(0,screenWidth - sideChunkWidth);
        right.put(1,sideIndent);
        right.put(2,sideChunkWidth);
        right.put(3,sideChunkHeight);
        scanAreas.put("right",right);

        top.put(0,middleIndent);
        top.put(1,0);
        top.put(2,middleChunkWidth);
        top.put(3,middleChunkHeight);
        scanAreas.put("top",top);

        bottom.put(0,middleIndent);
        bottom.put(1,screenHeight - middleChunkHeight);
        bottom.put(2,middleChunkWidth);
        bottom.put(3,middleChunkHeight);
        scanAreas.put("bottom",bottom);



        runsSinceResync = 0;
    }

}
