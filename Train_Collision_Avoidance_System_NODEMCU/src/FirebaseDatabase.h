
#ifndef FIREBASE_DATABSE_H
#define FIREBASE_DATABSE_H

class FirebaseDatabase {
public:
  FirebaseDatabase();
  void SETUP();
  void sensorActivateLogWrite(String sensor);
  void run();
};

extern FirebaseDatabase firebaseDatabase;

#endif
