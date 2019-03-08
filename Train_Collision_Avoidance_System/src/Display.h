
#ifndef DISPLAY_H
#define DISPLAY_H

class Display {
public:
  Display();
  void SETUP();
  void welcomeMessageDisplay();
  void loading();
  void print(int row, int line, String message);
  void printHome();
  void printOnline();
  void leftLaserAnimation();
  void rightLaserAnimation();
  void motionAnimation();
  void nodeNumberDisplay(String nodeId);
  void clear();
};

extern Display display;

#endif
