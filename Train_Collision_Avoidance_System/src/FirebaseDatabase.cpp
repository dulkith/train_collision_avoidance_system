
#include <Arduino.h>

#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include <ESP8266HTTPClient.h>

#include "Display.h"
#include "FirebaseDatabase.h"
#include "LaserSensors.h"
#include "MotionSensor.h"
#include "Buzzer.h"

String NODE_ID = "1";

// Set these to run.
#define FIREBASE_HOST "traincollisionavoidancesystem.firebaseio.com"
#define FIREBASE_AUTH "3EQjhz2VuLnr3NSLQWT6aT0wiV7iFivQyvi9bmFX"
//#define WIFI_SSID "Dialog 4G"
//#define WIFI_PASSWORD "Q6Q8JBA2A19"
//#define WIFI_SSID "Dumbledore's IT Staff"
//#define WIFI_PASSWORD "icantremember"
#define WIFI_SSID "OOO4G"
#define WIFI_PASSWORD "duka1234"

int systemStaus = 0;
int onlineCheck = 0;
int reset = 0;

FirebaseDatabase::FirebaseDatabase() {}

void FirebaseDatabase::SETUP() {

  // connect to wifi.
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  
  display.clear();
  display.print(0, 0, "CONNECTING.....");
  display.print(0, 1, "WIFI NETWORK.");
  delay(2000);
  // Set a static IP (optional)
  IPAddress ip(192, 168, 8, 55);
  IPAddress gateway(192, 168, 8, 1);
  IPAddress subnet(255, 255, 255, 0);
  WiFi.config(ip, gateway, subnet);

  while (WiFi.status() != WL_CONNECTED) {
    //Serial.print(".");
    delay(500);
  }

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  display.clear();
  display.printHome();
}

void FirebaseDatabase::sensorActivateLogWrite(String sensor) {
  DynamicJsonBuffer jsonBuffer;
  // Push to Firebase
  JsonObject& logObject = jsonBuffer.createObject();
  JsonObject& tempTime = logObject.createNestedObject("timestamp");
  if (sensor == "LEFT")
    logObject["sensor"] = "LEFT";
  else if (sensor == "RIGHT")
    logObject["sensor"] = "RIGHT";
  else
    logObject["sensor"] = "MOTION";
  tempTime[".sv"] = "timestamp";
  
  if(sensor == "LEFT" || sensor == "RIGHT")
    Firebase.push("EDGES/NODE_" + NODE_ID + "/LASER_ACTIVATE_HISTORY", logObject);
  else
    Firebase.push("EDGES/NODE_" + NODE_ID + "/MOTION_ACTIVATE_HISTORY", logObject);
}

void FirebaseDatabase::run() {
  // handle error
  if (Firebase.failed()) {
     //Serial.print("setting /number failed:");
     //Serial.println(Firebase.error());
    return;
  }

  if (laserSensors.getValue(1) == 0) {
    display.rightLaserAnimation();
    reset = 1;
    if (1 != Firebase.getInt("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_RIGHT")) {
      Firebase.set("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_RIGHT", 1);
      sensorActivateLogWrite("RIGHT");
      display.rightLaserAnimation();
    }
  }else{
    Firebase.set("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_RIGHT", 0);
  }

  if (laserSensors.getValue(2) == 0) {
    display.leftLaserAnimation();
    reset = 1;
    if (1 != Firebase.getInt("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_LEFT")) {
      Firebase.set("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_LEFT", 1);
      sensorActivateLogWrite("LEFT");
    }
  }else{
    Firebase.set("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_LEFT", 0);
  }

  if (motionSensor.getValue() == 0) {
    Firebase.set("EDGES/NODE_" + NODE_ID + "/MOTION_SENSOR", 0);
  }else{
    if (1 != Firebase.getInt("EDGES/NODE_" + NODE_ID + "/MOTION_SENSOR")) {
      Firebase.set("EDGES/NODE_" + NODE_ID + "/MOTION_SENSOR", 1);
      sensorActivateLogWrite("MOTION");
    }
    display.motionAnimation();
    reset = 1;
  }

  if(motionSensor.getValue() == 1 || laserSensors.getValue(1) == 0 || laserSensors.getValue(2) == 0 ){
    buzzer.beep(50);
    display.nodeNumberDisplay("01");
  }

  if(motionSensor.getValue() == 0 && laserSensors.getValue(1) == 1 && laserSensors.getValue(2) == 1 && reset == 1){
    display.printHome();
    reset = 0;
  }
  

}

FirebaseDatabase firebaseDatabase = FirebaseDatabase();
