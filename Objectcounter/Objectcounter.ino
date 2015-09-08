
#include "Adafruit_BLE_UART.h"
#include <SPI.h>

#include <FileIO.h>
#include <NewPing.h>

const int MAX_DISTANCE = 200;
const int ledPin = 4; 
const int chipSelect = 53;

long pulse,inches,cm;
#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2     // This should be an interrupt pin, on Uno thats #2 or #3
#define ADAFRUITBLE_RST 9

Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

NewPing sensor1(13,11,MAX_DISTANCE);

File dataFile;
File objectCountFile;

int ledState = LOW;
int buttonState = 0;
int objectPassedCount = 0;
int loopsWithNoObjectInfrontCount = 0;
int loopsWithObjectInfrontCount = 0;
boolean canCount = true;


void setup() {
   Serial.begin(9600);
   BTLEserial.setDeviceName("Ardu"); /* 7 characters max! */
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
/*void buttonCheck(){
   aci_evt_opcode_t status = BTLEserial.getState();

    //If writing was turned off, write to a file saying how many people it counting during the time it was writing
    if(status == ACI_EVT_CONNECTED )
     {
      Serial.print("Objects counted:");
      Serial.print(objectPassedCount);
      
       if (status == ACI_EVT_CONNECTED) {
        // Read a line from Serial
        Serial.setTimeout(100); // 100 millisecond timeout
        
        //String s = "Objects counted " + String(objectPassedCount);
        String s = String(objectPassedCount);
  
        // We need to convert the line to bytes, no more than 20 at this time
        uint8_t sendbuffer[20];
        s.getBytes(sendbuffer, 20);
        char sendbuffersize = min(20, s.length());
  
        Serial.print(F("\n* Sending -> \"")); Serial.print((char *)sendbuffer); Serial.println("\"");
  
        // write the data
        BTLEserial.write(sendbuffer, sendbuffersize);
       }
      objectPassedCount = 0;
     }
      //Reset counter for next round of counting

  
} //End buttonCheck*/

//Writes data into the SD card if writing is enabled and sensor picks up objects
void checkForCount(){
  aci_evt_opcode_t status = BTLEserial.getState();
  //If true, can write into SD card
  if(status == ACI_EVT_CONNECTED)
  {
    //Read in distance from sonar sensors
    int sensor1Time = sensor1.ping();

    //Delay so sensors don't interfere with each other
         delay(20);
    Serial.print(cm);
    int sensor1Distance = sensor1Time / US_ROUNDTRIP_CM;
    //sendData(String(cm));
   //If distance is less than 2m
   if (sensor1Distance > 30 && sensor1Distance < 150) 
   {
     loopsWithNoObjectInfrontCount = 0;
     loopsWithObjectInfrontCount++;
     //Can only read 1 object until sensor reads nothing again, to avoid counting 1 object multiple times.
     if(canCount && loopsWithObjectInfrontCount > 4)
     { 
         objectPassedCount++;
         canCount = false;
         Serial.print("counted ");
         Serial.println(objectPassedCount);  
         //sendData("Distance " + String(sensor1Distance) + " cm");
         sendData(String(sensor1Distance));
         
    }
   }//End of distance check 
   else
   {
     if(loopsWithNoObjectInfrontCount > 4)
     {
       loopsWithObjectInfrontCount = 0;
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

void sendData(String data) {
 
  
 aci_evt_opcode_t status = BTLEserial.getState();
      // Read a line from Serial
      if (status == ACI_EVT_CONNECTED) {
      Serial.setTimeout(100); // 100 millisecond timeout
      String s = data;
      
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
  aci_evt_opcode_t status = BTLEserial.getState();
  //Check for button press
  //buttonCheck();

  //Set LED to it's current state(lit or unlit)

  digitalWrite(ledPin, ledState);
  
  //Try to write into SD card if enabled
  checkForCount();
  recieveData();
 
}//End loop
