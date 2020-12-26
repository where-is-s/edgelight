# Edgelight
Ambilight-like experience for lighting up wall behind your monitor based on screen content.

[![](https://img.youtube.com/vi/4WdhOpwml-g/0.jpg)](https://www.youtube.com/watch?v=4WdhOpwml-g "Click to play on YouTube")

## Solution

My solution works on software level. Screen is captured by a small Java app which hides in a tray and runs in background. LED color & brightness configuration is calculated based on screen data. When calculated, it is sent through USB cable to the microcontroller. Microcontroller sends data directly to LED strip.
Pros, compared to other DYI solutions out there:
+ Very easy to make as home project
+ Very low cost (see below)
+ Not limited to a certain output connection type. HDMI, DP, VGA, anything will work since data is calculated on PC and passed to microcontroller through USB cable.

Cons:
- Works only with a PC
- Puts slight strain on CPU (see below)
- LED response time is acceptable but not great

Response time by the way is limited by screen capture API. On Windows the API limits requests to monitor refresh rate, so if you have 60Hz monitor, you'll have around 60 fps. Moreover, this rate must be divided by 4 because optimizations I made capture one screen border at a time. Currently the solution gives around 15 full content refreshes per second on a 60Hz monitor. This can seem low but LED states are interpolated between data refreshes so it actually looks smooth and not too laggy. LEDs are updated at about 100Hz so transitions are very smooth.
It would be good to improve screenshot rate but I didn't find a way to do that. If you have an idea, please let me know.

## What do I need to do the same?

In order to set up Edgelight, you'll need:
1) Arduino-compatible board connected to USB in serial mode. I used Arduino Nano-compatible Chinese board with CH340/ATmega328P and then switched to ESP8266. You can still go with ATmega328P but it will require some tinkering with the code in order to optimize few things.
For my setup I bought this one: https://ru.aliexpress.com/item/D1-mini-Mini-NodeMcu-4M-bytes-Lua-WIFI-Internet-of-Things-development-board-based-ESP8266-by/32662942091.html?spm=a2g0s.9042311.0.0.50af33eda0EGJ1

2) LED-strip consisting of WS2812B or similar LEDs connected to Arduino board.
Strip I used: https://www.aliexpress.com/item/DC5V-1m-4m-5m-WS2812B-Smart-led-pixel-strip-Black-White-PCB-30-60-144-leds/32337440906.html. I bought 4m 60 IP65 and used only 2 meters of it, you can just buy 2 meters and solder them together. You can choose 30 LEDs/m strip and it will work fine as well, only the max brightness will be lower. IP65 is not required of course, IP30 will be just fine.

3) LED-strip should be powered by a separate PSU adapter. It is not completely necessary but it's strongly recommended if you want to drive more than 10-20 LEDs with good brightness. Just for testing you can set very low brightness and skip on the PSU. In general 14 W/meter is recommended for a 60 LEDs/m strip. It makes 28 W of recommended power for my setup which has almost 2 meters of LED strip.
PSU I used: https://www.aliexpress.com/item/LED-power-supply-adapter-AC100-240V-to-DC-5V-12V-24V-to-1A-2A-3A-4A/32842360990.html (5V 6A version)

3) Windows (preferred) or Mac OS.
4) Arduino IDE, Java.

Total hardware cost was about $35 but only 2 meters of 4m LED strip were used. If you're really into saving you can get a similar setup for $20.

## I got everything, how do I make it work?

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
* If your display connection supports DDC/CI, the Java code will pick it up and allow simultaneous brightness change for LED strip and the monitor using Meta+F3 / Meta+F4 hotkeys. For me DDC/CI doesn't work with DisplayPort but works great with HDMI.
* Meta+F2 hotkey changes modes between Dynamic / Static / Christmas celebration / Off. Dynamic is a standard mode which uses screen content to determine LED brightness and color, Static just lights LEDs with a fixed brightness and color, Off turns LEDs off. Christmas celebration starts a loop of changing and moving colors. Very pleasant and soothing for your best Christmas experience.
