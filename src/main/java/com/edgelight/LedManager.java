package com.edgelight;

import static com.edgelight.Configuration.BRIGHTNESS_CORRECTION_BOTTOM;
import static com.edgelight.Configuration.BRIGHTNESS_CORRECTION_LEFT;
import static com.edgelight.Configuration.BRIGHTNESS_CORRECTION_RIGHT;
import static com.edgelight.Configuration.BRIGHTNESS_CORRECTION_TOP;
import static com.edgelight.Configuration.CHANGE_SPEED;
import static com.edgelight.Configuration.COLOR_CORRECTION_BLUE;
import static com.edgelight.Configuration.COLOR_CORRECTION_GREEN;
import static com.edgelight.Configuration.COLOR_CORRECTION_RED;
import static com.edgelight.Configuration.HORZ_LEDS;
import static com.edgelight.Configuration.LEDS_COUNT;
import static com.edgelight.Configuration.MONITOR_HIGH_BRIGHTNESS;
import static com.edgelight.Configuration.MONITOR_LOW_BRIGHTNESS;
import static com.edgelight.Configuration.VERT_LEDS;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.edgelight.common.RGB;
import com.edgelight.ledconnector.LedConnector;

public class LedManager {

//	private static Logger logger = LoggerFactory.getLogger(LedManager.class);

	public static enum MODE {
		DYNAMIC, STATIC, OFF
	}

	private Robot robot;
	private LedConnector ledConnector;
	private List<RGB> targetLeds = new ArrayList<>();
	private List<RGB> curLeds = new ArrayList<>();
	private MODE mode = MODE.DYNAMIC;
	private int brightness;

	private class ScreenCapturer extends Thread {
		private int updateTargetLeds(int offset, List<RGB> rgbs) {
			synchronized (targetLeds) {
				for (int i = 0; i < rgbs.size(); ++i) {
					targetLeds.set(offset++, rgbs.get(i));
				}
				targetLeds.notifyAll();
			}
			return offset;
		}

		public void run() {
			while (!isInterrupted()) {
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				int captureOffset = 50;
				int captureWidth = 1;
				int perimeter = screenSize.width * 2 + screenSize.height * 2;
				BufferedImage top = getCapture(new Rectangle(0, captureOffset, screenSize.width, captureWidth));
				BufferedImage left = getCapture(new Rectangle(captureOffset, 0, captureWidth, screenSize.height));
				BufferedImage bottom = getCapture(
						new Rectangle(0, screenSize.height - captureOffset, screenSize.width, captureWidth));
				BufferedImage right = getCapture(
						new Rectangle(screenSize.width - captureOffset - 120, 0, captureWidth, screenSize.height));
				float pixelsPerLed = perimeter / (float) LEDS_COUNT;

				int offset = updateTargetLeds(0, postProcessLeds(convertToLed(top, (int) pixelsPerLed, true)));
				offset = updateTargetLeds(offset, postProcessLeds(convertToLed(left, (int) pixelsPerLed, false)));
				offset = updateTargetLeds(offset, postProcessLeds(convertToLed(bottom, (int) pixelsPerLed, false)));
				updateTargetLeds(offset, postProcessLeds(convertToLed(right, (int) pixelsPerLed, true)));
			}
		}
	};

	private ScreenCapturer screenCapturer;

	public LedManager(LedConnector ledConnector) {
		this.ledConnector = ledConnector;
		for (int i = 0; i < LEDS_COUNT; ++i) {
			targetLeds.add(new RGB(0, 0, 0));
			curLeds.add(new RGB(0, 0, 0));
		}
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private List<RGB> convertToLed(BufferedImage bufferedImage, int pixelsPerLed, boolean inverse) {
		List<RGB> output = new ArrayList<>();
		int rgbArray[] = new int[Math.max(bufferedImage.getWidth(), bufferedImage.getHeight())];
		bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), rgbArray, 0, 1);
		int ledPixels = 0;
		int start = inverse ? rgbArray.length - 1 : 0;
		int step = inverse ? -1 : +1;
		RGB average = new RGB(0, 0, 0);
		for (int i = start; i <= rgbArray.length - 1 && i >= 0; i += step) {
			ledPixels++;
			average.r += (rgbArray[i] & 0x00FF0000) >> 16;
			average.g += (rgbArray[i] & 0x0000FF00) >> 8;
			average.b += (rgbArray[i] & 0x000000FF);
			if (ledPixels >= pixelsPerLed) {
				ledPixels = 0;
				average.forEachColor(color -> color / pixelsPerLed);
				output.add(average);
				average = new RGB(0, 0, 0);
			}
		}
		return output;
	}

	private BufferedImage getCapture(Rectangle bounds) {
//		long start = System.currentTimeMillis();
//		BufferedImage image = JNAScreenshot.getScreenshot(bounds);
		BufferedImage image = robot.createScreenCapture(bounds);
//		logger.debug((System.currentTimeMillis() - start) + " ms");
		return image;
	}

	private List<RGB> postProcessLeds(List<RGB> leds) {
		float hsb[] = new float[3];
		for (int i = 0; i < leds.size(); ++i) {
			RGB led = leds.get(i);

			Color.RGBtoHSB((int) led.r, (int) led.g, (int) led.b, hsb);
			hsb[1] = (float) Math.sqrt(hsb[1]);
			hsb[2] = (float) Math.sqrt(hsb[2]);
			Color c = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
			led.r = c.getRed();
			led.g = c.getGreen();
			led.b = c.getBlue();

			if (i <= HORZ_LEDS) {
				led.multiply(BRIGHTNESS_CORRECTION_TOP);
			} else if (i > HORZ_LEDS && i <= VERT_LEDS) {
				led.multiply(BRIGHTNESS_CORRECTION_LEFT);
			} else if (i > HORZ_LEDS + VERT_LEDS && i < HORZ_LEDS + VERT_LEDS + HORZ_LEDS) {
				led.multiply(BRIGHTNESS_CORRECTION_BOTTOM);
			} else {
				led.multiply(BRIGHTNESS_CORRECTION_RIGHT);
			}
			// colors correction
			led.r *= COLOR_CORRECTION_RED;
			led.g *= COLOR_CORRECTION_GREEN;
			led.b *= COLOR_CORRECTION_BLUE;

			// monitor brightness = MONITOR_LOW_BRIGHTNESS gives led brightness = 0
			// monitor brightness = MONITOR_HIGH_BRIGHTNESS and higher don't change led
			// brightness
			// linear proportion between
			if (brightness <= MONITOR_LOW_BRIGHTNESS) {
				led.multiply(0);
			} else if (brightness >= MONITOR_HIGH_BRIGHTNESS) {
//				led.multiply(1);
			} else {
				led.multiply((brightness - MONITOR_LOW_BRIGHTNESS) / MONITOR_HIGH_BRIGHTNESS);
			}
			led.checkBounds();
		}
		return leds;
	}

	private void run() {
		try {
			if (mode.equals(MODE.DYNAMIC)) {
				screenCapturer = new ScreenCapturer();
				screenCapturer.start();
			} else {
				screenCapturer = null;
			}

			while (!Thread.interrupted()) {

				switch (mode) {
				case STATIC:
					for (int i = 0; i < targetLeds.size(); ++i) {
						targetLeds.set(i, new RGB(255, 255, 255));
					}
					postProcessLeds(targetLeds);
					break;
				case OFF:
					for (int i = 0; i < targetLeds.size(); ++i) {
						targetLeds.set(i, new RGB(0, 0, 0));
					}
					break;
				default:
					break;
				}

				int sleepTime = 0;
				while (sleepTime < 5000) {
					synchronized (targetLeds) {
						targetLeds.wait(15);
						if (!targetLeds.equals(curLeds)) {
							break;
						}
					}
					sleepTime += 15;
				}

				for (int i = 0; i < curLeds.size(); ++i) {
					RGB cur = curLeds.get(i);
					RGB target = targetLeds.get(i);
					for (int j = 0; j < 3; ++j) {
						if (Math.abs(cur.getColor(j) - target.getColor(j)) >= 1) {
							cur.setColor(j, (1 - CHANGE_SPEED) * cur.getColor(j) + CHANGE_SPEED * target.getColor(j));
						} else {
							cur.setColor(j, target.getColor(j));
						}
					}
				}

				ledConnector.submit(curLeds);
			}
		} catch (InterruptedException e) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (screenCapturer != null) {
				screenCapturer.interrupt();
				screenCapturer = null;
			}
		}
	}

	private Thread runner;

	public void start() {
		if (runner == null) {
			runner = new Thread(this::run);
			runner.start();
		}
	}

	public void setMode(MODE mode) {
		if (mode.equals(this.mode)) {
			return;
		}
		this.stop();
		this.mode = mode;
		this.start();
	}

	public void toggleMode() {
		switch (mode) {
		case DYNAMIC:
			setMode(MODE.STATIC);
			break;
		case STATIC:
			setMode(MODE.OFF);
			break;
		default:
			setMode(MODE.DYNAMIC);
		}
	}

	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}

	public void stop() {
		if (runner == null) {
			return;
		}
		runner.interrupt();
		try {
			runner.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		runner = null;
	}

	public MODE getMode() {
		return this.mode;
	}
}