#include <Time.h>
#include <SD.h>
#include "Ultrasonic.h"
#include <FileIO.h>

Ultrasonic ultrasonic (1,1);
const int ledPin = 3;
const int buttonPin = 2; 
const int chipSelect = 53;

File dataFile;
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
<<<<<<< HEAD
      Serial.println("Writing turned off");
=======
      Serial.print("Objects countedL ");
      Serial.println(objectPassedCount);
>>>>>>> origin/master
      File objectPassCountFile = SD.open("objectPassCount.txt", FILE_WRITE);
      objectPassCountFile.print("Objects counted: ");
      objectPassCountFile.println(objectPassedCount);
      objectPassCountFile.close();
      
      
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
    //Read in distance from sonar sensor
    int distance = ultrasonic.Ranging(CM);
    Serial.print(distance);
   //If distance is less than 2m
   if (distance < 100) 
   {
<<<<<<< HEAD
     Serial.print(distance);
=======
     loopsWithNoObjectInfrontCount = 0;
     
>>>>>>> origin/master
     //Open the file or create and open file if it doesn't exist already.
     dataFile = SD.open("datalog.txt", FILE_WRITE);
      
     //If there is a data file, write into it
     if(dataFile)
     { 
       
       Serial.println("Datafile opened");
       //Can only read 1 object until sensor reads nothing again, to avoid counting 1 object multiple times.
       if(canCount)
       {
         Serial.println("Counted");
         objectPassedCount++;
         canCount = false;
         
         //write to SD card
         dataFile.print("d");
         dataFile.print(distance);
         dataFile.print(", ");
        
         //Write to serial
         Serial.print(distance);
       }
    }
    
    //Close SD cards file
    dataFile.close();
   }//End of distance check
   //Nothing is close enough to sensor, enable counting again.
   else
   {
     if(loopsWithNoObjectInfrontCount > 5)
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
