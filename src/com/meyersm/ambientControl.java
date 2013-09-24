package com.meyersm;

import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.util.HashMap;


import de.jaetzold.philips.hue.*;


public class ambientControl {


    public int pixelScanSpeed = 1;
    public int resyncFequency = 100;

    protected Robot robotHelper;
    protected Rectangle screenRectangle;
    protected int screenWidth;
    protected int screenHeight;
    protected int runsSinceResync = 0;

    protected double scanSpan  = 0.8;
    protected double scanDepth = 0.3;
    protected HashMap <String,HashMap<Integer,Integer>> scanAreas;

    public ambientControl()
    {
          this.setup();
    }


    /**
     * eyeyr
     */
    public void scanAll()
    {
        runsSinceResync++;
        if (runsSinceResync >= resyncFequency)
            reSyncScreenSize();

        float[] left = scanSection(scanAreas.get("left"));
        float[] right = scanSection(scanAreas.get("right"));
        float[] top = scanSection(scanAreas.get("top"));
        float[] bottom = scanSection(scanAreas.get("bottom"));

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
        System.out.print(Math.round(r));
        System.out.print(", ");
        System.out.print(Math.round(g));
        System.out.print(", ");
        System.out.println(Math.round(b));
//You can multiply SAT or BRI by a digit to make it less saturated or bright
        float HUE= hsv[0] * 65535;
        float SAT= hsv[1] * 255;
        float BRI= hsv[2] * 255;

//Convert floats to integers
        String hue = String.valueOf(Math.round(HUE));
        String sat = String.valueOf(Math.round(SAT));
        String bri = String.valueOf(Math.round(BRI));




// print a message, this is just for testing purpose
            System.out.println(hue);
            System.out.println(sat);
            System.out.println(bri);
// create a process and execute cmdArray and currect environment
            //Process process = Runtime.getRuntime().exec(cmdArray);


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
