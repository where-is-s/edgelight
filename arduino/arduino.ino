#include "FastLED.h"

// Configure this!
#define NUM_LEDS 112 // total number of leds, see Java code for details
#define DATA_PIN 3 // your data pin where LED strip is connected!
#define CHANGE_SPEED 64 // of 256 

// Configure this if your strip is not WS2812B
#define LED_TYPE WS2812B
#define COLOR_ORDER GRB
#define BRIGHTNESS 255

#define BLOCK_SIZE 16
#define START_MAGIC_BYTE 27
#define END_MAGIC_BYTE 91

CRGB curLeds[NUM_LEDS];
CRGB targetLeds[NUM_LEDS];
uint16_t block[BLOCK_SIZE];
byte header[1];
int read = 0;

void setup() {
  delay(1000); // initial delay of a few seconds is recommended
  FastLED.addLeds<LED_TYPE, DATA_PIN, COLOR_ORDER>(curLeds, NUM_LEDS); //.setCorrection(TypicalLEDStrip); // initializes LED strip
  FastLED.setBrightness(BRIGHTNESS); // global brightness
  Serial.setTimeout(20);
  Serial.begin(128000);//153600);
}

void loop() {
  header[0] = 0;
  read = 1;
  do {
    read = Serial.readBytes(header, 1);
  } while (header[0] != START_MAGIC_BYTE && read > 0);

  if (read > 0) { // successfully found header
    Serial.readBytes(header, 1);
    int offset = header[0];
    
    Serial.readBytes((byte*) block, 2 * BLOCK_SIZE);
    Serial.readBytes(header, 1);

    if (header[0] == END_MAGIC_BYTE) { // correctly read the whole block
      for (int i = 0; i < BLOCK_SIZE; ++i) {
        uint16_t leval = block[i];
        uint16_t val = ((leval & 0xFF) << 8) + (leval >> 8);
        targetLeds[i + offset].r = ((val >> 11) << 3) & 0xFF;
        targetLeds[i + offset].g = ((val >> 5) << 2) & 0xFF;
        targetLeds[i + offset].b = ((val) << 3) & 0xFF;
      }
    }
  } else { // no COM connection
    for (int i = 0; i < NUM_LEDS; ++i) {
      targetLeds[i] = CRGB(192, 160, 112);
    }
  }

  for (int i = 0; i < NUM_LEDS; ++i) {
    if (curLeds[i].r - targetLeds[i].r < 5 && curLeds[i].r - targetLeds[i].r > -5 &&
        curLeds[i].g - targetLeds[i].g < 5 && curLeds[i].g - targetLeds[i].g > -5 &&
        curLeds[i].b - targetLeds[i].b < 5 && curLeds[i].b - targetLeds[i].r > -5) {
      curLeds[i] = targetLeds[i];
    } else {
      curLeds[i] = curLeds[i] % (256 - CHANGE_SPEED) + targetLeds[i] % CHANGE_SPEED;
    }
  }
  
  FastLED.show();
}
