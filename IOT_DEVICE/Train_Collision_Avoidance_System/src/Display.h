
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
  void doorLock();
  void doorUnLock();
  void bulbOn(int bulbNumber);
  void bulbOff(int bulbNumber);
  void printOnline();
  void clear();
};

extern Display display;

#endif
