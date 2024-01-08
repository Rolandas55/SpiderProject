 #include <stdio.h>
#include <Wire.h>
#include <Servo.h>
#include <SoftwareSerial.h>
#include <Adafruit_PWMServoDriver.h> 


Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();  

#define SERVO_FREQ 50 // Analog servos run at ~50 Hz updates

Servo servo1;
SoftwareSerial HM10(2, 3);  //RX = 2, TX = 3

struct engine_st {
  int minV;
  int maxV;
  int curr;
};

struct engine_st g_engines[16] = {
  {90, 500, 190}, {60, 540, 100}, {}, {90, 500, 100},
  {90, 500, 400}, {60, 540, 100}, {}, {90, 500, 100},
  {90, 500, 190}, {60, 540, 100}, {}, {90, 500, 100},
  {90, 500, 390}, {60, 540, 100}, {}, {90, 500, 100}
};

void inPosition ();
void gaitOne (int stepsNumber);
void gaitTwo (int stepsNumber);
void gaitThree(int stepsNumber);
void positionZero ();
void positionOne ();
void positionTwo ();
void positionThree ();

//uint8_t appData;
//int i = 0;
char buffer[10];
void (*func_gait[])(int) = {&inPosition, &gaitOne, &gaitTwo, &gaitThree};
void (*func_pos[])(void) = {&positionZero, &positionOne, &positionTwo, &positionThree};
int curr_gait = 0;


void setup() {
  Serial.begin(9600);
  HM10.begin(9600);
  pwm.begin();
  pwm.setOscillatorFrequency(27000000);
  pwm.setPWMFreq(SERVO_FREQ);
  delay(10);
}

void loop() {
  inPosition();
  
  HM10.listen();
  if (HM10.available() > 0) {
    HM10.readBytes(buffer, 10);
    if (buffer[0] == 0) {
      Serial.write(buffer[1]);
//      Serial.write('\n');
      switch (buffer[1]) {
        case 4:
//          Serial.write('i');
          curr_gait = buffer[2];
          (func_pos[curr_gait])();
          break;
        case 5:
          (func_gait[curr_gait])(buffer[2]);
          break;
      }
    }
  }
}
