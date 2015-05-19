#include "Adafruit_BLE_UART.h"
#include <SPI.h>
#include <FileIO.h>
#include <NewPing.h>

const int MAX_DISTANCE = 200;
const int ledPin = 4;
const int buttonPin = 3; 
const int chipSelect = 53;

#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2     // This should be an interrupt pin, on Uno thats #2 or #3
#define ADAFRUITBLE_RST 9

Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

NewPing sensor1(13,12,MAX_DISTANCE);

File dataFile;
File objectCountFile;

int ledState = LOW;
int buttonState = 0;
int objectPassedCount = 0;
int loopsWithNoObjectInfrontCount = 0;

boolean writeToggle = false;
boolean buttonRelease = true;
boolean canCount = true;


void setup() {
   Serial.begin(9600);
   pinMode(ledPin, OUTPUT);
   pinMode(buttonPin, INPUT);
   BTLEserial.setDeviceName("Pingas"); /* 7 characters max! */
   BTLEserial.begin();

}

aci_evt_opcode_t laststatus = ACI_EVT_DISCONNECTED;

void bluetoothCheck(){
  BTLEserial.pollACI();
  //Ask what is our current status
  aci_evt_opcode_t status = BTLEserial.getState();
  // If the status changed....
  if (status != laststatus) {
    // print it out!
    if (status == ACI_EVT_DEVICE_STARTED) {
        Serial.println(F("* Advertising started"));
    }
    if (status == ACI_EVT_CONNECTED) {
        Serial.println(F("* Connected!"));
    }
    if (status == ACI_EVT_DISCONNECTED) {
        Serial.println(F("* Disconnected or advertising timed out"));
    }
    // OK set the last status change to this one
    laststatus = status;
  }
}
//Performs any actions that will happen if the button is pushed
void buttonCheck(){
   aci_evt_opcode_t status = BTLEserial.getState();
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
     
      Serial.print("Objects counted:");
      Serial.print(objectPassedCount);
      
      if (status == ACI_EVT_CONNECTED) {
        // Read a line from Serial
        Serial.setTimeout(100); // 100 millisecond timeout
        String s = String(objectPassedCount);
  
        // We need to convert the line to bytes, no more than 20 at this time
        uint8_t sendbuffer[20];
        s.getBytes(sendbuffer, 20);
        char sendbuffersize = min(20, s.length());
  
        Serial.print(F("\n* Sending -> \"")); Serial.print((char *)sendbuffer); Serial.println("\"");
  
        // write the data
        BTLEserial.write(sendbuffer, sendbuffersize);
      }
    }
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
  
  
  //If button isn't being pushed then enable the first if statement again
  if(buttonState == LOW)
  {
   buttonRelease = true; 
  }
  
} //End buttonCheck

//Writes data into the SD card if writing is enabled and sensor picks up objects
void checkForCount(){
  
  //If true, can write into SD card
  if(writeToggle)
  {
    //Read in distance from sonar sensors
    int sensor1Time = sensor1.ping();
    
    //Delay so sensors don't interfere with each other
    delay(20);
    
    //int sensor2Time = sensor2.ping();
    
    int sensor1Distance = sensor1Time / US_ROUNDTRIP_CM;

   //If distance is less than 2m
   if (sensor1Distance < 150  && sensor1Distance > 0) 
   {
     loopsWithNoObjectInfrontCount = 0;
     
     //Can only read 1 object until sensor reads nothing again, to avoid counting 1 object multiple times.
     if(canCount)
     { 
         objectPassedCount++;
         canCount = false;
         Serial.print("counted ");
         Serial.println(objectPassedCount);  
    }
   }//End of distance check
   else
   {
     if(loopsWithNoObjectInfrontCount > 4)
     {
       canCount = true;
       Serial.println("can count");
       loopsWithNoObjectInfrontCount = 0;
     }
     else
     {
       loopsWithNoObjectInfrontCount++;
     }
   }
 }
}//End writeToSD

void recieveData() {
   aci_evt_opcode_t status = BTLEserial.getState();
  if (status == ACI_EVT_CONNECTED) {
    // Lets see if there's any data for us!
    if (BTLEserial.available()) {
      Serial.print("* "); Serial.print(BTLEserial.available()); Serial.println(F(" bytes available from BTLE"));
    }
    // OK while we still have something to read, get a character and print it out
    while (BTLEserial.available()) {
      char c = BTLEserial.read();
      Serial.print(c);
    }
  }
}

void sendData() {
 aci_evt_opcode_t status = BTLEserial.getState();
  if (Serial.available()) {
      // Read a line from Serial
      Serial.setTimeout(100); // 100 millisecond timeout
      String s = Serial.readString();

      // We need to convert the line to bytes, no more than 20 at this time
      uint8_t sendbuffer[20];
      s.getBytes(sendbuffer, 20);
      char sendbuffersize = min(20, s.length());

      Serial.print(F("\n* Sending -> \"")); Serial.print((char *)sendbuffer); Serial.println("\"");

      // write the data
      BTLEserial.write(sendbuffer, sendbuffersize);
    }
}
void loop()
{  
  //Checks for the status of the bluetooth connection
  bluetoothCheck();
  
  //Check for button press
  buttonCheck();
  
  //Set LED to it's current state(lit or unlit)
  digitalWrite(ledPin, ledState);
  
  //Try to write into SD card if enabled
  checkForCount();
  recieveData();
  sendData();
 
}//End loop
