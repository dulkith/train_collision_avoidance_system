
#ifndef LASER_SENSOR_H
#define LASER_SENSOR_H

class LaserSensors {
public:
  LaserSensors();
  void SETUP();
  int getValue(int sensorNumber);
};

extern LaserSensors laserSensors;

#endif
