
#ifndef DOOR_H
#define DOOR_H

class MotionSensor {
public:
  MotionSensor();
  void SETUP();
  int getValue();
};

extern MotionSensor motionSensor;

#endif
