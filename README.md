# Edgelight
Ambilight-like experience for lighting up wall behind your monitor based on screen content.

[![](https://img.youtube.com/vi/4WdhOpwml-g/0.jpg)](https://www.youtube.com/watch?v=4WdhOpwml-g "Click to play on YouTube")

In order to set up Edgelight, you'll need:
1) Arduino-compatible board connected to USB in serial mode. I used Arduino Nano-compatible Chinese board with CH340/ATmega328P.
It is very limited in resources but still enough to drive LED strip with a good quality.
2) LED-strip consisting of WS2812B or similar LEDs connected to Arduino board and powered by a separate PSU adapter.
Adapter is not really necessary but strongly recommended if you want to drive more than 10-20 LEDs with high maximum brightness.
I used 30W PSU for 112 WS2812B LEDs, it works great.
3) Windows (preferred) or Mac OS.
4) Arduino IDE, Java.

Instructions:
1) Set up your hardware. Attach LED strip to the back of your monitor. I used following layout for LEDs:
	
```
 || 35.34...  ...4.3.2.1 <= first LED connected to data output of the microcontroller
 \/ ____________________
 36 |                  | 112 - last LED
 37 |     monitor      | ..
 .. |    front side    | 93
 56 |__________________| 92
 => 57.58.59 ...  .90.91 /\
                         ||
```
                         
In my case LEDs start at upper right corner of the screen and go counter-clockwise. In total there are 112 of them.

2) If you have another LED amount or layout, you need to change configurations and maybe update the code.
Refer to src/main/java/com/edgelight/Configuration.java and arduino/arduino.ino files for configuration.

3) Connect Arduino board to your PC, open arduino folder in Arduino IDE and install the code into the microcontroller.

4) Run the Java app (Launcher.java). It will automatically detect COM-port of your microcontroller and start updating the LEDs.

5) Voila, the strip works!

Features:
* Code is mostly cross-platform. Windows works really well, utilizing only 15-20% of a single CPU core (Core i7 4750HQ). It is pretty resource-consuming on Mac OS, taking up to a full core on the same CPU.
* There is NetworkLedController which allows to connect to LED strip using UDP. This is a solution for case if you want to go wireless and use a controller or micro PC board with integrated Wi-Fi. However remember that it will work only when you're in the same network, also signal will need to be strong. This was the first solution I implemented, but I was not happy with the result so I switched to serial connection which works WAY better.
* If your display connection supports DDC/CI, the Java code will pick it up and allow simultaneous brightness change for LED strip and the monitor using Meta+F3 / Meta+F4 hotkeys.
* Meta+F2 hotkey changes modes between Dynamic / Static / Off. Dynamic is a standard mode which uses screen content to determine LED brightness and color, Static just lights LEDs with a fixed brightness and color, Off turns LEDs off.
