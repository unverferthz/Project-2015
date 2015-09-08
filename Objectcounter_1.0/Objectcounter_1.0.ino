
#include <SPI.h>

#include <FileIO.h>
#include <NewPing.h>

const int MAX_DISTANCE = 200;
const int ledPin = 4; 

const int chipSelect = 53;


NewPing sensor1(9,8,MAX_DISTANCE);

File dataFile;
File objectCountFile;

int ledState = LOW;
int buttonState = 0;
int objectPassedCount = 0;
int loopsWithNoObjectInfrontCount = 0;
int loopsWithObjectInfrontCount = 0;

boolean isConnected = true;
boolean canCount = true;


void setup() {
   Serial.begin(9600);
}


//Writes data into the SD card if writing is enabled and sensor picks up objects
void checkForCount(){

  if(isConnected == true)
  {
    delay(5);
    double voltage = analogRead(5)*.0049;
    int sensor1Distance = voltage/.0049;
   if (sensor1Distance > 30 && sensor1Distance < 150) 
   {
     loopsWithNoObjectInfrontCount = 0;
     loopsWithObjectInfrontCount++;
     //Can only read 1 object until sensor reads nothing again, to avoid counting 1 object multiple times.
     if(canCount && loopsWithObjectInfrontCount > 6)
     { 
         objectPassedCount++;
         canCount = false;
         //sendData("Distance " + String(sensor1Distance) + " cm");
         sendData(String(sensor1Distance));
         
    }
   }//End of distance check 
   else
   {
     if(loopsWithNoObjectInfrontCount > 3)
     {
       loopsWithObjectInfrontCount = 0;
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

void sendData(String data) {
      // Read a line from Serial
      if (Serial) {
      Serial.setTimeout(100); // 100 millisecond timeout
      String s = data;
      
      // We need to convert the line to bytes, no more than 20 at this time
      uint8_t sendbuffer[20];
      s.getBytes(sendbuffer, 20);
      char sendbuffersize = min(20, s.length());

      Serial.println((char *)sendbuffer);
      Serial.print("|");
      }
}
void loop()
{  
 
  checkForCount();

 
}//End loop
