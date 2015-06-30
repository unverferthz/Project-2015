
#include "Adafruit_BLE_UART.h"
#include <SPI.h>

#include <FileIO.h>
#include <NewPing.h>

const int MAX_DISTANCE = 400;
//const int ledPin = 4;
//const int buttonPin = 3; 
const int MAX_DISTANCE = 200;
//const int ledPin = 4; 
const int chipSelect = 53;

#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2     // This should be an interrupt pin, on Uno thats #2 or #3
#define ADAFRUITBLE_RST 9

Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

NewPing sensor1(13,12,MAX_DISTANCE);

File dataFile;
File objectCountFile;

//int ledState = LOW;
//int buttonState = 0;
int objectPassedCount = 0;
int loopsWithNoObjectInfrontCount = 0;
//boolean switchTurnedOff = true;
//boolean writeToggle = false;
boolean canCount = true;


void setup() {
   Serial.begin(9600);
   BTLEserial.setDeviceName("Ardu"); /* 7 characters max! */
   BTLEserial.begin();
}

aci_evt_opcode_t laststatus = ACI_EVT_DISCONNECTED;

//Checks current status of bluetooth and performs necessary action
void bluetoothCheck(){
  BTLEserial.pollACI();
  
  //Ask what is our current status
  aci_evt_opcode_t status = BTLEserial.getState();
  
  // If the status changed
  if (status != laststatus) {
    // print it out
    if (status == ACI_EVT_DEVICE_STARTED) {
        Serial.println(F("* Advertising started"));
    }
    if (status == ACI_EVT_CONNECTED) {
        Serial.println(F("* Connected!"));
    }
    if (status == ACI_EVT_DISCONNECTED) {
      {
        Serial.println(F("* Disconnected or advertising timed out"));
      }
    }
    //Keep track of what the status changed to, incase it changes again
    laststatus = status;
  }
}

//Performs any actions that will happen if the button is pushed
/*void buttonCheck(){
   aci_evt_opcode_t status = BTLEserial.getState();
  
  //Check if button is pushed down or not
  buttonState = digitalRead(buttonPin);
  
  if(buttonState == HIGH)
  {
    writeToggle = true;
  }
  else
  {
    switchTurnedOff = false;
    writeToggle = false;
  }
  //If button is pushed and first time through loop while button is down, stops toggling if button is held down


    //Set bool false, so this if statement doesn't loop
    
    
    //Toggle if SD is being written to
    //writeToggle = !writeToggle;
    
    
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

//Checks for objects infront of sensors and counts them if values are reset
void checkForCount(){
  
  //Get current bluetooth status
  aci_evt_opcode_t status = BTLEserial.getState();
  
  //Check that bluetooth is connected before sending data
  if(status == ACI_EVT_CONNECTED)
  {
    //Read in distance from sonar sensors
    int sensor1Time = sensor1.ping();
    
    //Delay so sensors don't interfere with each other
    //delay(20);
    
    //int sensor2Time = sensor2.ping();
    delay(20);
    
    //Get distance in CM
    int sensor1Distance = sensor1Time / US_ROUNDTRIP_CM;
    
    /****************  Remove when done  *******************/
    /********* Send sensor distance for testing  **********/
    sendData(String(sensor1Distance));
    
    
   //Distance threshold for counting object
   if (sensor1Distance < 10000  && sensor1Distance > 0) 
   {     
     //Reset value because object passed infront of sensor before counted to 4
     loopsWithNoObjectInfrontCount = 0;
     
     //Can only read 1 object until sensor reads nothing again, to avoid counting 1 object multiple times.
     if(canCount)
     { 
       //Stop counting until reset
       canCount = false;
       
       //Object was counted, update values and print
       objectPassedCount++;
       Serial.print("counted ");
       Serial.println(objectPassedCount);  
         
       //Send the distance over bluetooth to phone
       sendData(String(sensor1Distance));
    }
   }//End of distance check
   else
   {
     //Reset so can count objects again after 4 times through loop with no values
     if(loopsWithNoObjectInfrontCount > 4)
     {
       //Reset values
       canCount = true;
       Serial.println("can count");
       loopsWithNoObjectInfrontCount = 0;
     }
     else
     {
       //Add to counter for reset
       loopsWithNoObjectInfrontCount++;
     }
   }
 }
}//End checkForCount


//Method for handling when data is received over bluetooth
void recieveData() {
  //Get bluetooth status
  aci_evt_opcode_t status = BTLEserial.getState();
   
  //If it's connected
   if (status == ACI_EVT_CONNECTED) {
    //Check if there is any data
    if (BTLEserial.available()) {
      Serial.print("* "); Serial.print(BTLEserial.available()); Serial.println(F(" bytes available from BTLE"));
    }
    
    //Read over all the characters and print it out
    while (BTLEserial.available()) {
      char c = BTLEserial.read();
      Serial.print(c);
    }
  }
}

//The string passed into method will be send over bluetooth
void sendData(String data) {  
 aci_evt_opcode_t status = BTLEserial.getState();
      //Read a line from Serial
      Serial.setTimeout(100); // 100 millisecond timeout
      String s = data;
      
      //Convert the line to bytes, no more than 20 at this time
      uint8_t sendbuffer[20];
      s.getBytes(sendbuffer, 20);
      char sendbuffersize = min(20, s.length());

      //Display current status over serial
      Serial.print(F("\n* Sending -> \"")); Serial.print((char *)sendbuffer); Serial.println("\"");

      //Send data over bluetooth
      BTLEserial.write(sendbuffer, sendbuffersize);
    
} //End sendData


void loop()
{  
  //Checks for the status of the bluetooth connection and performs relevant actions
  bluetoothCheck();
  
  //aci_evt_opcode_t status = BTLEserial.getState();
  //Check for button press
  //buttonCheck();

  //Set LED to it's current state(lit or unlit)

  //digitalWrite(ledPin, ledState);
  
  //Check sonar sensors
  checkForCount();
  
  //Check if data was received over bluetooth
  recieveData();
 
}//End loop
