
#include <Arduino.h>
#include <LiquidCrystal_I2C.h>

#include "Buzzer.h"
#include "Display.h"
#include "LaserSensors.h"
#include "MotionSensor.h"

#define LCD_ADDRESS 0x3F
#define NEW_SDA 0
#define NEW_SCL 2
#define USE_NEW_I2C

LiquidCrystal_I2C lcd(LCD_ADDRESS, 16, 4);

// Custom characters.
// Door Lock.
uint8_t LOCK_LOGO[8] = {B01110, B10001, B10001, B11111,
                        B11011, B11011, B11111, B00000};
// Door Un-Lock.
uint8_t UNLOCK_LOGO[8] = {B01110, B10000, B10000, B11111,
                          B11011, B11011, B11111, B00000};
// Ccaracteres para formar BATMAN
uint8_t B1_UL[8] = {B00000, B00000, B00000, B11111,
                    B11111, B01111, B00111, B00111};
uint8_t B1_ULM[8] = {B00000, B00000, B00000, B10000,
                     B10010, B11011, B11111, B11111};
uint8_t B1_URM[8] = {B00000, B00000, B00000, B00001,
                     B01001, B11011, B11111, B11111};
uint8_t B1_UR[8] = {B00000, B00000, B00000, B11111,
                    B11111, B11110, B11100, B11100};
uint8_t B1_DL[8] = {B00000, B00000, B00000, B00011,
                    B00001, B00000, B00000, B00000};
uint8_t B1_DLM[8] = {B01111, B00011, B00001, B00000,
                     B00000, B00000, B00000, B00000};
uint8_t B1_DRM[8] = {B11110, B11000, B10000, B00000,
                     B00000, B0000,  B00000, B00000};
uint8_t B1_DR[8] = {B00000, B00000, B00000, B11000,
                    B10000, B00000, B00000, B00000};
// Loading bar
uint8_t loadBar[8] = {B00000, B00000, B00000, B00000,
                      B11111, B11111, B11111, B00000};
// Bulb on/off
uint8_t BULB_OFF[8] = {0x0E, 0x11, 0x11, 0x11, 0x11, 0x0E, 0x0E, 0x04};
uint8_t BULB_ON[8] = {0x0E, 0x1F, 0x1F, 0x1F, 0x1B, 0x0E, 0x0E, 0x04};

// Online
byte ONLINE[] = {0x01, 0x01, 0x1F, 0x11, 0x11, 0x1F, 0x1F, 0x1F};

Display::Display() {}

void Display::SETUP() {
  // LCD display
#ifdef USE_NEW_I2C
  lcd.init(NEW_SDA, NEW_SCL);
#else
  lcd.init();
#endif
  lcd.backlight();
  welcomeMessageDisplay();
  loading();
}

void Display::welcomeMessageDisplay() {

  lcd.createChar(0, B1_UL);
  lcd.createChar(1, B1_ULM);
  lcd.createChar(2, B1_URM);
  lcd.createChar(3, B1_UR);
  lcd.createChar(4, B1_DL);
  lcd.createChar(5, B1_DLM);
  lcd.createChar(6, B1_DRM);
  lcd.createChar(7, B1_DR);

  lcd.setCursor(5, 0);
  lcd.write(byte(4));
  lcd.write(byte(0));
  lcd.write(byte(1));
  lcd.write(byte(2));
  lcd.write(byte(3));
  lcd.write(byte(7));
  lcd.setCursor(7, 1);
  lcd.write(byte(5));
  lcd.write(byte(6));

  print(-4, 2, "TRAIN COLLISION");
  print(-4, 3, "AVOIDANCE SYSTEM");
  delay(4000);
  buzzer.beep(60, 5);
}

void Display::loading() {
  lcd.clear();
  print(2, 2, "LOADING...");
  lcd.createChar(8, loadBar);
  lcd.setCursor(-4, 3);
  for (int i = 0; i < 16; i++) {
    lcd.write(byte(8));
    delay(60);
  }
  delay(1000);
  buzzer.beep(50, 2);
  //lcd.clear();
}

void Display::print(int row, int line, String message) {
  lcd.setCursor(row, line);
  lcd.print(message);
}
/*
void Display::printHome() {
  print(0, 0, "-- HAPPY LOCK --");

  if (door.isDoorLock())
    doorLock();
  else
    doorUnLock();

  if (relayBulbs.isBulbOn(1))
    bulbOff(1);
  else
    bulbOn(1);

  if (relayBulbs.isBulbOn(2))
    bulbOff(2);
  else
    bulbOn(2);
}*/

void Display::doorLock() {
  lcd.createChar(9, LOCK_LOGO);
  lcd.setCursor(-3, 3);
  lcd.write(9);
  print(-2, 3, "-LOCK  ");
};
void Display::doorUnLock() {
  lcd.createChar(10, UNLOCK_LOGO);
  lcd.setCursor(-3, 3);
  lcd.write(10);
  print(-2, 3, "-UNLOCK");
};
void Display::bulbOn(int bulbNumber) {
  lcd.createChar(11, BULB_ON);
  if (bulbNumber == 1)
    lcd.setCursor(7, 3);
  else
    lcd.setCursor(8, 3);
  lcd.write(11);
};
void Display::bulbOff(int bulbNumber) {
  lcd.createChar(12, BULB_OFF);
  if (bulbNumber == 1)
    lcd.setCursor(7, 3);
  else
    lcd.setCursor(8, 3);
  lcd.write(12);
};

void Display::printOnline() {
  lcd.createChar(13, ONLINE);
  lcd.setCursor(11, 3);
  lcd.write(13);
  delay(100);
  print(11, 3, " ");
}

void Display::clear() { lcd.clear(); }

Display display = Display();
