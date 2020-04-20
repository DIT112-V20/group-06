#include <Smartcar.h>

BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

const int TRIGGER_PIN = 5;
const int ECHO_PIN = 18;
const unsigned int MAX_DISTANCE = 100;
SR04 sensor(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);

const int GYROSCOPE_OFFSET = 12;
GY50 gyro(GYROSCOPE_OFFSET);

const unsigned long leftPulsesPerMeter = 943;
const unsigned long rightPulsesPerMeter = 981;
DirectionalOdometer leftOdometer (smartcarlib::pins::v2::leftOdometerPins, [](){
    leftOdometer.update();
  },
  leftPulsesPerMeter);
DirectionalOdometer rightOdometer (smartcarlib::pins::v2::rightOdometerPins, [](){
    rightOdometer.update();
  },
  rightPulsesPerMeter);

SmartCar car(control, gyro, leftOdometer, rightOdometer);

void setup() {
    Serial.begin(9600);
}

void loop() {
    unsigned int distance = sensor.getDistance();
    
    if (distance != 0 && distance < 20){
      car.setSpeed(0);
    } else {
      int danceID = random(1, 4);
      handleInput(danceID);
    }
}

/** 
 *  Takes an integer and switch case determines which dance to perform
 */
void handleInput(int danceID) {
  switch (danceID) {
    case 1:
      spin();
      break;
    case 2:
      shuffle(50);
      break;
    case 3:
      shake(50);
      break;
    default:
      break;
  }
}

/**
 * Spins the car on the spot
 */
void spin() {
    rotateOnSpot(360, 100);   
}

/**
 * Rotate the car on spot at the specified degrees with the certain speed
 * @param degrees   The degrees to rotate on spot. Positive values for clockwise negative for counter-clockwise.
 * @param speed     The speed to rotate
 * 
 * Title: smartCar shield rotateOnSpot
 * Author: Dimitrios Platis
 * Date: 2020-04-20
 * Availability: https://github.com/platisd/smartcar_shield/blob/master/examples/Car/rotateOnSpot/rotateOnSpot.ino
 */
void rotateOnSpot(int targetDegrees, int speed) {
    speed = smartcarlib::utils::getAbsolute(speed);
    targetDegrees %= 360; /* puts it on a (-360,360) scale */
    
    if (!targetDegrees)
        return; /* if the target degrees is 0, don't bother doing anything */
    
    /* Let's set opposite speed for the motors on opposite sides, so it rotates on spot */
    if (targetDegrees > 0) { /* positive value means we should rotate clockwise */
        car.overrideMotorSpeed(speed, -speed); /* left motors spin forward, right motors spin backward */
    } else { /* rotate counter clockwise */
        car.overrideMotorSpeed(-speed, speed); /* left motors spin backward, right motors spin forward */
    }
    
    const auto initialHeading = car.getHeading(); /* the initial heading we'll use as offset to calculate the absolute displacement */
    int degreesTurnedSoFar = 0; /* this variable will hold the absolute displacement from the beginning of the rotation */
    
    while (abs(degreesTurnedSoFar) < abs(targetDegrees)) { /* while absolute displacement hasn't reached the (absolute) target, keep turning */
        car.update(); /* update to integrate the latest heading sensor readings */
        auto currentHeading = car.getHeading(); /* in the scale of 0 to 360 */
        
        if ((targetDegrees < 0) && (currentHeading > initialHeading)) { /* if we are turning left and the current heading is larger than the initial one 
                                                                        (e.g. started at 10 degrees and now we are at 350), we need to substract 360, 
                                                                        so to eventually get a signed displacement from the initial heading (-20) */
            currentHeading -= 360; 
        } else if ((targetDegrees > 0) && (currentHeading < initialHeading)) { /* if we are turning right and the heading is smaller than the initial one 
                                                                               (e.g. started at 350 degrees and now we are at 20), 
                                                                               so to get a signed displacement (+30) */
            currentHeading += 360;
        }
        
        degreesTurnedSoFar = initialHeading - currentHeading; /* degrees turned so far is initial heading minus current 
                                                              (initial heading is at least 0 and at most 360. To handle the "edge" cases we substracted or added 
                                                              360 to currentHeading) */
    }
    
    car.setSpeed(0); /* we have reached the target, so stop the car */
}

/**
 * Shuffle dance move
 * Car moves forwards and backwards (at set intervals) at the specified speed.
 */
void shuffle(int speed) {
  const int shortDistance = 5;
  const int mediumDistance = 10;
  const int longDistance = 20;
  
  long startingPoint = leftOdometer.getDistance();
  bool danceIsFinished = false;
  int steps = 1;
  
  car.setSpeed(speed);
  
  while(!danceIsFinished) {
    if ((steps == 1 || steps == 5) && (leftOdometer.getDirection() == 1) && ((car.getDistance() - startingPoint) == mediumDistance)) {
      changeDirection(speed); /* backwards*/
      steps++;
    } else if ((steps == 2 || steps == 4) && (leftOdometer.getDirection() == -1) && ((car.getDistance() - startingPoint) == shortDistance)) {
      changeDirection(speed); /* forwards */
      steps++;
    } else if ((steps == 3) && (leftOdometer.getDirection() == 1) && ((car.getDistance() - startingPoint) == longDistance)) {
      changeDirection(speed); /* backwards*/
      steps++;
    } else if ((steps == 6) && ((car.getDistance() - startingPoint) == 0)) {
      danceIsFinished = true;
      car.setSpeed(0);
    }
  }
}

/**
 * Changes the direction the car moves in
 */
void changeDirection(int speed) {
  if (leftOdometer.getDirection() == 1) {
    car.setSpeed(speed * -1);
  } else {
    car.setSpeed(speed);
  }
}

/**
 * Shake dance move
 */
void shake(int speed) {
  long startingPoint = 0;
  int steps = 1; 
  int repeats = 0;
  
  while (repeats != 3){
    if (steps == 1) {
      startingPoint = leftOdometer.getDistance();
      car.setAngle(-45);      
      car.setSpeed(speed * -1); /* going backwards, start of 'shake'*/
      steps++;
    } else if ((steps == 2 || steps == 4) && (car.getDistance() - startingPoint) == -20) {
      changeDirection(speed); /* going forwards, left side of "V"*/
      steps++;
    } else if (steps == 3 && (car.getDistance() - startingPoint) == 0) {
      car.setAngle(45);
      changeDirection(speed); /* going backwards, right side of "v"*/
      steps++;
    } else if (steps == 5 && (car.getDistance() - startingPoint) == 0) {
      steps = 1;
      repeats++; 
      car.setAngle(0);
    }
  } 
  
  car.setSpeed(0);
}
