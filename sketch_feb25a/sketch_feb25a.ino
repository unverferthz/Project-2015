
#include "Ultrasonic.h"
#include <FileIO.h>



Ultrasonic ultrasonic (12,13);

void setup() {
   Serial.begin(9600); 
}

void loop()
{
 int distance = ultrasonic.Ranging(CM);
 
 if (distance < 100)
 {
  Serial.print(distance);
  Serial.print("cm ");
 }
 
  delay(100);
}
