void raiseLegs () {
  moveJoint(1, 85, 20);
  moveJoint(3, 150, 1);
}

void positionZero () {
  raiseLegs();
  
  servoMove(0, 190, 1);
  servoMove(4, 400, 1);
  servoMove(8, 190, 1);
  servoMove(12, 390, 1);

  moveJoint(1, 100, 1);
  moveJoint(3, 100, 1);
}

void positionOne () {
  raiseLegs();
  moveJoint(0, 300, 1);
  moveJoint(3, 150, 1);
  moveJoint(1, 200, 1);
}

void positionTwo () {
  raiseLegs();
  servoMove(0, 300, 1);
  servoMove(4, 200, 1);
  servoMove(8, 400, 1);
  servoMove(12, 300, 1);

  moveJoint(1, 200, 1);
}

void positionThree () {
  raiseLegs();
  moveJoint(0, 300, 1);
  moveJoint(3, 150, 1);
  servoMove(15, 250, 1);
  servoMove(7, 100, 1);
  moveJoint(1, 200, 1);
  servoMove(9, 120, 1);
  servoMove(8, 220, 1);
  servoMove(9, 200, 1);

  servoMove(1, 120, 1);
  servoMove(0, 380, 1);
  servoMove(1, 200, 1);
}

void gaitOne (int stepsNumber) {
  int srv[] = {0, 4, 8, 12};
  int pos[] = {250, 350, 350, 250};
  for (int i = 0; i < stepsNumber; i++) {
    inPosition();
  
    servoMove(9, 120, 1);   //priekine desine
    servoMove(8, 250, 1);
    servoMove(9, 200, 1);
    
    servoMove(5, 120, 1);   //galine desine
    servoMove(4, 250, 1);
    servoMove(5, 200, 1);
  
    servoMove(13, 120, 1);  //priekine kaire
    servoMove(12, 350, 1);
    servoMove(13, 200, 1);
    
    servoMove(1, 120, 1);   //galine kaire
    servoMove(0, 350, 1);
    servoMove(1, 200, 1);
  
    delay(100);
    moveMult(srv, pos, 4, 8);
  }
}

void gaitTwo (int stepsNumber) {
  int servo[] = {11, 8, 0, 3, 4, 12};
  int pos[] = {150, 300, 250, 250, 300, 200};
  int servom[] = {15, 12, 4, 7, 0, 8};
  int posm[] = {150, 300, 350, 250, 300, 400};

  for (int i = 0; i < stepsNumber; i++) {
    servoMove(9, 120, 1);
    servoMove(8, 250, 1);
    servoMove(11, 250, 1);
    servoMove(9, 200, 1);
  
    moveMult(servo, pos, 6, 1);
    servoMove(1, 120, 1);
    servoMove(3, 150, 1);
    servoMove(0, 400, 1);
    servoMove(1, 200, 1);
    //antra puse
  
    servoMove(13, 120, 1);
    servoMove(12, 350, 1);
    servoMove(15, 250, 1);
    servoMove(13, 200, 1);
    
    moveMult(servom, posm, 6, 1);
    servoMove(5, 120, 1);
    servoMove(7, 150, 1);
    servoMove(4, 200, 1);
    servoMove(5, 200, 1);
  }
}

void gaitThree (int stepsNumber) {
  int servo[] = {15, 7, 0, 8};
  int pos[] = {150, 200, 240, 380};

  for (int i = 0; i < stepsNumber; i++) {
  
    moveMult(servo, pos, 4, 8);

    servoMove(5, 120, 1);
    servoMove(7, 100, 1);
    servoMove(5, 200, 1);
    
    servoMove(9, 120, 1);
    servoMove(8, 220, 1);
    servoMove(9, 200, 1);
  
    servoMove(1, 120, 1);
    servoMove(0, 380, 1);
    servoMove(1, 200, 1);
  
    servoMove(13, 120, 1);
    servoMove(15, 250, 1);
    servoMove(13, 200, 1);
  }
}

void moveMult (int servo[], int pos[], int array_size, int moveSpeed) {
  void (*add[array_size])(int);
  for (int i = 0; i < array_size; i++) {
    if (pos[i] > g_engines[servo[i]].curr) {
      add[i] = &increase;
    } else {
      add[i] = &decrease;
    }
  }
  while (1) {
    bool done = true;
    for (int i = 0; i < array_size; i++) {
      if (g_engines[servo[i]].curr != pos[i]) {
        (add[i])(servo[i]);
        pwm.setPWM(servo[i], 0, g_engines[servo[i]].curr);
        done = false;
      }
    }
    delay(moveSpeed);
    if (done == true) {
      break;
    }
  }
}

void moveJoint(int joint, int pos, int moveSpeed) {
  void (*add[4])(int);
  for (int i = 0; i < 4; i++) {
    if (pos > g_engines[joint + i*4].curr) {
      add[i] = &increase;
    } else {
      add[i] = &decrease;
    }
  }
  while (1) {
    bool done = true;
    for (int i = 0; i < 4; i++) {
      if (g_engines[joint + i*4].curr != pos) {
        (add[i])(joint + i*4);
        pwm.setPWM(joint + i*4, 0, g_engines[joint + i*4].curr);
        done = false;
      }
    }
    delay(moveSpeed);
    if (done == true) {
      break;
    }
  }
}

void increase (int servo) {
  if (g_engines[servo].maxV > g_engines[servo].curr) {
    g_engines[servo].curr++;
  }
}

void decrease (int servo) {
  if (g_engines[servo].minV < g_engines[servo].curr) {
    g_engines[servo].curr--;
  }
}

void inPosition (void) {
  for (int i = 0; i < 16; i++) {
    pwm.setPWM(i, 0, g_engines[i].curr);
  }
}

void jointMove (int jointN, int addition, int speedDelay) {
  if (addition > 0) {
    for (int i = 0; i < addition; i++) {
      for (int j = 0; j < 4; j++) {
        int servo = 4 * j + jointN;
        increase(servo);
        pwm.setPWM(servo, 0, g_engines[servo].curr);
        delay(speedDelay);
      }
    }
  }
  for (int i = 0; i > addition; i--) {
      for (int j = 0; j < 4; j++) {
        int servo = 4 * j + jointN;
        decrease(servo);
        pwm.setPWM(servo, 0, g_engines[servo].curr);
        delay(speedDelay);
      }
  }
}

void servoMove (int servo, int destination, int speedDelay) {
  int travel = destination - g_engines[servo].curr;
  if (travel == 0) {
    return;
  }
  if (travel > 0) {
    for (int i = 0; i < travel; i++) {
      increase(servo);
      pwm.setPWM(servo, 0, g_engines[servo].curr);
      delay(speedDelay);
    }
  }
  else {
    for (int i = 0; i > travel; i--) {
      decrease(servo);
      pwm.setPWM(servo, 0, g_engines[servo].curr);
      delay(speedDelay);
    }  
  }
}
