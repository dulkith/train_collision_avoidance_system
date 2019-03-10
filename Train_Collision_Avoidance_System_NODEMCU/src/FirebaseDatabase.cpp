
#include <Arduino.h>

#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include <ESP8266HTTPClient.h>

#include "Display.h"
#include "FirebaseDatabase.h"
#include "LaserSensors.h"
#include "MotionSensor.h"

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

  onlineCheck = Firebase.getInt("ONLINE");

  if (laserSensors.getValue(1) == 0) {
    //display.printOnline();
    if (1 != Firebase.getInt("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_RIGHT")) {
      Firebase.set("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_RIGHT", 1);
      sensorActivateLogWrite("RIGHT");
    }
  }else{
    Firebase.set("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_RIGHT", 0);
  }

  if (laserSensors.getValue(2) == 0) {
    //display.printOnline();
    if (1 != Firebase.getInt("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_LEFT")) {
      Firebase.set("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_LEFT", 1);
      sensorActivateLogWrite("LEFT");
    }
  }else{
    Firebase.set("EDGES/NODE_" + NODE_ID + "/LASER_SENSOR_LEFT", 0);
  }

  if (motionSensor.getValue() == 0) {
    //display.printOnline();
    Firebase.set("EDGES/NODE_" + NODE_ID + "/MOTION_SENSOR", 0);
  }else{
    if (1 != Firebase.getInt("EDGES/NODE_" + NODE_ID + "/MOTION_SENSOR")) {
      Firebase.set("EDGES/NODE_" + NODE_ID + "/MOTION_SENSOR", 1);
      sensorActivateLogWrite("MOTION");
    }
  }

  // Online
  if (onlineCheck == 0) {
    display.printOnline();
    Firebase.set("ONLINE", 1);
  }

}

FirebaseDatabase firebaseDatabase = FirebaseDatabase();
