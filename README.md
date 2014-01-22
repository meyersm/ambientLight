ambientLight
============

Uses 3 Phillips Hue bulbs, positioned to the left, right, and behind a monitor to create ambient light that mirrors what is currently on the screen. Large chunks of the screen are scanned then the average color of pixels within those chunks are used to
set the color of the bulb, meaning the right light will match the color of screen near the right edge. The average color for the screen chunks is evaluated constantly and the lights will change as the screen changes.


[Example of ambientLight in action](sample/Sample-1.JPG)

Getting Started
---------------

First, find the IP address of your Phillips Hue bridge.

If you have an account on https://www.meethue.com/ you can go to My Settings >  My bridge > Show me more > Internal Ip Address

Edit the Main.java file with the correct ip address and a username you want to use. Then run the following code:
```java
ambientControl ac = new ambientControl();
ac.newLinkByIp(ip,username);
```
You will need to press the button on your hue bridge during this process

Next you can test your light configuration, turn all of the lights off, and run this code:
```java
ambientControl ac = new ambientControl();
ac.connectBridgeLink(username,ip);
ac.testLightConnection();
```
This will turn on lights one by one in order of Left, Top, Right, follow along with the console output to make sure they are in the right positions

Finally, you can run this code to start the ambient light scanning
```java
ambientControl ac = new ambientControl();
ac.connectBridgeLink(username,ip);
ac.startScanThread();
```

Playing Games With AmbientLight
------------------------

You can play games while ambientLight is running, but it will not work if the game is running in normal fullscreen mode, if available select a "Windowed fullscreen" or "Windowed borderless" display mode
