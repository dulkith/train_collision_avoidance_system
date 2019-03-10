
/*------------------- Includes -------------------*/

#include <Arduino.h>

#include "ActionSwitch.h"
#include "Buzzer.h"
#include "Display.h"
#include "LaserSensors.h"
#include "MotionSensor.h"
#include "FirebaseDatabase.h"

/*-------------- Global Variables --------------*/

/*------------------- Setup -------------------*/

void setup() {
  // Debug console
  Serial.begin(9600);
  //
  laserSensors.SETUP();
  actionSwitch.SETUP();
  buzzer.SETUP();
  display.SETUP();
  motionSensor.SETUP();
  firebaseDatabase.SETUP();
}

/*------------------- Loop -------------------*/

void loop() {
  firebaseDatabase.run();
  // actionSwitch.lightsControl();
  // actionSwitch.doorControl();
  //modeSwitch.modControl();
}
