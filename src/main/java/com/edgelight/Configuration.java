package com.edgelight;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public class Configuration {
	
	// LEDs scheme:
	//
	// || 35.34...  ...4.3.2.1 <= data input
	// \/ ____________________
	// 36 |                  | 112 - last LED
	// 37 |     monitor      | ..
	// .. |    front side    | 93
	// 56 |__________________| 92
	// => 57.58.59 ...  .90.91 /\
	//                         ||

	public static final int VERT_LEDS = 21; // count of leds on horizonal sides of the display 
	public static final int HORZ_LEDS = 35; // count of leds on vertical sides of the display
	public static final int LEDS_COUNT = (VERT_LEDS + HORZ_LEDS)*2;
	
	public static final double CHANGE_SPEED = 1; // (0..1], defines the speed of color & brightness change on Java side
	
	public static final double MONITOR_LOW_BRIGHTNESS = 0; // when monitor brightness is below this value, Edgelight turns off
	public static final double MONITOR_HIGH_BRIGHTNESS = 55; // when monitor brightness is above this value, Edgelight uses max brightness
	
	// Use your own values here. Top side is most visible so it's brightness is reduced, bottom side is least visible so it is toned up
	public static final double BRIGHTNESS_CORRECTION_TOP = 0.65;
	public static final double BRIGHTNESS_CORRECTION_LEFT = 1.0;
	public static final double BRIGHTNESS_CORRECTION_RIGHT = 1.0;
	public static final double BRIGHTNESS_CORRECTION_BOTTOM = 1.3;
	
	// Color correction for WS2812B, use your values here to match your background and monitor settings
	public static final double COLOR_CORRECTION_RED = 1;
	public static final double COLOR_CORRECTION_GREEN = 0.75;
	public static final double COLOR_CORRECTION_BLUE = 0.45;
	
	public static final String PREFERRED_PORT = null; // required only for SerialLedConnector, null means use first available
	public static final String IP_ADDRESS = "192.168.1.50"; // required only for NetworkLedConnector
	
	public static final KeyStroke HOTKEY_CHANGE_MODE = KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.META_MASK);
	public static final KeyStroke HOTKEY_BRIGHTNESS_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.META_MASK);
	public static final KeyStroke HOTKEY_BRIGHTNESS_UP = KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.META_MASK);
	
	public static final int LUMINANCE_STEP = 5; // Brightness step when changing brightness 
	
}
