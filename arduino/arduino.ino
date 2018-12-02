#define FASTLED_ALLOW_INTERRUPTS 0

#include "FastLED.h"

// Configure this!
#define NUM_LEDS 112 // total number of leds, see Java code for details
#define DATA_PIN 13 // your data pin where LED strip is connected!
#define CHANGE_SPEED 64 // of 256 

// Configure this if your strip is not WS2812B
#define LED_TYPE WS2812B
#define COLOR_ORDER GRB
#define BRIGHTNESS 255

#define START_MAGIC_BYTE 27
#define END_MAGIC_BYTE 91

CRGB curLeds[NUM_LEDS];
CRGB targetLeds[NUM_LEDS];
byte block[NUM_LEDS * 3];
byte header[1];
int read = 0;

void setup() {
  delay(1000); // initial delay of a few seconds is recommended
  FastLED.addLeds<LED_TYPE, DATA_PIN, COLOR_ORDER>(curLeds, NUM_LEDS); //.setCorrection(TypicalLEDStrip); // initializes LED strip
  FastLED.setBrightness(BRIGHTNESS); // global brightness
  Serial.setTimeout(20);
  Serial.begin(921600);
}

int timeout = 0;

void loop() {
  do {
    read = Serial.readBytes(header, 1);
  } while (header[0] != START_MAGIC_BYTE && read == 1);

  if (read == 1) { // successfully found header
    Serial.readBytes(header, 1);
    int offset = header[0];
    
    Serial.readBytes(block, 3 * NUM_LEDS);
    Serial.readBytes(header, 1);

    if (header[0] == END_MAGIC_BYTE) { // correctly read the whole block
      for (int i = 0; i < NUM_LEDS; ++i) {
        curLeds[i + offset].r = block[3 * i + 0];
        curLeds[i + offset].g = block[3 * i + 1];
        curLeds[i + offset].b = block[3 * i + 2];
      }
    }

    timeout = 0;
  } else { // no COM connection
    if (timeout < 100) {
      ++timeout;
    } else {
      CRGB defaultLed(192, 160, 112);
      for (int i = 0; i < NUM_LEDS; ++i) {
        curLeds[i] = curLeds[i] % (256 - CHANGE_SPEED) + defaultLed % CHANGE_SPEED;
      }
    }
  }

  FastLED.show();
}
