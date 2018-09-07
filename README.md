# Edgelight
Ambilight-like experience for lighting up wall behind your monitor based on screen content.

[![](https://img.youtube.com/vi/4WdhOpwml-g/0.jpg)](https://www.youtube.com/watch?v=4WdhOpwml-g "Click to play on YouTube")

In order to set up Edgelight, you'll need:
1) Arduino-compatible board connected to USB in serial mode. I used Arduino Nano-compatible Chinese board with CH340/ATmega328P.
It is very limited in resources but still enough to drive LED strip with a good quality.
For my setup I bought this one: https://www.aliexpress.com/item/Nano-CH340-ATmega328P-MicroUSB-Compatible-for-Arduino-Nano-V3/32572612009.html

2) LED-strip consisting of WS2812B or similar LEDs connected to Arduino board.
Strip I used: https://www.aliexpress.com/item/DC5V-1m-4m-5m-WS2812B-Smart-led-pixel-strip-Black-White-PCB-30-60-144-leds/32337440906.html. I bought 4m 60 IP65 and used only 2 meters of it, you can just buy 2 meters and solder them together. You can choose 30 LEDs/m strip and it will work fine as well, only the max brightness will be lower.

3) LED-strip should better be powered by a separate PSU adapter. It is not completely necessary but it's strongly recommended if you want to drive more than 10-20 LEDs with good brightness. Just for testing you can set very low brightness and skip on the PSU. In general 14 W/meter is recommended for a 60 LEDs/m strip. It makes 28 W of recommended power for my setup.
PSU I used: https://www.aliexpress.com/item/LED-power-supply-adapter-AC100-240V-to-DC-5V-12V-24V-to-1A-2A-3A-4A/32842360990.html (5V 6A version)

3) Windows (preferred) or Mac OS.
4) Arduino IDE, Java.

Total hardware cost was about $35 but only 2 meters of 4m LED strip were used. If you're really into saving you can get a similar setup for $20.

Instructions:
1) Connect your hardware. Attach LED strip to the back of your monitor. I used following layout for LEDs:
	
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
