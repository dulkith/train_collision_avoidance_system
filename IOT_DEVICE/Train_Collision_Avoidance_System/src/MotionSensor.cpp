
#include <Arduino.h>
#include <Servo.h>

#include "Display.h"
#include "MotionSensor.h"

//the time we give the sensor to calibrate (10-60 secs according to the datasheet)
int calibrationTime = 30;       
 
//the time when the sensor outputs a low impulse
long unsigned int lowIn;        
 
//the amount of milliseconds the sensor has to be low
//before we assume all motion has stopped
long unsigned int pause = 5000; 
 
boolean lockLow = true;
boolean takeLowTime; 

#define MOTION_SENSOR 13

int state = 0;

MotionSensor::MotionSensor() {}

void MotionSensor::SETUP() {
  pinMode(MOTION_SENSOR, INPUT);
  digitalWrite(MOTION_SENSOR, LOW);
 
  //give the sensor some time to calibrate
  //Serial.print("calibrating sensor ");
    for(int i = 0; i < calibrationTime; i++){
      //Serial.print(".");
      delay(1000);
      }
    //Serial.println(" done");
    //Serial.println("SENSOR ACTIVE");
    delay(50);
  }

/**
    [MotionSensor::getValue from PIR Motion sensor.]
*/
int MotionSensor::getValue()  {
  if(digitalRead(MOTION_SENSOR) == HIGH){
       state = 1;   //the led visualizes the sensors output pin state
       if(lockLow){ 
         //makes sure we wait for a transition to LOW before any further output is made:
         lockLow = false;           
         /*Serial.println("---");
         Serial.print("motion detected at ");
         Serial.print(millis()/1000);
         Serial.println(" sec");*/
         //delay(50);
         }        
         takeLowTime = true;
       }
 
     if(digitalRead(MOTION_SENSOR) == LOW){      
       //digitalWrite(ledPin, LOW);  //the led visualizes the sensors output pin state
       if(takeLowTime){
        lowIn = millis();          //save the time of the transition from high to LOW
        takeLowTime = false;       //make sure this is only done at the start of a LOW phase
        }
       //if the sensor is low for more than the given pause,
       //we assume that no more motion is going to happen
       if(!lockLow && millis() - lowIn > pause){ 
           //makes sure this block of code is only executed again after
           //a new motion sequence has been detected
           lockLow = true; 
           state = 0;                    
           /*Serial.print("motion ended at ");      //output
           Serial.print((millis() - pause)/1000);
           Serial.println(" sec");*/
           //delay(50);
           }
       }
       return state;
}

MotionSensor motionSensor = MotionSensor();
