#include <Time.h>

#include <SD.h>
#include "Ultrasonic.h"
#include <FileIO.h>

Ultrasonic ultrasonic (12,13);
const int ledPin = 3;
const int buttonPin = 2; 
const int chipSelect = 53;
File dataFile;

int ledState = LOW;
int buttonState = 0;
boolean writeToggle = false;
boolean buttonRelease = true;

void setup() {
   Serial.begin(9600);
   pinMode(ledPin, OUTPUT);
   pinMode(buttonPin, INPUT);
   initSD();
}

void initSD() {
   Serial.print("Initializing SD card...");
   pinMode(10, OUTPUT);
 
   if (!SD.begin(chipSelect)) {
    Serial.println("Card failed, or not present");
    // don't do anything more:
    return;
  }
  Serial.println("card initialized.");
}

void loop()
{
  
buttonState = digitalRead(buttonPin);

if(buttonState == HIGH && buttonRelease )
{
  buttonRelease = false;
  writeToggle = !writeToggle;
 
  if(ledState == HIGH)
  {
     ledState = LOW; 
  }
  else
  {
    ledState = HIGH;
  }
}

if(buttonState == LOW)
{
 buttonRelease = true; 
}

digitalWrite(ledPin, ledState);

if(writeToggle)
{
 dataFile = SD.open("datalog.txt", FILE_WRITE);
  
 int distance = ultrasonic.Ranging(CM);
 
 if(dataFile){ 

   if (distance < 100) {
    dataFile.print("d");
    dataFile.print(distance);
    Serial.print(distance);
    Serial.print("t");
    dataFile.close();
    }
  }
}
 delay(100);
  
}
