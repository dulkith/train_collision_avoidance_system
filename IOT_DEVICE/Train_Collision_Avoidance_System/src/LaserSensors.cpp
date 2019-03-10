
#include <Arduino.h>

#include "Display.h"
#include "FirebaseDatabase.h"
#include "LaserSensors.h"

#define LASER_SENSOR_1 14
#define LASER_SENSOR_2 12

LaserSensors::LaserSensors() {}

void LaserSensors::SETUP() {
  pinMode(LASER_SENSOR_1, INPUT);
  pinMode(LASER_SENSOR_2, INPUT);
}

/**
    [LaserSensors::getValue from laser detector.]
    @param sensorNumber [Relay number 1 or 2]
*/
int LaserSensors::getValue(int sensorNumber)  {
  if (sensorNumber == 1) {
    return digitalRead(LASER_SENSOR_1);
  } else if (sensorNumber == 2) {
    return digitalRead(LASER_SENSOR_2);
  }
}

LaserSensors laserSensors = LaserSensors();
