#include <Time.h>
#include <SD.h>
#include "Ultrasonic.h"
#include <FileIO.h>

Ultrasonic sensor1 (12,13);
Ultrasonic sensor2(10,11);
const int ledPin = 3;
const int buttonPin = 2; 
const int chipSelect = 53;

File dataFile;
File objectCountFile;

int ledState = LOW;
int buttonState = 0;
int objectPassedCount = 0;
boolean writeToggle = false;
boolean buttonRelease = true;
boolean canCount = true;
int loopsWithNoObjectInfrontCount = 0;

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


//Performs any actions that will happen if the button is pushed
void buttonCheck(){
  
  //Check if button is pushed down or not
  buttonState = digitalRead(buttonPin);
  
  //If button is pushed and first time through loop while button is down, stops toggling if button is held down
  if(buttonState == HIGH && buttonRelease)
  {
    //Set bool false, so this if statement doesn't loop
    buttonRelease = false;
    
    //Toggle if SD is being written to
    writeToggle = !writeToggle;
    
    //If writing was turned off, write to a file saying how many people it counting during the time it was writing
    if(!writeToggle)
    {
      Serial.print("Objects counted: ");
      Serial.println(objectPassedCount);
      objectCountFile = SD.open("objectPassCount.txt", FILE_WRITE);
      delay(500);
      objectCountFile.print("Objects counted: ");
      objectCountFile.println(objectPassedCount);
      objectCountFile.close();
      
      dataFile = SD.open("datalog.txt", FILE_WRITE);
      dataFile.println("");
      dataFile.print("Objects Counted: ");
      dataFile.println(objectPassedCount);
      dataFile.close();
      
      //Reset counter for next round of counting
      objectPassedCount = 0;
    }
   
    //Turn LED on or off, to indicated if SD is being written to
    if(ledState == HIGH)
    {
       ledState = LOW; 
    }
    else
    {
      ledState = HIGH;
    }
  }
  
  //If button isn't being pushed then enable the first if statement again
  if(buttonState == LOW)
  {
   buttonRelease = true; 
  }
  
} //End buttonCheck

//Writes data into the SD card if writing is enabled and sensor picks up objects
void writeToSD(){
  
  //If true, can write into SD card
  if(writeToggle)
  {
    //Read in distance from sonar sensors
    int sensor1Distance = sensor1.Ranging(CM);
    
    //Delay so sensors don't interfere with each other
    delay(20);
    
    int sensor2Distance = sensor2.Ranging(CM);
    
    Serial.print("Sensor 1: "); Serial.println(sensor1Distance);
    Serial.print("Sensor 2: "); Serial.println(sensor2Distance);
    
    
   //If distance is less than 2m
   if (sensor1Distance < 200) 
   {
     loopsWithNoObjectInfrontCount = 0;
     
     //Can only read 1 object until sensor reads nothing again, to avoid counting 1 object multiple times.
     if(canCount)
     { 
       //Open the file or create and open file if it doesn't exist already.
       dataFile = SD.open("datalog.txt", FILE_WRITE);
       
       //If there is a data file, write into it
       if(dataFile)
       {
         objectPassedCount++;
         canCount = false;
         
         //write to SD card
         dataFile.print("d");
         dataFile.print(sensor1Distance);
         dataFile.print(",");
        
         //Write to serial
         Serial.print(distance);
       }
       
       //Close SD cards file
       dataFile.close();
    }
   }//End of distance check
   //Sensor1 picked up nothing, check sensor 2
   else if(sensor2Distance < 200)
   {
     loopsWithNoObjectInfrontCount = 0;
     
     //Can only read 1 object until sensor reads nothing again, to avoid counting 1 object multiple times.
     if(canCount)
     { 
       //Open the file or create and open file if it doesn't exist already.
       dataFile = SD.open("datalog.txt", FILE_WRITE);
       
       //If there is a data file, write into it
       if(dataFile)
       {
         objectPassedCount++;
         canCount = false;
         
         //write to SD card
         dataFile.print("d");
         dataFile.print(sensor2Distance);
         dataFile.print(",");
        
         //Write to serial
         Serial.print(distance);
       }
       
       //Close SD cards file
       dataFile.close();
    }
   }
   //Nothing is close enough to sensor, try enable counting again.
   else
   {
     if(loopsWithNoObjectInfrontCount > 4)
     {
       canCount = true;
       loopsWithNoObjectInfrontCount = 0;
     }
     else
     {
       loopsWithNoObjectInfrontCount++;
     }
   }
 }
}//End writeToSD

void loop()
{  
  //Check for button press
  buttonCheck();
  
  //Set LED to it's current state(lit or unlit)
  digitalWrite(ledPin, ledState);
  
  //Try to write into SD card if enabled
  writeToSD();
  
  delay(100);
 
}//End loop
