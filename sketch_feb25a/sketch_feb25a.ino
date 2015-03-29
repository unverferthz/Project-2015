#include <Time.h>

#include <SD.h>
#include "Ultrasonic.h"
#include <FileIO.h>



Ultrasonic ultrasonic (12,13);

const int chipSelect = 53;
const int buttonPin = 2;

File dataFile;

void setup() {
   Serial.begin(9600);
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

 if(!buttonState == HIGH){
     
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
